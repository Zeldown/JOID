package com.example.backend;

import com.example.backend.engine.Engine;

import dev.joid.lib.bridge.clock.IClockBridge;

// #region clock
public final class EngineClockBridge implements IClockBridge {

	private final Engine engine;

	public EngineClockBridge(final Engine engine) {
		this.engine = engine;
	}

	@Override
	public long nanoTime() {
		return this.engine.getGameNanos();
	}

	@Override
	public long currentTimeMillis() {
		return this.engine.getGameNanos() / 1_000_000L;
	}

}
// #endregion