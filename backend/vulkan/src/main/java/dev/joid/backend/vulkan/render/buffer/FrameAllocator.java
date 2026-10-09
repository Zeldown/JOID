package dev.joid.backend.vulkan.render.buffer;

import java.util.function.Consumer;

import dev.joid.backend.vulkan.render.VulkanContext;
import lombok.Getter;

@Getter
public final class FrameAllocator {

	private final int                usage;
	private final long               alignment;
	private final VulkanContext      context;
	private final Consumer<Runnable> disposer;

	private long   offset;
	private Buffer buffer;

	public FrameAllocator(final VulkanContext context, final long capacity, final int usage, final long alignment, final Consumer<Runnable> disposer) {
		this.context   = context;
		this.usage     = usage;
		this.alignment = alignment;
		this.disposer  = disposer;
		this.buffer    = Buffer.create(context, capacity, usage);
	}

	public long allocate(final long size) {
		final long aligned = (this.offset + this.alignment - 1) / this.alignment * this.alignment;
		if (aligned + size > this.buffer.getSize()) {
			final Buffer previous = this.buffer;
			this.disposer.accept(previous::destroy);
			this.buffer = Buffer.create(this.context, Math.max(previous.getSize() * 2L, size * 2L), this.usage);
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