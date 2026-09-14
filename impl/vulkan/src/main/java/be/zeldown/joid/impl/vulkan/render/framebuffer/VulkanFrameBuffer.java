package be.zeldown.joid.impl.vulkan.render.framebuffer;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

import be.zeldown.joid.impl.vulkan.render.VulkanContext;
import be.zeldown.joid.impl.vulkan.render.VulkanRenderBridge;
import be.zeldown.joid.impl.vulkan.render.texture.VulkanTexture;
import be.zeldown.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class VulkanFrameBuffer implements IFrameBuffer {

	private final VulkanRenderBridge bridge;
	private final VulkanTexture      texture;
	private final long               framebuffer;

	public static @NonNull VulkanFrameBuffer create(final VulkanRenderBridge bridge, final int width, final int height) {
		final VulkanContext context = bridge.getContext();
		final VulkanTexture texture = new VulkanTexture(bridge).allocate(width, height);
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final LongBuffer framebuffer = stack.mallocLong(1);
			VulkanContext.check(VK10.vkCreateFramebuffer(context.getDevice(), VkFramebufferCreateInfo.calloc(stack).sType$Default().renderPass(context.getOffscreenRenderPass()).pAttachments(stack.longs(texture.getView())).width(width).height(height).layers(1), null, framebuffer), "vkCreateFramebuffer");
			return new VulkanFrameBuffer(bridge, texture, framebuffer.get(0));
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
		final VulkanContext context = this.bridge.getContext();
		this.bridge.dispose(() -> VK10.vkDestroyFramebuffer(context.getDevice(), framebuffer, null));
		this.texture.delete();
	}

}