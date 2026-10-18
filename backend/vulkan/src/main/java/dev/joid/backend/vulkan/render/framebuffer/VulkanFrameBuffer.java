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
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class VulkanFrameBuffer extends FrameBufferHandle<VulkanTexture> {

	private final long               depthView;
	private final long               depthImage;
	private final long               framebuffer;
	private final long               depthMemory;
	private final VulkanRenderBridge bridge;

	private VulkanFrameBuffer(final VulkanRenderBridge bridge, final VulkanTexture texture, final long framebuffer, final long depthImage, final long depthMemory, final long depthView) {
		super(texture);
		this.bridge      = bridge;
		this.framebuffer = framebuffer;
		this.depthImage  = depthImage;
		this.depthMemory = depthMemory;
		this.depthView   = depthView;
	}

	public static @NonNull VulkanFrameBuffer create(final VulkanRenderBridge bridge, final int width, final int height) {
		final VulkanContext context = bridge.getContext();
		final VulkanTexture texture = VulkanTexture.create(bridge);
		texture.allocate(width, height);
		final long[] depth = context.createImage(width, height, context.getDepthFormat(), VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT, VK10.VK_IMAGE_ASPECT_DEPTH_BIT, 1);
		bridge.record(buffer -> {
			try (MemoryStack stack = MemoryStack.stackPush()) {
				VulkanContext.transition(buffer, depth[0], VK10.VK_IMAGE_ASPECT_DEPTH_BIT, VK10.VK_IMAGE_LAYOUT_UNDEFINED, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
				final VkImageSubresourceRange range = VkImageSubresourceRange.calloc(stack).aspectMask(VK10.VK_IMAGE_ASPECT_DEPTH_BIT).baseMipLevel(0).levelCount(1).baseArrayLayer(0).layerCount(1);
				VK10.vkCmdClearDepthStencilImage(buffer, depth[0], VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VkClearDepthStencilValue.calloc(stack).depth(1F), range);
				VulkanContext.transition(buffer, depth[0], VK10.VK_IMAGE_ASPECT_DEPTH_BIT, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
			}
		});
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final LongBuffer framebuffer = stack.mallocLong(1);
			VulkanContext.check(VK10.vkCreateFramebuffer(context.getDevice(), VkFramebufferCreateInfo.calloc(stack).sType$Default().renderPass(context.getOffscreenRenderPass()).pAttachments(stack.longs(texture.getView(), depth[2])).width(width).height(height).layers(1), null, framebuffer), "vkCreateFramebuffer");
			return new VulkanFrameBuffer(bridge, texture, framebuffer.get(0), depth[0], depth[1], depth[2]);
		}
	}

	@Override
	protected void deleteHandle() {
		final long framebuffer = this.framebuffer;
		final long depthImage = this.depthImage;
		final long depthMemory = this.depthMemory;
		final long depthView = this.depthView;
		final VulkanContext context = this.bridge.getContext();
		this.bridge.dispose(() -> {
			VK10.vkDestroyFramebuffer(context.getDevice(), framebuffer, null);
			VK10.vkDestroyImageView(context.getDevice(), depthView, null);
			VK10.vkDestroyImage(context.getDevice(), depthImage, null);
			VK10.vkFreeMemory(context.getDevice(), depthMemory, null);
		});
	}

}