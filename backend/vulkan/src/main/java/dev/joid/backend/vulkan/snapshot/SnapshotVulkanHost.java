package dev.joid.backend.vulkan.snapshot;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
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
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.lwjgl.vulkan.VkSubmitInfo;

import dev.joid.backend.vulkan.host.IVulkanHost;
import dev.joid.backend.vulkan.render.VulkanContext;
import lombok.Getter;
import lombok.NonNull;

public final class SnapshotVulkanHost implements IVulkanHost {

	@Getter private final VkQueue          queue;
	@Getter private final VkDevice         device;
	@Getter private final int              targetWidth;
	@Getter private final int              targetHeight;
	@Getter private final int              queueFamilyIndex;
	@Getter private final VkCommandBuffer  commandBuffer;
	@Getter private final VkPhysicalDevice physicalDevice;

	private final long          fence;
	private final long[]        target;
	private final long          commandPool;
	private final VkInstance    instance;
	private final VulkanContext context;

	@Getter private long    frameNumber;
	@Getter private boolean frameActive;

	private SnapshotVulkanHost(final int width, final int height) {
		this.targetWidth  = width;
		this.targetHeight = height;

		try (final MemoryStack stack = MemoryStack.stackPush()) {
			this.instance         = SnapshotVulkanHost.createInstance(stack);
			this.physicalDevice   = SnapshotVulkanHost.selectPhysicalDevice(stack, this.instance);
			this.queueFamilyIndex = SnapshotVulkanHost.findQueueFamily(stack, this.physicalDevice);
			this.device           = SnapshotVulkanHost.createDevice(stack, this.physicalDevice, this.queueFamilyIndex);

			final PointerBuffer queue = stack.mallocPointer(1);
			VK10.vkGetDeviceQueue(this.device, this.queueFamilyIndex, 0, queue);
			this.queue = new VkQueue(queue.get(0), this.device);

			this.commandPool = VulkanContext.createCommandPool(this.device, this.queueFamilyIndex);
			final PointerBuffer buffer = stack.mallocPointer(1);
			VulkanContext.check(VK10.vkAllocateCommandBuffers(this.device, VkCommandBufferAllocateInfo.calloc(stack).sType$Default().commandPool(this.commandPool).level(VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY).commandBufferCount(1), buffer), "vkAllocateCommandBuffers");
			this.commandBuffer = new VkCommandBuffer(buffer.get(0), this.device);

			final LongBuffer fence = stack.mallocLong(1);
			VulkanContext.check(VK10.vkCreateFence(this.device, VkFenceCreateInfo.calloc(stack).sType$Default().flags(VK10.VK_FENCE_CREATE_SIGNALED_BIT), null, fence), "vkCreateFence");
			this.fence = fence.get(0);
		}

		this.context = new VulkanContext(this);
		this.target  = this.context.createImage(width, height, this.getColorFormat(), VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT, 1);
	}

	public static @NonNull SnapshotVulkanHost create(final int width, final int height) {
		return new SnapshotVulkanHost(width, height);
	}

	public void destroy() {
		VK10.vkDeviceWaitIdle(this.device);
		this.context.destroyImage(this.target);
		this.context.destroy();
		VK10.vkDestroyFence(this.device, this.fence, null);
		VK10.vkDestroyCommandPool(this.device, this.commandPool, null);
		VK10.vkDestroyDevice(this.device, null);
		VK10.vkDestroyInstance(this.instance, null);
	}

	public @NonNull SnapshotVulkanHost beginFrame() {
		try (final MemoryStack stack = MemoryStack.stackPush()) {
			VulkanContext.check(VK10.vkWaitForFences(this.device, this.fence, true, -1L), "vkWaitForFences");
			VulkanContext.check(VK10.vkResetFences(this.device, this.fence), "vkResetFences");
			VulkanContext.check(VK10.vkResetCommandBuffer(this.commandBuffer, 0), "vkResetCommandBuffer");
			VulkanContext.check(VK10.vkBeginCommandBuffer(this.commandBuffer, VkCommandBufferBeginInfo.calloc(stack).sType$Default().flags(VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)), "vkBeginCommandBuffer");

			final long image = this.getTargetImage();
			final VkImageSubresourceRange range = VkImageSubresourceRange.calloc(stack).aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT).baseMipLevel(0).levelCount(1).baseArrayLayer(0).layerCount(1);
			VulkanContext.transition(this.commandBuffer, image, VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_UNDEFINED, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
			VK10.vkCmdClearColorImage(this.commandBuffer, image, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VkClearColorValue.calloc(stack), range);
			VulkanContext.transition(this.commandBuffer, image, VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
		}

		this.frameNumber++;
		this.frameActive = true;
		return this;
	}

	public @NonNull SnapshotVulkanHost endFrame() {
		this.frameActive = false;
		VulkanContext.check(VK10.vkEndCommandBuffer(this.commandBuffer), "vkEndCommandBuffer");
		try (final MemoryStack stack = MemoryStack.stackPush()) {
			VulkanContext.check(VK10.vkQueueSubmit(this.queue, VkSubmitInfo.calloc(stack).sType$Default().pCommandBuffers(stack.pointers(this.commandBuffer)), this.fence), "vkQueueSubmit");
		}
		return this;
	}

	@Override
	public int getFrameIndex() {
		return 0;
	}

	@Override
	public int getFramesInFlight() {
		return 1;
	}

	@Override
	public int getColorFormat() {
		return VK10.VK_FORMAT_B8G8R8A8_UNORM;
	}

	@Override
	public long getTargetView() {
		return this.target[2];
	}

	public long getTargetImage() {
		return this.target[0];
	}

	private static VkInstance createInstance(final MemoryStack stack) {
		final VkApplicationInfo application = VkApplicationInfo.calloc(stack)
				.sType$Default()
				.pApplicationName(stack.UTF8("JOID"))
				.pEngineName(stack.UTF8("JOID"))
				.apiVersion(VK13.VK_API_VERSION_1_3);
		final VkInstanceCreateInfo info = VkInstanceCreateInfo.calloc(stack)
				.sType$Default()
				.pApplicationInfo(application);

		final PointerBuffer instance = stack.mallocPointer(1);
		VulkanContext.check(VK10.vkCreateInstance(info, null, instance), "vkCreateInstance");
		return new VkInstance(instance.get(0), info);
	}

	private static VkPhysicalDevice selectPhysicalDevice(final MemoryStack stack, final VkInstance instance) {
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
			if (properties.apiVersion() < VK13.VK_API_VERSION_1_3 || SnapshotVulkanHost.findQueueFamily(stack, device) == -1) {
				continue;
			}

			if (selected == null || !discrete && properties.deviceType() == VK10.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU) {
				selected = device;
				discrete = properties.deviceType() == VK10.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU;
			}
		}

		if (selected == null) {
			throw new IllegalStateException("No Vulkan 1.3 device able to draw was found, update the graphics driver or use an OpenGL backend");
		}
		return selected;
	}

	private static int findQueueFamily(final MemoryStack stack, final VkPhysicalDevice device) {
		final IntBuffer count = stack.mallocInt(1);
		VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, count, null);
		final VkQueueFamilyProperties.Buffer families = VkQueueFamilyProperties.malloc(count.get(0), stack);
		VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, count, families);

		for (int i = 0; i < families.capacity(); i++) {
			if ((families.get(i).queueFlags() & VK10.VK_QUEUE_GRAPHICS_BIT) != 0) {
				return i;
			}
		}
		return -1;
	}

	private static VkDevice createDevice(final MemoryStack stack, final VkPhysicalDevice physicalDevice, final int queueFamily) {
		final VkDeviceQueueCreateInfo.Buffer queues = VkDeviceQueueCreateInfo.calloc(1, stack);
		queues.get(0).sType$Default().queueFamilyIndex(queueFamily).pQueuePriorities(stack.floats(1F));

		final PointerBuffer device = stack.mallocPointer(1);
		final VkDeviceCreateInfo info = VkDeviceCreateInfo.calloc(stack).sType$Default().pQueueCreateInfos(queues);
		VulkanContext.check(VK10.vkCreateDevice(physicalDevice, info, null, device), "vkCreateDevice");
		return new VkDevice(device.get(0), physicalDevice, info);
	}

}