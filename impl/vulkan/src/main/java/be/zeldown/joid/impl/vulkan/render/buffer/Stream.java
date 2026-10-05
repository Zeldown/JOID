package be.zeldown.joid.impl.vulkan.render.buffer;

import java.util.function.Consumer;

import be.zeldown.joid.impl.vulkan.render.Context;
import lombok.Getter;

@Getter
public final class Stream {

	private final int                usage;
	private final long               alignment;
	private final Context            context;
	private final Consumer<Runnable> disposer;

	private long   offset;
	private Buffer buffer;

	public Stream(final Context context, final long capacity, final int usage, final long alignment, final Consumer<Runnable> disposer) {
		this.context   = context;
		this.usage     = usage;
		this.alignment = alignment;
		this.disposer  = disposer;
		this.buffer    = Buffer.create(context, capacity, usage);
	}

	public void reset() {
		this.offset = 0L;
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

}