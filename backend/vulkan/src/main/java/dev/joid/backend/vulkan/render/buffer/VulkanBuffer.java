package dev.joid.backend.vulkan.render.buffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;

import dev.joid.backend.vulkan.render.VulkanContext;
import lombok.Getter;

@Getter
public final class VulkanBuffer {

	private final long          size;
	private final long          buffer;
	private final long          memory;
	private final long          address;
	private final VulkanContext context;

	public VulkanBuffer(final VulkanContext context, final long size, final int usage) {
		final long[] handles = context.createBuffer(size, usage, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
		try (final MemoryStack stack = MemoryStack.stackPush()) {
			final PointerBuffer address = stack.mallocPointer(1);
			VulkanContext.check(VK10.vkMapMemory(context.getDevice(), handles[1], 0L, size, 0, address), "vkMapMemory");
			this.context = context;
			this.buffer  = handles[0];
			this.memory  = handles[1];
			this.size    = size;
			this.address = address.get(0);
		}
	}

	public void destroy() {
		VK10.vkUnmapMemory(this.context.getDevice(), this.memory);
		VK10.vkDestroyBuffer(this.context.getDevice(), this.buffer, null);
		VK10.vkFreeMemory(this.context.getDevice(), this.memory, null);
	}

}