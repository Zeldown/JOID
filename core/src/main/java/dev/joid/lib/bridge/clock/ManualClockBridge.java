package dev.joid.lib.bridge.clock;

import dev.joid.lib.bridge.BridgeHandler;
import lombok.Setter;
import lombok.experimental.Accessors;

public final class ManualClockBridge implements IClockBridge {

	private final long origin;

	@Setter
	@Accessors(fluent = true, chain = true)
	private long time;

	public ManualClockBridge(final long time) {
		this.origin = BridgeHandler.CLOCK.get().nanoTime() - time * 1_000_000L;
		this.time   = time;
	}

	public void advance(final long milliseconds) {
		this.time += milliseconds;
	}

	@Override
	public long nanoTime() {
		return this.origin + this.time * 1_000_000L;
	}

	@Override
	public long currentTimeMillis() {
		return this.time;
	}

}