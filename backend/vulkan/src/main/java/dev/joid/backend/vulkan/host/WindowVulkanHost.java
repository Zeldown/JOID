package dev.joid.backend.vulkan.host;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkClearColorValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import dev.joid.backend.vulkan.render.VulkanContext;
import lombok.Getter;
import lombok.NonNull;

public final class WindowVulkanHost implements IVulkanHost {

	@Getter private final VkQueue          queue;
	@Getter private final VkDevice         device;
	@Getter private final int              colorFormat;
	@Getter private final int              framesInFlight;
	@Getter private final VkInstance       instance;
	@Getter private final int              queueFamilyIndex;
	@Getter private final VkPhysicalDevice physicalDevice;

	private final long              window;
	private final long              surface;
	private final long[]            fences;
	private final int               colorSpace;
	private final long              commandPool;
	private final long[]            imageSemaphores;
	private final VkCommandBuffer[] commandBuffers;

	@Getter private int     frameIndex;
	@Getter private int     targetWidth;
	@Getter private int     targetHeight;
	@Getter private long    frameNumber;
	@Getter private boolean frameActive;

	private long[] views;
	private long[] images;
	private int    imageIndex;
	private long   swapchain;
	private long[] renderSemaphores;

	private WindowVulkanHost(final long window, final int framesInFlight) {
		this.window         = window;
		this.framesInFlight = framesInFlight;

		try (final MemoryStack stack = MemoryStack.stackPush()) {
			this.instance         = WindowVulkanHost.createInstance(stack);
			this.surface          = WindowVulkanHost.createSurface(stack, this.instance, window);
			this.physicalDevice   = WindowVulkanHost.selectPhysicalDevice(stack, this.instance, this.surface);
			this.queueFamilyIndex = WindowVulkanHost.findQueueFamily(stack, this.physicalDevice, this.surface);
			this.device           = WindowVulkanHost.createDevice(stack, this.physicalDevice, this.queueFamilyIndex);

			final PointerBuffer queue = stack.mallocPointer(1);
			VK10.vkGetDeviceQueue(this.device, this.queueFamilyIndex, 0, queue);
			this.queue = new VkQueue(queue.get(0), this.device);

			final IntBuffer count = stack.mallocInt(1);
			KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(this.physicalDevice, this.surface, count, null);
			final VkSurfaceFormatKHR.Buffer formats = VkSurfaceFormatKHR.malloc(count.get(0), stack);
			KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(this.physicalDevice, this.surface, count, formats);

			VkSurfaceFormatKHR selected = formats.get(0);
			for (final VkSurfaceFormatKHR format : formats) {
				if (format.format() == VK10.VK_FORMAT_B8G8R8A8_UNORM && format.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
					selected = format;
				}
			}
			this.colorFormat = selected.format();
			this.colorSpace  = selected.colorSpace();

			this.commandPool = VulkanContext.createCommandPool(this.device, this.queueFamilyIndex);

			final PointerBuffer buffers = stack.mallocPointer(framesInFlight);
			VulkanContext.check(VK10.vkAllocateCommandBuffers(this.device, VkCommandBufferAllocateInfo.calloc(stack).sType$Default().commandPool(this.commandPool).level(VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY).commandBufferCount(framesInFlight), buffers), "vkAllocateCommandBuffers");
			this.commandBuffers  = new VkCommandBuffer[framesInFlight];
			this.fences          = new long[framesInFlight];
			this.imageSemaphores = new long[framesInFlight];
			for (int i = 0; i < framesInFlight; i++) {
				this.commandBuffers[i] = new VkCommandBuffer(buffers.get(i), this.device);
				this.imageSemaphores[i] = WindowVulkanHost.createSemaphore(stack, this.device);

				final LongBuffer fence = stack.mallocLong(1);
				VulkanContext.check(VK10.vkCreateFence(this.device, VkFenceCreateInfo.calloc(stack).sType$Default().flags(VK10.VK_FENCE_CREATE_SIGNALED_BIT), null, fence), "vkCreateFence");
				this.fences[i] = fence.get(0);
			}
		}

		this.createSwapchain();
	}

	public static @NonNull WindowVulkanHost create(final long window) {
		return WindowVulkanHost.create(window, 2);
	}

	public static @NonNull WindowVulkanHost create(final long window, final int framesInFlight) {
		if (framesInFlight < 1) {
			throw new IllegalArgumentException("A Vulkan window host needs at least one frame in flight, got " + framesInFlight + ": pass 1 to wait for each frame, 2 or more to record the next frame while the GPU draws the previous ones");
		}
		return new WindowVulkanHost(window, framesInFlight);
	}

	public void destroy() {
		if (this.frameActive) {
			this.endFrame();
		}

		VK10.vkDeviceWaitIdle(this.device);
		this.destroySwapchainResources();
		KHRSwapchain.vkDestroySwapchainKHR(this.device, this.swapchain, null);
		for (int i = 0; i < this.framesInFlight; i++) {
			VK10.vkDestroyFence(this.device, this.fences[i], null);
			VK10.vkDestroySemaphore(this.device, this.imageSemaphores[i], null);
		}

		VK10.vkDestroyCommandPool(this.device, this.commandPool, null);
		VK10.vkDestroyDevice(this.device, null);
		KHRSurface.vkDestroySurfaceKHR(this.instance, this.surface, null);
		VK10.vkDestroyInstance(this.instance, null);
	}

	public @NonNull WindowVulkanHost beginFrame() {
		if (this.frameActive) {
			throw new IllegalStateException("The Vulkan window frame has already begun, call endFrame() first");
		}

		if (this.isOutdated()) {
			this.recreate();
		}

		try (final MemoryStack stack = MemoryStack.stackPush()) {
			VulkanContext.check(VK10.vkWaitForFences(this.device, this.fences[this.frameIndex], true, -1L), "vkWaitForFences");

			final IntBuffer index = stack.mallocInt(1);
			int result = KHRSwapchain.vkAcquireNextImageKHR(this.device, this.swapchain, -1L, this.imageSemaphores[this.frameIndex], VK10.VK_NULL_HANDLE, index);
			if (result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) {
				this.recreate();
				result = KHRSwapchain.vkAcquireNextImageKHR(this.device, this.swapchain, -1L, this.imageSemaphores[this.frameIndex], VK10.VK_NULL_HANDLE, index);
			}

			if (result != KHRSwapchain.VK_SUBOPTIMAL_KHR) {
				VulkanContext.check(result, "vkAcquireNextImageKHR");
			}

			this.imageIndex = index.get(0);
			VulkanContext.check(VK10.vkResetFences(this.device, this.fences[this.frameIndex]), "vkResetFences");

			final VkCommandBuffer buffer = this.getCommandBuffer();
			VulkanContext.check(VK10.vkResetCommandBuffer(buffer, 0), "vkResetCommandBuffer");
			VulkanContext.check(VK10.vkBeginCommandBuffer(buffer, VkCommandBufferBeginInfo.calloc(stack).sType$Default().flags(VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)), "vkBeginCommandBuffer");

			final long image = this.getTargetImage();
			final VkImageSubresourceRange range = VkImageSubresourceRange.calloc(stack).aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT).baseMipLevel(0).levelCount(1).baseArrayLayer(0).layerCount(1);
			VulkanContext.transition(buffer, image, VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_UNDEFINED, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
			VK10.vkCmdClearColorImage(buffer, image, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VkClearColorValue.calloc(stack), range);
			VulkanContext.transition(buffer, image, VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
		}

		this.frameNumber++;
		this.frameActive = true;
		return this;
	}

	public @NonNull WindowVulkanHost endFrame() {
		if (!this.frameActive) {
			throw new IllegalStateException("The Vulkan window frame has not begun, call beginFrame() first");
		}

		this.frameActive = false;
		final VkCommandBuffer buffer = this.getCommandBuffer();
		VulkanContext.transition(buffer, this.getTargetImage(), VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);
		VulkanContext.check(VK10.vkEndCommandBuffer(buffer), "vkEndCommandBuffer");

		try (final MemoryStack stack = MemoryStack.stackPush()) {
			final VkSubmitInfo submit = VkSubmitInfo.calloc(stack)
					.sType$Default()
					.waitSemaphoreCount(1)
					.pWaitSemaphores(stack.longs(this.imageSemaphores[this.frameIndex]))
					.pWaitDstStageMask(stack.ints(VK10.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT))
					.pCommandBuffers(stack.pointers(buffer))
					.pSignalSemaphores(stack.longs(this.renderSemaphores[this.imageIndex]));
			VulkanContext.check(VK10.vkQueueSubmit(this.queue, submit, this.fences[this.frameIndex]), "vkQueueSubmit");

			final VkPresentInfoKHR present = VkPresentInfoKHR.calloc(stack)
					.sType$Default()
					.pWaitSemaphores(stack.longs(this.renderSemaphores[this.imageIndex]))
					.swapchainCount(1)
					.pSwapchains(stack.longs(this.swapchain))
					.pImageIndices(stack.ints(this.imageIndex));
			final int result = KHRSwapchain.vkQueuePresentKHR(this.queue, present);
			this.frameIndex = (this.frameIndex + 1) % this.framesInFlight;
			if (result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR || result == KHRSwapchain.VK_SUBOPTIMAL_KHR) {
				this.recreate();
			} else {
				VulkanContext.check(result, "vkQueuePresentKHR");
			}
		}
		return this;
	}

	@Override
	public long getTargetView() {
		return this.views[this.imageIndex];
	}

	public long getTargetImage() {
		return this.images[this.imageIndex];
	}

	@Override
	public @NonNull VkCommandBuffer getCommandBuffer() {
		return this.commandBuffers[this.frameIndex];
	}

	private boolean isOutdated() {
		final int[] width = new int[1];
		final int[] height = new int[1];
		GLFW.glfwGetFramebufferSize(this.window, width, height);
		return width[0] != this.targetWidth || height[0] != this.targetHeight;
	}

	private void recreate() {
		final int[] width = new int[1];
		final int[] height = new int[1];
		GLFW.glfwGetFramebufferSize(this.window, width, height);
		while ((width[0] == 0 || height[0] == 0) && !GLFW.glfwWindowShouldClose(this.window)) {
			GLFW.glfwWaitEvents();
			GLFW.glfwGetFramebufferSize(this.window, width, height);
		}

		if (width[0] == 0 || height[0] == 0) {
			return;
		}

		VK10.vkDeviceWaitIdle(this.device);
		this.destroySwapchainResources();
		this.createSwapchain();
	}

	private void createSwapchain() {
		try (final MemoryStack stack = MemoryStack.stackPush()) {
			final VkSurfaceCapabilitiesKHR capabilities = VkSurfaceCapabilitiesKHR.malloc(stack);
			VulkanContext.check(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(this.physicalDevice, this.surface, capabilities), "vkGetPhysicalDeviceSurfaceCapabilitiesKHR");

			final int[] framebufferWidth = new int[1];
			final int[] framebufferHeight = new int[1];
			GLFW.glfwGetFramebufferSize(this.window, framebufferWidth, framebufferHeight);
			this.targetWidth = capabilities.currentExtent().width() != -1 ? capabilities.currentExtent().width() : Math.max(capabilities.minImageExtent().width(), Math.min(capabilities.maxImageExtent().width(), framebufferWidth[0]));
			this.targetHeight = capabilities.currentExtent().height() != -1 ? capabilities.currentExtent().height() : Math.max(capabilities.minImageExtent().height(), Math.min(capabilities.maxImageExtent().height(), framebufferHeight[0]));

			int imageCount = capabilities.minImageCount() + 1;
			if (capabilities.maxImageCount() > 0) {
				imageCount = Math.min(imageCount, capabilities.maxImageCount());
			}

			final VkSwapchainCreateInfoKHR info = VkSwapchainCreateInfoKHR.calloc(stack)
					.sType$Default()
					.surface(this.surface)
					.minImageCount(imageCount)
					.imageFormat(this.colorFormat)
					.imageColorSpace(this.colorSpace)
					.imageArrayLayers(1)
					.imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT)
					.imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE)
					.preTransform(capabilities.currentTransform())
					.compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
					.presentMode(this.selectPresentMode(stack))
					.clipped(true)
					.oldSwapchain(this.swapchain);
			info.imageExtent().set(this.targetWidth, this.targetHeight);

			final LongBuffer swapchain = stack.mallocLong(1);
			VulkanContext.check(KHRSwapchain.vkCreateSwapchainKHR(this.device, info, null, swapchain), "vkCreateSwapchainKHR");
			if (this.swapchain != VK10.VK_NULL_HANDLE) {
				KHRSwapchain.vkDestroySwapchainKHR(this.device, this.swapchain, null);
			}
			this.swapchain = swapchain.get(0);

			final IntBuffer count = stack.mallocInt(1);
			KHRSwapchain.vkGetSwapchainImagesKHR(this.device, this.swapchain, count, null);
			final LongBuffer images = stack.mallocLong(count.get(0));
			KHRSwapchain.vkGetSwapchainImagesKHR(this.device, this.swapchain, count, images);

			this.images           = new long[count.get(0)];
			this.views            = new long[count.get(0)];
			this.renderSemaphores = new long[count.get(0)];
			for (int i = 0; i < this.images.length; i++) {
				this.images[i] = images.get(i);
				this.views[i] = VulkanContext.createImageView(this.device, this.images[i], this.colorFormat, VK10.VK_IMAGE_ASPECT_COLOR_BIT, 1);
				this.renderSemaphores[i] = WindowVulkanHost.createSemaphore(stack, this.device);
			}
		}
	}

	private int selectPresentMode(final MemoryStack stack) {
		final IntBuffer count = stack.mallocInt(1);
		KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(this.physicalDevice, this.surface, count, null);
		final IntBuffer modes = stack.mallocInt(count.get(0));
		KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(this.physicalDevice, this.surface, count, modes);

		for (int i = 0; i < modes.capacity(); i++) {
			if (modes.get(i) == KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR) {
				return KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR;
			}
		}
		return KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
	}

	private void destroySwapchainResources() {
		for (int i = 0; i < this.images.length; i++) {
			VK10.vkDestroyImageView(this.device, this.views[i], null);
			VK10.vkDestroySemaphore(this.device, this.renderSemaphores[i], null);
		}
	}

	private static long createSemaphore(final MemoryStack stack, final VkDevice device) {
		final LongBuffer semaphore = stack.mallocLong(1);
		VulkanContext.check(VK10.vkCreateSemaphore(device, VkSemaphoreCreateInfo.calloc(stack).sType$Default(), null, semaphore), "vkCreateSemaphore");
		return semaphore.get(0);
	}

	private static VkInstance createInstance(final MemoryStack stack) {
		final PointerBuffer extensions = GLFWVulkan.glfwGetRequiredInstanceExtensions();
		if (extensions == null) {
			throw new IllegalStateException("Vulkan is not supported on this system, update the graphics driver or use an OpenGL backend");
		}

		final VkApplicationInfo application = VkApplicationInfo.calloc(stack)
				.sType$Default()
				.pApplicationName(stack.UTF8("JOID"))
				.pEngineName(stack.UTF8("JOID"))
				.apiVersion(VK13.VK_API_VERSION_1_3);
		final VkInstanceCreateInfo info = VkInstanceCreateInfo.calloc(stack)
				.sType$Default()
				.pApplicationInfo(application)
				.ppEnabledExtensionNames(extensions);

		final PointerBuffer instance = stack.mallocPointer(1);
		VulkanContext.check(VK10.vkCreateInstance(info, null, instance), "vkCreateInstance");
		return new VkInstance(instance.get(0), info);
	}

	private static long createSurface(final MemoryStack stack, final VkInstance instance, final long window) {
		final LongBuffer surface = stack.mallocLong(1);
		VulkanContext.check(GLFWVulkan.glfwCreateWindowSurface(instance, window, null, surface), "glfwCreateWindowSurface");
		return surface.get(0);
	}

	private static VkPhysicalDevice selectPhysicalDevice(final MemoryStack stack, final VkInstance instance, final long surface) {
		final IntBuffer count = stack.mallocInt(1);
		VulkanContext.check(VK10.vkEnumeratePhysicalDevices(instance, count, null), "vkEnumeratePhysicalDevices");
		final PointerBuffer devices = stack.mallocPointer(count.get(0));
		VulkanContext.check(VK10.vkEnumeratePhysicalDevices(instance, count, devices), "vkEnumeratePhysicalDevices");

		VkPhysicalDevice selected = null;
		boolean discrete = false;
		final VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.malloc(stack);
		for (int i = 0; i < devices.capacity(); i++) {
			final VkPhysicalDevice device = new VkPhysicalDevice(devices.get(i), instance);
			VK10.vkGetPhysicalDeviceProperties(device, properties);
			if (properties.apiVersion() < VK13.VK_API_VERSION_1_3 || WindowVulkanHost.findQueueFamily(stack, device, surface) == -1 || !WindowVulkanHost.hasExtension(device, KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME)) {
				continue;
			}

			if (selected == null || !discrete && properties.deviceType() == VK10.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU) {
				selected = device;
				discrete = properties.deviceType() == VK10.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU;
			}
		}

		if (selected == null) {
			throw new IllegalStateException("No Vulkan 1.3 device able to present to the window was found, update the graphics driver or use an OpenGL backend");
		}
		return selected;
	}

	private static int findQueueFamily(final MemoryStack stack, final VkPhysicalDevice device, final long surface) {
		final IntBuffer count = stack.mallocInt(1);
		VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, count, null);
		final VkQueueFamilyProperties.Buffer families = VkQueueFamilyProperties.malloc(count.get(0), stack);
		VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, count, families);

		final IntBuffer supported = stack.mallocInt(1);
		for (int i = 0; i < families.capacity(); i++) {
			KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(device, i, surface, supported);
			if ((families.get(i).queueFlags() & VK10.VK_QUEUE_GRAPHICS_BIT) != 0 && supported.get(0) == VK10.VK_TRUE) {
				return i;
			}
		}
		return -1;
	}

	private static boolean hasExtension(final VkPhysicalDevice device, final String name) {
		try (final MemoryStack stack = MemoryStack.stackPush()) {
			final IntBuffer count = stack.mallocInt(1);
			VK10.vkEnumerateDeviceExtensionProperties(device, (ByteBuffer) null, count, null);
			try (final VkExtensionProperties.Buffer extensions = VkExtensionProperties.malloc(count.get(0))) {
				VK10.vkEnumerateDeviceExtensionProperties(device, (ByteBuffer) null, count, extensions);
				for (final VkExtensionProperties extension : extensions) {
					if (name.equals(extension.extensionNameString())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static VkDevice createDevice(final MemoryStack stack, final VkPhysicalDevice physicalDevice, final int queueFamily) {
		final VkDeviceQueueCreateInfo.Buffer queues = VkDeviceQueueCreateInfo.calloc(1, stack);
		queues.get(0).sType$Default().queueFamilyIndex(queueFamily).pQueuePriorities(stack.floats(1F));

		final VkDeviceCreateInfo info = VkDeviceCreateInfo.calloc(stack)
				.sType$Default()
				.pQueueCreateInfos(queues)
				.ppEnabledExtensionNames(stack.pointers(stack.UTF8(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME)));

		final PointerBuffer device = stack.mallocPointer(1);
		VulkanContext.check(VK10.vkCreateDevice(physicalDevice, info, null, device), "vkCreateDevice");
		return new VkDevice(device.get(0), physicalDevice, info);
	}

}