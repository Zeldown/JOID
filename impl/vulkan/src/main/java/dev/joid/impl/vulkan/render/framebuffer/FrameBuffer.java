package dev.joid.impl.vulkan.render.framebuffer;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

import dev.joid.impl.vulkan.render.Context;
import dev.joid.impl.vulkan.render.RenderBridge;
import dev.joid.impl.vulkan.render.texture.Texture;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FrameBuffer implements IFrameBuffer {

	private final RenderBridge bridge;
	private final Texture      texture;
	private final long         framebuffer;
	private final long         depthImage;
	private final long         depthMemory;
	private final long         depthView;

	public static @NonNull FrameBuffer create(final RenderBridge bridge, final int width, final int height) {
		final Context context = bridge.getContext();
		final Texture texture = new Texture(bridge).allocate(width, height);
		final long[] depth = context.createImage(width, height, context.getDepthStencilFormat(), VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT);
		final long depthView = context.createImageView(depth[0], context.getDepthStencilFormat(), VK10.VK_IMAGE_ASPECT_DEPTH_BIT | VK10.VK_IMAGE_ASPECT_STENCIL_BIT);
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final LongBuffer framebuffer = stack.mallocLong(1);
			Context.check(VK10.vkCreateFramebuffer(context.getDevice(), VkFramebufferCreateInfo.calloc(stack).sType$Default().renderPass(context.getOffscreenRenderPass()).pAttachments(stack.longs(texture.getView(), depthView)).width(width).height(height).layers(1), null, framebuffer), "vkCreateFramebuffer");
			return new FrameBuffer(bridge, texture, framebuffer.get(0), depth[0], depth[1], depthView);
		}
	}

	@Override
	public int getWidth() {
		return this.texture.getWidth();
	}

	@Override
	public int getHeight() {
		return this.texture.getHeight();
	}

	@Override
	public void delete() {
		final long framebuffer = this.framebuffer;
		final long depthImage = this.depthImage;
		final long depthMemory = this.depthMemory;
		final long depthView = this.depthView;
		final Context context = this.bridge.getContext();
		this.bridge.dispose(() -> {
			VK10.vkDestroyFramebuffer(context.getDevice(), framebuffer, null);
			VK10.vkDestroyImageView(context.getDevice(), depthView, null);
			VK10.vkDestroyImage(context.getDevice(), depthImage, null);
			VK10.vkFreeMemory(context.getDevice(), depthMemory, null);
		});
		this.texture.delete();
	}

}