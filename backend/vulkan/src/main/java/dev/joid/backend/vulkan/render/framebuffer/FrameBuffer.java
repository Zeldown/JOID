package dev.joid.backend.vulkan.render.framebuffer;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

import dev.joid.backend.vulkan.render.Context;
import dev.joid.backend.vulkan.render.RenderBridge;
import dev.joid.backend.vulkan.render.texture.Texture;
import dev.joid.lib.bridge.render.framebuffer.FrameBufferHandle;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class FrameBuffer extends FrameBufferHandle<Texture> {

	private final RenderBridge bridge;
	private final long         framebuffer;
	private final long         depthImage;
	private final long         depthMemory;
	private final long         depthView;

	private FrameBuffer(final RenderBridge bridge, final Texture texture, final long framebuffer, final long depthImage, final long depthMemory, final long depthView) {
		super(texture);
		this.bridge      = bridge;
		this.framebuffer = framebuffer;
		this.depthImage  = depthImage;
		this.depthMemory = depthMemory;
		this.depthView   = depthView;
	}

	public static @NonNull FrameBuffer create(final RenderBridge bridge, final int width, final int height) {
		final Context context = bridge.getContext();
		final Texture texture = new Texture(bridge);
		texture.allocate(width, height);
		final long[] depth = context.createImage(width, height, context.getDepthStencilFormat(), VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT);
		final long depthView = context.createImageView(depth[0], context.getDepthStencilFormat(), VK10.VK_IMAGE_ASPECT_DEPTH_BIT | VK10.VK_IMAGE_ASPECT_STENCIL_BIT);
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final LongBuffer framebuffer = stack.mallocLong(1);
			Context.check(VK10.vkCreateFramebuffer(context.getDevice(), VkFramebufferCreateInfo.calloc(stack).sType$Default().renderPass(context.getOffscreenRenderPass()).pAttachments(stack.longs(texture.getView(), depthView)).width(width).height(height).layers(1), null, framebuffer), "vkCreateFramebuffer");
			return new FrameBuffer(bridge, texture, framebuffer.get(0), depth[0], depth[1], depthView);
		}
	}

	@Override
	protected void onDelete() {
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
	}

}