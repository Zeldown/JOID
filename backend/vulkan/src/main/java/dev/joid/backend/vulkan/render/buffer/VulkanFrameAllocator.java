package dev.joid.backend.vulkan.render.buffer;

import java.util.function.Consumer;

import dev.joid.backend.vulkan.render.VulkanContext;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class VulkanFrameAllocator {

	private final int                usage;
	private final long               alignment;
	private final VulkanContext      context;
	private final Consumer<Runnable> disposer;

	private long         offset;
	private VulkanBuffer buffer;

	private VulkanFrameAllocator(final VulkanContext context, final long capacity, final int usage, final long alignment, final Consumer<Runnable> disposer) {
		this.context   = context;
		this.usage     = usage;
		this.alignment = alignment;
		this.disposer  = disposer;
		this.buffer    = VulkanBuffer.create(context, capacity, usage);
	}

	public static @NonNull VulkanFrameAllocator create(final VulkanContext context, final long capacity, final int usage, final long alignment, final Consumer<Runnable> disposer) {
		return new VulkanFrameAllocator(context, capacity, usage, alignment, disposer);
	}

	public void destroy() {
		this.buffer.destroy();
	}

	public long allocate(final long size) {
		final long aligned = (this.offset + this.alignment - 1) / this.alignment * this.alignment;
		if (aligned + size > this.buffer.getSize()) {
			final VulkanBuffer previous = this.buffer;
			this.disposer.accept(previous::destroy);
			this.buffer = VulkanBuffer.create(this.context, Math.max(previous.getSize() * 2L, size * 2L), this.usage);
			this.offset = size;
			return 0L;
		}

		this.offset = aligned + size;
		return aligned;
	}

	public void reset() {
		this.offset = 0L;
	}

}