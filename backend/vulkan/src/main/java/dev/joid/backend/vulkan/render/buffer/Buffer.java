package dev.joid.backend.vulkan.render.buffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;

import dev.joid.backend.vulkan.render.VulkanContext;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Buffer {

	private final VulkanContext context;
	private final long          buffer;
	private final long          memory;
	private final long          size;
	private final long          address;

	public static @NonNull Buffer create(final VulkanContext context, final long size, final int usage) {
		final long[] handles = context.createBuffer(size, usage, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final PointerBuffer address = stack.mallocPointer(1);
			VulkanContext.check(VK10.vkMapMemory(context.getDevice(), handles[1], 0L, size, 0, address), "vkMapMemory");
			return new Buffer(context, handles[0], handles[1], size, address.get(0));
		}
	}

	public void destroy() {
		VK10.vkUnmapMemory(this.context.getDevice(), this.memory);
		VK10.vkDestroyBuffer(this.context.getDevice(), this.buffer, null);
		VK10.vkFreeMemory(this.context.getDevice(), this.memory, null);
	}

}