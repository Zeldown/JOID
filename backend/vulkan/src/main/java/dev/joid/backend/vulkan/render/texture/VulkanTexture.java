package dev.joid.backend.vulkan.render.texture;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkImageBlit;
import org.lwjgl.vulkan.VkImageCopy;

import dev.joid.backend.vulkan.render.VulkanContext;
import dev.joid.backend.vulkan.render.VulkanRenderBridge;
import dev.joid.backend.vulkan.render.buffer.Buffer;
import dev.joid.lib.bridge.render.texture.MipmapChain;
import dev.joid.lib.bridge.render.texture.Texture;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class VulkanTexture extends Texture {

	private final VulkanRenderBridge bridge;

	private long view;
	private long image;
	private long memory;

	public VulkanTexture(final @NonNull VulkanRenderBridge bridge) {
		this.bridge = bridge;
	}

	@Override
	protected void onAllocate(final @NonNull MipmapChain chain) {
		this.release();
		final VulkanContext context = this.bridge.getContext();
		final long[] handles = context.createImage(chain.getWidth(), chain.getHeight(), VulkanContext.TEXTURE_FORMAT, VK10.VK_IMAGE_USAGE_SAMPLED_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT, chain.getLevels());
		this.image  = handles[0];
		this.memory = handles[1];
		this.view   = context.createImageView(this.image, VulkanContext.TEXTURE_FORMAT, VK10.VK_IMAGE_ASPECT_COLOR_BIT, chain.getLevels());

		final long image = this.image;
		context.submit(buffer -> VulkanContext.transition(buffer, image, VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_UNDEFINED, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, 0, chain.getLevels()));
	}

	@Override
	protected void onUpload(final @NonNull int[] pixels, final @NonNull MipmapChain chain) {
		final int width = chain.getWidth();
		final int height = chain.getHeight();
		final Buffer staging = this.bridge.getStagingBuffer((long) width * height * 4L);
		MemoryUtil.memIntBuffer(staging.getAddress(), width * height).put(pixels, 0, width * height);

		final long image = this.image;
		final int levels = super.getLevels();
		this.bridge.getContext().submit(buffer -> {
			try (MemoryStack stack = MemoryStack.stackPush()) {
				VulkanContext.transition(buffer, image, VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, 0, levels);

				final VkBufferImageCopy.Buffer copy = VkBufferImageCopy.calloc(1, stack).bufferOffset(0L);
				copy.imageSubresource().aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(0).baseArrayLayer(0).layerCount(1);
				copy.imageExtent().set(width, height, 1);
				VK10.vkCmdCopyBufferToImage(buffer, staging.getBuffer(), image, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, copy);
				VulkanTexture.blitLevels(buffer, stack, image, levels, chain);
			}
		});
	}

	@Override
	protected void onGenerateLevels(final @NonNull MipmapChain chain, final int allocatedLevels) {
		if (allocatedLevels == chain.getLevels()) {
			final long image = this.image;
			this.bridge.getContext().submit(buffer -> {
				try (MemoryStack stack = MemoryStack.stackPush()) {
					VulkanContext.transition(buffer, image, VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, 0, chain.getLevels());
					VulkanTexture.blitLevels(buffer, stack, image, chain.getLevels(), chain);
				}
			});
			return;
		}

		final VulkanContext context = this.bridge.getContext();
		final long[] handles = context.createImage(chain.getWidth(), chain.getHeight(), VulkanContext.TEXTURE_FORMAT, VK10.VK_IMAGE_USAGE_SAMPLED_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT, chain.getLevels());
		final long view = context.createImageView(handles[0], VulkanContext.TEXTURE_FORMAT, VK10.VK_IMAGE_ASPECT_COLOR_BIT, chain.getLevels());
		final long source = this.image;
		final long target = handles[0];
		context.submit(buffer -> {
			try (MemoryStack stack = MemoryStack.stackPush()) {
				VulkanContext.transition(buffer, source, VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, 0, 1);
				VulkanContext.transition(buffer, target, VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_UNDEFINED, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, 0, chain.getLevels());

				final VkImageCopy.Buffer copy = VkImageCopy.calloc(1, stack);
				copy.srcSubresource().aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(0).baseArrayLayer(0).layerCount(1);
				copy.dstSubresource().aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(0).baseArrayLayer(0).layerCount(1);
				copy.extent().set(chain.getWidth(), chain.getHeight(), 1);
				VK10.vkCmdCopyImage(buffer, source, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, target, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, copy);

				VulkanContext.transition(buffer, source, VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, 0, 1);
				VulkanTexture.blitLevels(buffer, stack, target, chain.getLevels(), chain);
			}
		});

		this.release();
		this.image  = target;
		this.memory = handles[1];
		this.view   = view;
	}

	@Override
	protected void onDelete() {
		this.release();
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

	private static void blitLevels(final VkCommandBuffer buffer, final MemoryStack stack, final long image, final int levels, final MipmapChain chain) {
		chain.forEachStep((level, sourceWidth, sourceHeight, targetWidth, targetHeight) -> {
			VulkanContext.transition(buffer, image, VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, level - 1, 1);

			final VkImageBlit.Buffer blit = VkImageBlit.calloc(1, stack);
			blit.srcSubresource().aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(level - 1).baseArrayLayer(0).layerCount(1);
			blit.srcOffsets(1).set(sourceWidth, sourceHeight, 1);
			blit.dstSubresource().aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(level).baseArrayLayer(0).layerCount(1);
			blit.dstOffsets(1).set(targetWidth, targetHeight, 1);
			VK10.vkCmdBlitImage(buffer, image, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, image, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, blit, VK10.VK_FILTER_LINEAR);

			VulkanContext.transition(buffer, image, VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, level - 1, 1);
		});

		VulkanContext.transition(buffer, image, VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, chain.getLevels() - 1, levels - chain.getLevels() + 1);
	}

}