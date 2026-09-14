package be.zeldown.joid.impl.vulkan.render.texture;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferImageCopy;

import be.zeldown.joid.impl.vulkan.render.VulkanContext;
import be.zeldown.joid.impl.vulkan.render.VulkanRenderBridge;
import be.zeldown.joid.impl.vulkan.render.buffer.VulkanBuffer;
import be.zeldown.joid.lib.bridge.render.texture.ITexture;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class VulkanTexture implements ITexture {

	private final VulkanRenderBridge bridge;

	private long    image;
	private long    memory;
	private long    view;
	private int     width;
	private int     height;
	private boolean deleted;

	public VulkanTexture(final VulkanRenderBridge bridge) {
		this.bridge = bridge;
	}

	@Override
	public @NonNull VulkanTexture allocate(final int width, final int height) {
		if (this.image != VK10.VK_NULL_HANDLE && this.width == width && this.height == height) {
			return this;
		}

		this.release();
		final VulkanContext context = this.bridge.getContext();
		final long[] handles = context.createImage(width, height, VulkanContext.TEXTURE_FORMAT, VK10.VK_IMAGE_USAGE_SAMPLED_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
		this.image  = handles[0];
		this.memory = handles[1];
		this.view   = context.createImageView(this.image, VulkanContext.TEXTURE_FORMAT, VK10.VK_IMAGE_ASPECT_COLOR_BIT);
		this.width  = width;
		this.height = height;

		final long image = this.image;
		context.submit(buffer -> VulkanContext.transition(buffer, image, VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_UNDEFINED, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL));
		return this;
	}

	@Override
	public @NonNull VulkanTexture upload(final @NonNull int[] pixels, final int width, final int height) {
		final VulkanBuffer staging = this.bridge.getStagingBuffer((long) width * height * 4L);
		MemoryUtil.memIntBuffer(staging.getAddress(), width * height).put(pixels, 0, width * height);

		final long image = this.image;
		this.bridge.getContext().submit(buffer -> {
			try (MemoryStack stack = MemoryStack.stackPush()) {
				VulkanContext.transition(buffer, image, VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);

				final VkBufferImageCopy.Buffer copy = VkBufferImageCopy.calloc(1, stack).bufferOffset(0L);
				copy.imageSubresource().aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(0).baseArrayLayer(0).layerCount(1);
				copy.imageExtent().set(width, height, 1);
				VK10.vkCmdCopyBufferToImage(buffer, staging.getBuffer(), image, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, copy);

				VulkanContext.transition(buffer, image, VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
			}
		});
		return this;
	}

	@Override
	public void delete() {
		if (this.deleted) {
			return;
		}

		this.release();
		this.deleted = true;
	}

	private void release() {
		if (this.image == VK10.VK_NULL_HANDLE) {
			return;
		}

		final long image = this.image;
		final long memory = this.memory;
		final long view = this.view;
		final VulkanContext context = this.bridge.getContext();
		this.bridge.releaseHandle(view);
		this.bridge.dispose(() -> {
			VK10.vkDestroyImageView(context.getDevice(), view, null);
			VK10.vkDestroyImage(context.getDevice(), image, null);
			VK10.vkFreeMemory(context.getDevice(), memory, null);
		});

		this.image  = VK10.VK_NULL_HANDLE;
		this.memory = VK10.VK_NULL_HANDLE;
		this.view   = VK10.VK_NULL_HANDLE;
	}

}