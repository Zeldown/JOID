package dev.joid.lib.bridge.clock;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@AllArgsConstructor
public final class ManualClockBridge implements IClockBridge {

	@Setter
	@Accessors(fluent = true, chain = true)
	private long time;

	public void advance(final long milliseconds) {
		this.time += milliseconds;
	}

	@Override
	public long nanoTime() {
		return this.time * 1_000_000L;
	}

	@Override
	public long currentTimeMillis() {
		return this.time;
	}

}