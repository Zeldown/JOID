package dev.joid.backend.vulkan.render.framebuffer;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearDepthStencilValue;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkImageSubresourceRange;

import dev.joid.backend.vulkan.render.VulkanContext;
import dev.joid.backend.vulkan.render.VulkanRenderBridge;
import dev.joid.backend.vulkan.render.texture.VulkanTexture;
import dev.joid.lib.bridge.render.framebuffer.FrameBufferHandle;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public final class VulkanFrameBuffer extends FrameBufferHandle<VulkanTexture> {

	private final long               framebuffer;
	private final VulkanRenderBridge bridge;
	@Getter(AccessLevel.NONE) private final long[] depth;

	public VulkanFrameBuffer(final VulkanRenderBridge bridge, final int width, final int height) {
		super(VulkanTexture.create(bridge));
		final VulkanContext context = bridge.getContext();
		super.getTexture().allocate(width, height);
		final long[] depth = context.createImage(width, height, context.getDepthFormat(), VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT, VK10.VK_IMAGE_ASPECT_DEPTH_BIT, 1);
		bridge.record(buffer -> {
			try (MemoryStack stack = MemoryStack.stackPush()) {
				VulkanContext.transition(buffer, depth[0], VK10.VK_IMAGE_ASPECT_DEPTH_BIT, VK10.VK_IMAGE_LAYOUT_UNDEFINED, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
				final VkImageSubresourceRange range = VkImageSubresourceRange.calloc(stack).aspectMask(VK10.VK_IMAGE_ASPECT_DEPTH_BIT).baseMipLevel(0).levelCount(1).baseArrayLayer(0).layerCount(1);
				VK10.vkCmdClearDepthStencilImage(buffer, depth[0], VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VkClearDepthStencilValue.calloc(stack).depth(1F), range);
				VulkanContext.transition(buffer, depth[0], VK10.VK_IMAGE_ASPECT_DEPTH_BIT, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
			}
		});
		this.bridge = bridge;
		this.depth  = depth;
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final LongBuffer framebuffer = stack.mallocLong(1);
			VulkanContext.check(VK10.vkCreateFramebuffer(context.getDevice(), VkFramebufferCreateInfo.calloc(stack).sType$Default().renderPass(context.getOffscreenRenderPass()).pAttachments(stack.longs(super.getTexture().getView(), depth[2])).width(width).height(height).layers(1), null, framebuffer), "vkCreateFramebuffer");
			this.framebuffer = framebuffer.get(0);
		}
	}

	@Override
	protected void deleteHandle() {
		final long framebuffer = this.framebuffer;
		final long[] depth = this.depth;
		final VulkanContext context = this.bridge.getContext();
		this.bridge.dispose(() -> {
			VK10.vkDestroyFramebuffer(context.getDevice(), framebuffer, null);
			context.destroyImage(depth);
		});
	}

}