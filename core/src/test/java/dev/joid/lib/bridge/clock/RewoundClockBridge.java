package dev.joid.lib.bridge.clock;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class RewoundClockBridge implements IClockBridge {

	private final long         milliseconds;
	private final IClockBridge clock;

	@Override
	public long nanoTime() {
		return this.clock.nanoTime();
	}

	@Override
	public long currentTimeMillis() {
		return this.clock.currentTimeMillis() - this.milliseconds;
	}

}