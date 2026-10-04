package be.zeldown.joid.impl.vulkan.render.texture;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkImageBlit;

import be.zeldown.joid.impl.vulkan.render.Context;
import be.zeldown.joid.impl.vulkan.render.RenderBridge;
import be.zeldown.joid.impl.vulkan.render.buffer.Buffer;
import be.zeldown.joid.lib.bridge.render.texture.ITexture;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Texture implements ITexture {

	private final RenderBridge bridge;

	private long    image;
	private long    memory;
	private long    view;
	private int     width;
	private int     height;
	private int     levels;
	private boolean deleted;
	private boolean mipmapped;

	public Texture(final RenderBridge bridge) {
		this.bridge = bridge;
		this.levels = 1;
	}

	@Override
	public @NonNull Texture mipmap(final boolean mipmap) {
		if (this.mipmapped == mipmap) {
			return this;
		}

		this.mipmapped = mipmap;
		if (this.image != VK10.VK_NULL_HANDLE && this.levels != Texture.levels(mipmap, this.width, this.height)) {
			this.release();
		}

		return this;
	}

	@Override
	public @NonNull Texture allocate(final int width, final int height) {
		final int tempLevels = Texture.levels(this.mipmapped, width, height);
		if (this.image != VK10.VK_NULL_HANDLE && this.width == width && this.height == height && this.levels == tempLevels) {
			return this;
		}

		this.release();
		final Context context = this.bridge.getContext();
		final long[] handles = context.createImage(width, height, Context.TEXTURE_FORMAT, VK10.VK_IMAGE_USAGE_SAMPLED_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT, tempLevels);
		this.image  = handles[0];
		this.memory = handles[1];
		this.view   = context.createImageView(this.image, Context.TEXTURE_FORMAT, VK10.VK_IMAGE_ASPECT_COLOR_BIT, tempLevels);
		this.width  = width;
		this.height = height;
		this.levels = tempLevels;

		final long image = this.image;
		context.submit(buffer -> Context.transition(buffer, image, VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_UNDEFINED, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, 0, tempLevels));
		return this;
	}

	@Override
	public @NonNull Texture upload(final @NonNull int[] pixels, final int width, final int height) {
		final Buffer staging = this.bridge.getStagingBuffer((long) width * height * 4L);
		MemoryUtil.memIntBuffer(staging.getAddress(), width * height).put(pixels, 0, width * height);

		final long image = this.image;
		final int tempLevels = this.levels;
		this.bridge.getContext().submit(buffer -> {
			try (MemoryStack stack = MemoryStack.stackPush()) {
				Context.transition(buffer, image, VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, 0, tempLevels);

				final VkBufferImageCopy.Buffer copy = VkBufferImageCopy.calloc(1, stack).bufferOffset(0L);
				copy.imageSubresource().aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(0).baseArrayLayer(0).layerCount(1);
				copy.imageExtent().set(width, height, 1);
				VK10.vkCmdCopyBufferToImage(buffer, staging.getBuffer(), image, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, copy);

				int levelWidth = width;
				int levelHeight = height;
				for (int level = 1; level < tempLevels; level++) {
					final int nextWidth = Math.max(1, levelWidth / 2);
					final int nextHeight = Math.max(1, levelHeight / 2);
					Context.transition(buffer, image, VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, level - 1, 1);

					final VkImageBlit.Buffer blit = VkImageBlit.calloc(1, stack);
					blit.srcSubresource().aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(level - 1).baseArrayLayer(0).layerCount(1);
					blit.srcOffsets(1).set(levelWidth, levelHeight, 1);
					blit.dstSubresource().aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(level).baseArrayLayer(0).layerCount(1);
					blit.dstOffsets(1).set(nextWidth, nextHeight, 1);
					VK10.vkCmdBlitImage(buffer, image, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, image, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, blit, VK10.VK_FILTER_LINEAR);

					Context.transition(buffer, image, VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, level - 1, 1);
					levelWidth = nextWidth;
					levelHeight = nextHeight;
				}

				Context.transition(buffer, image, VK10.VK_IMAGE_ASPECT_COLOR_BIT, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, tempLevels - 1, 1);
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
		final Context context = this.bridge.getContext();
		this.bridge.releaseHandle(view);
		this.bridge.dispose(() -> {
			VK10.vkDestroyImageView(context.getDevice(), view, null);
			VK10.vkDestroyImage(context.getDevice(), image, null);
			VK10.vkFreeMemory(context.getDevice(), memory, null);
		});

		this.image  = VK10.VK_NULL_HANDLE;
		this.memory = VK10.VK_NULL_HANDLE;
		this.view   = VK10.VK_NULL_HANDLE;
		this.levels = 1;
	}

	private static int levels(final boolean mipmapped, final int width, final int height) {
		if (!mipmapped) {
			return 1;
		}

		return 32 - Integer.numberOfLeadingZeros(Math.max(1, Math.max(width, height)));
	}

}