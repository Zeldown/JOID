package be.zeldown.joid.impl.vulkan.render;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.function.Consumer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.EXTLineRasterization;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkFormatProperties;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;
import org.lwjgl.vulkan.VkPhysicalDeviceLineRasterizationFeaturesEXT;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSubpassDescription;

import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Context {

	public static final int TEXTURE_FORMAT = VK10.VK_FORMAT_B8G8R8A8_UNORM;

	private final VkInstance                       instance;
	private final long                             surface;
	private final VkPhysicalDevice                 physicalDevice;
	private final int                              queueFamily;
	private final boolean                          wideLines;
	private final boolean                          smoothLines;
	private final VkDevice                         device;
	private final VkQueue                          queue;
	private final long                             commandPool;
	private final int                              depthStencilFormat;
	private final long                             uniformAlignment;
	private final VkPhysicalDeviceMemoryProperties memoryProperties;
	private final long                             offscreenRenderPass;

	public Context(final long window) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			this.instance       = Context.createInstance(stack);
			this.surface        = Context.createSurface(stack, this.instance, window);
			this.physicalDevice = Context.selectPhysicalDevice(stack, this.instance, this.surface);
			this.queueFamily    = Context.findQueueFamily(stack, this.physicalDevice, this.surface);
			this.wideLines      = Context.supportsWideLines(stack, this.physicalDevice);
			this.smoothLines    = Context.supportsSmoothLines(stack, this.physicalDevice);
			this.device         = Context.createDevice(stack, this.physicalDevice, this.queueFamily, this.wideLines, this.smoothLines);

			final PointerBuffer queue = stack.mallocPointer(1);
			VK10.vkGetDeviceQueue(this.device, this.queueFamily, 0, queue);
			this.queue = new VkQueue(queue.get(0), this.device);

			final LongBuffer commandPool = stack.mallocLong(1);
			Context.check(VK10.vkCreateCommandPool(this.device, VkCommandPoolCreateInfo.calloc(stack).sType$Default().flags(VK10.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT).queueFamilyIndex(this.queueFamily), null, commandPool), "vkCreateCommandPool");
			this.commandPool = commandPool.get(0);

			final VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.malloc(stack);
			VK10.vkGetPhysicalDeviceProperties(this.physicalDevice, properties);
			this.uniformAlignment = properties.limits().minUniformBufferOffsetAlignment();

			this.memoryProperties = VkPhysicalDeviceMemoryProperties.malloc();
			VK10.vkGetPhysicalDeviceMemoryProperties(this.physicalDevice, this.memoryProperties);

			this.depthStencilFormat  = Context.selectDepthStencilFormat(stack, this.physicalDevice);
			this.offscreenRenderPass = this.createRenderPass(Context.TEXTURE_FORMAT, false, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, false);
		}
	}

	public long createRenderPass(final int colorFormat, final boolean clear, final int initialLayout, final int finalLayout, final boolean depthStencil) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final int loadOperation = clear ? VK10.VK_ATTACHMENT_LOAD_OP_CLEAR : VK10.VK_ATTACHMENT_LOAD_OP_LOAD;
			final VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc(depthStencil ? 2 : 1, stack);
			attachments.get(0)
					.format(colorFormat)
					.samples(VK10.VK_SAMPLE_COUNT_1_BIT)
					.loadOp(loadOperation)
					.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE)
					.stencilLoadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE)
					.stencilStoreOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE)
					.initialLayout(initialLayout)
					.finalLayout(finalLayout);

			final VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1, stack)
					.pipelineBindPoint(VK10.VK_PIPELINE_BIND_POINT_GRAPHICS)
					.colorAttachmentCount(1)
					.pColorAttachments(VkAttachmentReference.calloc(1, stack).attachment(0).layout(VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL));

			if (depthStencil) {
				attachments.get(1)
						.format(this.depthStencilFormat)
						.samples(VK10.VK_SAMPLE_COUNT_1_BIT)
						.loadOp(loadOperation)
						.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE)
						.stencilLoadOp(loadOperation)
						.stencilStoreOp(VK10.VK_ATTACHMENT_STORE_OP_STORE)
						.initialLayout(clear ? VK10.VK_IMAGE_LAYOUT_UNDEFINED : VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
						.finalLayout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
				subpass.pDepthStencilAttachment(VkAttachmentReference.calloc(stack).attachment(1).layout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL));
			}

			final LongBuffer renderPass = stack.mallocLong(1);
			Context.check(VK10.vkCreateRenderPass(this.device, VkRenderPassCreateInfo.calloc(stack).sType$Default().pAttachments(attachments).pSubpasses(subpass), null, renderPass), "vkCreateRenderPass");
			return renderPass.get(0);
		}
	}

	public long[] createImage(final int width, final int height, final int format, final int usage) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkImageCreateInfo info = VkImageCreateInfo.calloc(stack)
					.sType$Default()
					.imageType(VK10.VK_IMAGE_TYPE_2D)
					.format(format)
					.mipLevels(1)
					.arrayLayers(1)
					.samples(VK10.VK_SAMPLE_COUNT_1_BIT)
					.tiling(VK10.VK_IMAGE_TILING_OPTIMAL)
					.usage(usage)
					.sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE)
					.initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED);
			info.extent().set(width, height, 1);

			final LongBuffer image = stack.mallocLong(1);
			Context.check(VK10.vkCreateImage(this.device, info, null, image), "vkCreateImage");

			final VkMemoryRequirements requirements = VkMemoryRequirements.malloc(stack);
			VK10.vkGetImageMemoryRequirements(this.device, image.get(0), requirements);
			final long memory = this.allocateMemory(stack, requirements, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
			Context.check(VK10.vkBindImageMemory(this.device, image.get(0), memory, 0L), "vkBindImageMemory");
			return new long[] {image.get(0), memory};
		}
	}

	public long[] createBuffer(final long size, final int usage, final int properties) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final LongBuffer buffer = stack.mallocLong(1);
			Context.check(VK10.vkCreateBuffer(this.device, VkBufferCreateInfo.calloc(stack).sType$Default().size(size).usage(usage).sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE), null, buffer), "vkCreateBuffer");

			final VkMemoryRequirements requirements = VkMemoryRequirements.malloc(stack);
			VK10.vkGetBufferMemoryRequirements(this.device, buffer.get(0), requirements);
			final long memory = this.allocateMemory(stack, requirements, properties);
			Context.check(VK10.vkBindBufferMemory(this.device, buffer.get(0), memory, 0L), "vkBindBufferMemory");
			return new long[] {buffer.get(0), memory};
		}
	}

	public long createImageView(final long image, final int format, final int aspect) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkImageViewCreateInfo info = VkImageViewCreateInfo.calloc(stack)
					.sType$Default()
					.image(image)
					.viewType(VK10.VK_IMAGE_VIEW_TYPE_2D)
					.format(format);
			info.subresourceRange().aspectMask(aspect).baseMipLevel(0).levelCount(1).baseArrayLayer(0).layerCount(1);

			final LongBuffer view = stack.mallocLong(1);
			Context.check(VK10.vkCreateImageView(this.device, info, null, view), "vkCreateImageView");
			return view.get(0);
		}
	}

	public @NonNull VkCommandBuffer allocateCommandBuffer() {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final PointerBuffer buffer = stack.mallocPointer(1);
			Context.check(VK10.vkAllocateCommandBuffers(this.device, VkCommandBufferAllocateInfo.calloc(stack).sType$Default().commandPool(this.commandPool).level(VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY).commandBufferCount(1), buffer), "vkAllocateCommandBuffers");
			return new VkCommandBuffer(buffer.get(0), this.device);
		}
	}

	public void submit(final @NonNull Consumer<VkCommandBuffer> recorder) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkCommandBuffer buffer = this.allocateCommandBuffer();
			Context.check(VK10.vkBeginCommandBuffer(buffer, VkCommandBufferBeginInfo.calloc(stack).sType$Default().flags(VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)), "vkBeginCommandBuffer");
			recorder.accept(buffer);
			Context.check(VK10.vkEndCommandBuffer(buffer), "vkEndCommandBuffer");
			Context.check(VK10.vkQueueSubmit(this.queue, VkSubmitInfo.calloc(stack).sType$Default().pCommandBuffers(stack.pointers(buffer)), VK10.VK_NULL_HANDLE), "vkQueueSubmit");
			VK10.vkQueueWaitIdle(this.queue);
			VK10.vkFreeCommandBuffers(this.device, this.commandPool, buffer);
		}
	}

	public static void transition(final @NonNull VkCommandBuffer buffer, final long image, final int aspect, final int oldLayout, final int newLayout) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1, stack)
					.sType$Default()
					.oldLayout(oldLayout)
					.newLayout(newLayout)
					.srcQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED)
					.dstQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED)
					.image(image)
					.srcAccessMask(Context.getAccess(oldLayout))
					.dstAccessMask(Context.getAccess(newLayout));
			barrier.subresourceRange().aspectMask(aspect).baseMipLevel(0).levelCount(1).baseArrayLayer(0).layerCount(1);
			VK10.vkCmdPipelineBarrier(buffer, VK10.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, VK10.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, 0, null, null, barrier);
		}
	}

	public static void check(final int result, final @NonNull String action) {
		if (result != VK10.VK_SUCCESS) {
			throw new IllegalStateException(action + " failed with error " + result);
		}
	}

	private long allocateMemory(final MemoryStack stack, final VkMemoryRequirements requirements, final int properties) {
		final LongBuffer memory = stack.mallocLong(1);
		Context.check(VK10.vkAllocateMemory(this.device, VkMemoryAllocateInfo.calloc(stack).sType$Default().allocationSize(requirements.size()).memoryTypeIndex(this.findMemoryType(requirements.memoryTypeBits(), properties)), null, memory), "vkAllocateMemory");
		return memory.get(0);
	}

	private int findMemoryType(final int filter, final int properties) {
		for (int i = 0; i < this.memoryProperties.memoryTypeCount(); i++) {
			if ((filter & 1 << i) != 0 && (this.memoryProperties.memoryTypes(i).propertyFlags() & properties) == properties) {
				return i;
			}
		}
		throw new IllegalStateException("No Vulkan memory type matches the requested properties");
	}

	private static int getAccess(final int layout) {
		switch (layout) {
		case VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL:
			return VK10.VK_ACCESS_TRANSFER_WRITE_BIT;
		case VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL:
			return VK10.VK_ACCESS_TRANSFER_READ_BIT;
		case VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL:
			return VK10.VK_ACCESS_SHADER_READ_BIT;
		case VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL:
			return VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;
		case VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL:
			return VK10.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT;
		default:
			return 0;
		}
	}

	private static VkInstance createInstance(final MemoryStack stack) {
		final PointerBuffer extensions = GLFWVulkan.glfwGetRequiredInstanceExtensions();
		if (extensions == null) {
			throw new IllegalStateException("Vulkan is not supported on this system");
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
		Context.check(VK10.vkCreateInstance(info, null, instance), "vkCreateInstance");
		return new VkInstance(instance.get(0), info);
	}

	private static long createSurface(final MemoryStack stack, final VkInstance instance, final long window) {
		final LongBuffer surface = stack.mallocLong(1);
		Context.check(GLFWVulkan.glfwCreateWindowSurface(instance, window, null, surface), "glfwCreateWindowSurface");
		return surface.get(0);
	}

	private static VkPhysicalDevice selectPhysicalDevice(final MemoryStack stack, final VkInstance instance, final long surface) {
		final IntBuffer count = stack.mallocInt(1);
		Context.check(VK10.vkEnumeratePhysicalDevices(instance, count, null), "vkEnumeratePhysicalDevices");
		final PointerBuffer devices = stack.mallocPointer(count.get(0));
		Context.check(VK10.vkEnumeratePhysicalDevices(instance, count, devices), "vkEnumeratePhysicalDevices");

		VkPhysicalDevice selected = null;
		boolean discrete = false;
		final VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.malloc(stack);
		for (int i = 0; i < devices.capacity(); i++) {
			final VkPhysicalDevice device = new VkPhysicalDevice(devices.get(i), instance);
			VK10.vkGetPhysicalDeviceProperties(device, properties);
			if (properties.apiVersion() < VK13.VK_API_VERSION_1_3 || Context.findQueueFamily(stack, device, surface) == -1 || !Context.hasExtension(device, KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME)) {
				continue;
			}

			if (selected == null || !discrete && properties.deviceType() == VK10.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU) {
				selected = device;
				discrete = properties.deviceType() == VK10.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU;
			}
		}

		if (selected == null) {
			throw new IllegalStateException("No Vulkan 1.3 device able to present to the window was found");
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
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final IntBuffer count = stack.mallocInt(1);
			VK10.vkEnumerateDeviceExtensionProperties(device, (ByteBuffer) null, count, null);
			try (VkExtensionProperties.Buffer extensions = VkExtensionProperties.malloc(count.get(0))) {
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

	private static boolean supportsWideLines(final MemoryStack stack, final VkPhysicalDevice device) {
		final VkPhysicalDeviceFeatures features = VkPhysicalDeviceFeatures.malloc(stack);
		VK10.vkGetPhysicalDeviceFeatures(device, features);
		return features.wideLines();
	}

	private static boolean supportsSmoothLines(final MemoryStack stack, final VkPhysicalDevice device) {
		if (!Context.hasExtension(device, EXTLineRasterization.VK_EXT_LINE_RASTERIZATION_EXTENSION_NAME)) {
			return false;
		}

		final VkPhysicalDeviceLineRasterizationFeaturesEXT lineRasterization = VkPhysicalDeviceLineRasterizationFeaturesEXT.calloc(stack).sType$Default();
		VK11.vkGetPhysicalDeviceFeatures2(device, VkPhysicalDeviceFeatures2.calloc(stack).sType$Default().pNext(lineRasterization.address()));
		return lineRasterization.smoothLines();
	}

	private static int selectDepthStencilFormat(final MemoryStack stack, final VkPhysicalDevice device) {
		final VkFormatProperties properties = VkFormatProperties.malloc(stack);
		for (final int format : new int[] {VK10.VK_FORMAT_D24_UNORM_S8_UINT, VK10.VK_FORMAT_D32_SFLOAT_S8_UINT}) {
			VK10.vkGetPhysicalDeviceFormatProperties(device, format, properties);
			if ((properties.optimalTilingFeatures() & VK10.VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT) != 0) {
				return format;
			}
		}
		throw new IllegalStateException("No depth stencil format is supported by the Vulkan device");
	}

	private static VkDevice createDevice(final MemoryStack stack, final VkPhysicalDevice physicalDevice, final int queueFamily, final boolean wideLines, final boolean smoothLines) {
		final VkDeviceQueueCreateInfo.Buffer queues = VkDeviceQueueCreateInfo.calloc(1, stack);
		queues.get(0).sType$Default().queueFamilyIndex(queueFamily).pQueuePriorities(stack.floats(1F));

		final PointerBuffer extensions = stack.mallocPointer(smoothLines ? 2 : 1);
		extensions.put(stack.UTF8(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME));
		if (smoothLines) {
			extensions.put(stack.UTF8(EXTLineRasterization.VK_EXT_LINE_RASTERIZATION_EXTENSION_NAME));
		}
		extensions.flip();

		final VkDeviceCreateInfo info = VkDeviceCreateInfo.calloc(stack)
				.sType$Default()
				.pQueueCreateInfos(queues)
				.pEnabledFeatures(VkPhysicalDeviceFeatures.calloc(stack).wideLines(wideLines))
				.ppEnabledExtensionNames(extensions);
		if (smoothLines) {
			info.pNext(VkPhysicalDeviceLineRasterizationFeaturesEXT.calloc(stack).sType$Default().smoothLines(true).address());
		}

		final PointerBuffer device = stack.mallocPointer(1);
		Context.check(VK10.vkCreateDevice(physicalDevice, info, null, device), "vkCreateDevice");
		return new VkDevice(device.get(0), physicalDevice, info);
	}

}