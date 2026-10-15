package dev.joid.backend.vulkan.render;

import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.function.Consumer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFormatProperties;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;

import dev.joid.backend.vulkan.host.IVulkanHost;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class VulkanContext {

	public static final int TEXTURE_FORMAT = VK10.VK_FORMAT_B8G8R8A8_UNORM;

	private final VkQueue                          queue;
	private final VkDevice                         device;
	private final int                              depthFormat;
	private final long                             commandPool;
	private final String                           deviceName;
	private final int                              maxTextureSize;
	private final long                             uniformAlignment;
	private final int                              depthStencilFormat;
	private final long                             offscreenRenderPass;
	private final VkPhysicalDevice                 physicalDevice;
	private final VkPhysicalDeviceMemoryProperties memoryProperties;

	private VulkanContext(final IVulkanHost host) {
		this.queue          = host.getQueue();
		this.device         = host.getDevice();
		this.physicalDevice = host.getPhysicalDevice();

		this.commandPool    = VulkanContext.createCommandPool(this.device, host.getQueueFamilyIndex());

		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.malloc(stack);
			VK10.vkGetPhysicalDeviceProperties(this.physicalDevice, properties);
			this.deviceName       = properties.deviceNameString();
			this.maxTextureSize   = properties.limits().maxImageDimension2D();
			this.uniformAlignment = properties.limits().minUniformBufferOffsetAlignment();

			this.memoryProperties = VkPhysicalDeviceMemoryProperties.malloc();
			VK10.vkGetPhysicalDeviceMemoryProperties(this.physicalDevice, this.memoryProperties);

			this.depthFormat         = VulkanContext.selectDepthFormat(stack, this.physicalDevice, VK10.VK_FORMAT_X8_D24_UNORM_PACK32, VK10.VK_FORMAT_D32_SFLOAT, VK10.VK_FORMAT_D16_UNORM);
			this.depthStencilFormat  = VulkanContext.selectDepthFormat(stack, this.physicalDevice, VK10.VK_FORMAT_D24_UNORM_S8_UINT, VK10.VK_FORMAT_D32_SFLOAT_S8_UINT);
			this.offscreenRenderPass = this.createRenderPass(VulkanContext.TEXTURE_FORMAT, this.depthFormat, false);
		}
	}

	public static @NonNull VulkanContext create(final IVulkanHost host) {
		return new VulkanContext(host);
	}

	public void destroy() {
		VK10.vkDestroyRenderPass(this.device, this.offscreenRenderPass, null);
		VK10.vkDestroyCommandPool(this.device, this.commandPool, null);
		this.memoryProperties.free();
	}

	public long createRenderPass(final int colorFormat, final int depthFormat, final boolean clearDepth) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc(2, stack);
			attachments.get(0)
					.format(colorFormat)
					.samples(VK10.VK_SAMPLE_COUNT_1_BIT)
					.loadOp(VK10.VK_ATTACHMENT_LOAD_OP_LOAD)
					.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE)
					.stencilLoadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE)
					.stencilStoreOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE)
					.initialLayout(VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
					.finalLayout(VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

			final int depthLoadOperation = clearDepth ? VK10.VK_ATTACHMENT_LOAD_OP_CLEAR : VK10.VK_ATTACHMENT_LOAD_OP_LOAD;
			final boolean stencil = VulkanContext.hasStencil(depthFormat);
			attachments.get(1)
					.format(depthFormat)
					.samples(VK10.VK_SAMPLE_COUNT_1_BIT)
					.loadOp(depthLoadOperation)
					.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE)
					.stencilLoadOp(stencil ? depthLoadOperation : VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE)
					.stencilStoreOp(stencil ? VK10.VK_ATTACHMENT_STORE_OP_STORE : VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE)
					.initialLayout(clearDepth ? VK10.VK_IMAGE_LAYOUT_UNDEFINED : VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
					.finalLayout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

			final VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1, stack)
					.pipelineBindPoint(VK10.VK_PIPELINE_BIND_POINT_GRAPHICS)
					.colorAttachmentCount(1)
					.pColorAttachments(VkAttachmentReference.calloc(1, stack).attachment(0).layout(VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL))
					.pDepthStencilAttachment(VkAttachmentReference.calloc(stack).attachment(1).layout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL));

			final int stages = VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT | VK10.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT | VK10.VK_PIPELINE_STAGE_LATE_FRAGMENT_TESTS_BIT;
			final int access = VK10.VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT | VK10.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT | VK10.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT;
			final VkSubpassDependency.Buffer dependency = VkSubpassDependency.calloc(1, stack)
					.srcSubpass(VK10.VK_SUBPASS_EXTERNAL)
					.dstSubpass(0)
					.srcStageMask(stages)
					.dstStageMask(stages)
					.srcAccessMask(access)
					.dstAccessMask(access);

			final LongBuffer renderPass = stack.mallocLong(1);
			VulkanContext.check(VK10.vkCreateRenderPass(this.device, VkRenderPassCreateInfo.calloc(stack).sType$Default().pAttachments(attachments).pSubpasses(subpass).pDependencies(dependency), null, renderPass), "vkCreateRenderPass");
			return renderPass.get(0);
		}
	}

	public long createImageView(final long image, final int format, final int aspect) {
		return VulkanContext.createImageView(this.device, image, format, aspect, 1);
	}

	public long createImageView(final long image, final int format, final int aspect, final int levels) {
		return VulkanContext.createImageView(this.device, image, format, aspect, levels);
	}

	public long[] createImage(final int width, final int height, final int format, final int usage) {
		return this.createImage(width, height, format, usage, 1);
	}

	public long[] createImage(final int width, final int height, final int format, final int usage, final int levels) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkImageCreateInfo info = VkImageCreateInfo.calloc(stack)
					.sType$Default()
					.imageType(VK10.VK_IMAGE_TYPE_2D)
					.format(format)
					.mipLevels(levels)
					.arrayLayers(1)
					.samples(VK10.VK_SAMPLE_COUNT_1_BIT)
					.tiling(VK10.VK_IMAGE_TILING_OPTIMAL)
					.usage(usage)
					.sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE)
					.initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED);
			info.extent().set(width, height, 1);

			final LongBuffer image = stack.mallocLong(1);
			VulkanContext.check(VK10.vkCreateImage(this.device, info, null, image), "vkCreateImage");

			final VkMemoryRequirements requirements = VkMemoryRequirements.malloc(stack);
			VK10.vkGetImageMemoryRequirements(this.device, image.get(0), requirements);
			final long memory = this.allocateMemory(stack, requirements, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
			VulkanContext.check(VK10.vkBindImageMemory(this.device, image.get(0), memory, 0L), "vkBindImageMemory");
			return new long[] {image.get(0), memory};
		}
	}

	public long[] createBuffer(final long size, final int usage, final int properties) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final LongBuffer buffer = stack.mallocLong(1);
			VulkanContext.check(VK10.vkCreateBuffer(this.device, VkBufferCreateInfo.calloc(stack).sType$Default().size(size).usage(usage).sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE), null, buffer), "vkCreateBuffer");

			final VkMemoryRequirements requirements = VkMemoryRequirements.malloc(stack);
			VK10.vkGetBufferMemoryRequirements(this.device, buffer.get(0), requirements);
			final long memory = this.allocateMemory(stack, requirements, properties);
			VulkanContext.check(VK10.vkBindBufferMemory(this.device, buffer.get(0), memory, 0L), "vkBindBufferMemory");
			return new long[] {buffer.get(0), memory};
		}
	}

	public void submit(final @NonNull Consumer<VkCommandBuffer> recorder) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final PointerBuffer handle = stack.mallocPointer(1);
			VulkanContext.check(VK10.vkAllocateCommandBuffers(this.device, VkCommandBufferAllocateInfo.calloc(stack).sType$Default().commandPool(this.commandPool).level(VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY).commandBufferCount(1), handle), "vkAllocateCommandBuffers");
			final VkCommandBuffer buffer = new VkCommandBuffer(handle.get(0), this.device);
			VulkanContext.check(VK10.vkBeginCommandBuffer(buffer, VkCommandBufferBeginInfo.calloc(stack).sType$Default().flags(VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)), "vkBeginCommandBuffer");
			recorder.accept(buffer);
			VulkanContext.check(VK10.vkEndCommandBuffer(buffer), "vkEndCommandBuffer");
			VulkanContext.check(VK10.vkQueueSubmit(this.queue, VkSubmitInfo.calloc(stack).sType$Default().pCommandBuffers(stack.pointers(buffer)), VK10.VK_NULL_HANDLE), "vkQueueSubmit");
			VK10.vkQueueWaitIdle(this.queue);
			VK10.vkFreeCommandBuffers(this.device, this.commandPool, buffer);
		}
	}

	public static void transition(final @NonNull VkCommandBuffer buffer, final long image, final int aspect, final int oldLayout, final int newLayout) {
		VulkanContext.transition(buffer, image, aspect, oldLayout, newLayout, 0, 1);
	}

	public static void transition(final @NonNull VkCommandBuffer buffer, final long image, final int aspect, final int oldLayout, final int newLayout, final int baseLevel, final int levels) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1, stack)
					.sType$Default()
					.oldLayout(oldLayout)
					.newLayout(newLayout)
					.srcQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED)
					.dstQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED)
					.image(image)
					.srcAccessMask(VulkanContext.getAccess(oldLayout))
					.dstAccessMask(VulkanContext.getAccess(newLayout));
			barrier.subresourceRange().aspectMask(aspect).baseMipLevel(baseLevel).levelCount(levels).baseArrayLayer(0).layerCount(1);
			VK10.vkCmdPipelineBarrier(buffer, VK10.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, VK10.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, 0, null, null, barrier);
		}
	}

	public static long createCommandPool(final @NonNull VkDevice device, final int queueFamilyIndex) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final LongBuffer commandPool = stack.mallocLong(1);
			VulkanContext.check(VK10.vkCreateCommandPool(device, VkCommandPoolCreateInfo.calloc(stack).sType$Default().flags(VK10.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT).queueFamilyIndex(queueFamilyIndex), null, commandPool), "vkCreateCommandPool");
			return commandPool.get(0);
		}
	}

	public static long createImageView(final @NonNull VkDevice device, final long image, final int format, final int aspect, final int levels) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final VkImageViewCreateInfo info = VkImageViewCreateInfo.calloc(stack)
					.sType$Default()
					.image(image)
					.viewType(VK10.VK_IMAGE_VIEW_TYPE_2D)
					.format(format);
			info.subresourceRange().aspectMask(aspect).baseMipLevel(0).levelCount(levels).baseArrayLayer(0).layerCount(1);

			final LongBuffer view = stack.mallocLong(1);
			VulkanContext.check(VK10.vkCreateImageView(device, info, null, view), "vkCreateImageView");
			return view.get(0);
		}
	}

	public static void check(final int result, final @NonNull String action) {
		if (result != VK10.VK_SUCCESS) {
			throw new IllegalStateException(action + " failed with the Vulkan error " + result + ", update the graphics driver or run with the Vulkan validation layers to find the call at fault");
		}
	}

	private long allocateMemory(final MemoryStack stack, final VkMemoryRequirements requirements, final int properties) {
		final LongBuffer memory = stack.mallocLong(1);
		VulkanContext.check(VK10.vkAllocateMemory(this.device, VkMemoryAllocateInfo.calloc(stack).sType$Default().allocationSize(requirements.size()).memoryTypeIndex(this.findMemoryType(requirements.memoryTypeBits(), properties)), null, memory), "vkAllocateMemory");
		return memory.get(0);
	}

	private int findMemoryType(final int filter, final int properties) {
		for (int i = 0; i < this.memoryProperties.memoryTypeCount(); i++) {
			if ((filter & 1 << i) != 0 && (this.memoryProperties.memoryTypes(i).propertyFlags() & properties) == properties) {
				return i;
			}
		}
		throw new IllegalStateException("No Vulkan memory type of the device matches the properties " + properties + " for the types " + Integer.toBinaryString(filter) + ", update the graphics driver or use another device");
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

	private static boolean hasStencil(final int format) {
		return format == VK10.VK_FORMAT_D16_UNORM_S8_UINT || format == VK10.VK_FORMAT_D24_UNORM_S8_UINT || format == VK10.VK_FORMAT_D32_SFLOAT_S8_UINT;
	}

	private static int selectDepthFormat(final MemoryStack stack, final VkPhysicalDevice device, final int... formats) {
		final VkFormatProperties properties = VkFormatProperties.malloc(stack);
		for (final int format : formats) {
			VK10.vkGetPhysicalDeviceFormatProperties(device, format, properties);
			if ((properties.optimalTilingFeatures() & VK10.VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT) != 0) {
				return format;
			}
		}
		throw new IllegalStateException("The Vulkan device supports none of the depth formats " + Arrays.toString(formats) + ", use a device that supports one of them");
	}

}