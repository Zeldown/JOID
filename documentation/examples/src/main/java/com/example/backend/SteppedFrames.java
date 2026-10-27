package com.example.backend;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.clock.ManualClockBridge;

public final class SteppedFrames {

	private SteppedFrames() {}

	// #region clock
	public static void run(final int frames, final Runnable frame) {
		final ManualClockBridge clock = new ManualClockBridge(0L);
		BridgeHandler.CLOCK.register(clock);
		try {
			for (int index = 0; index < frames; index++) {
				clock.advance(16L);
				frame.run();
			}
		} finally {
			BridgeHandler.CLOCK.unregister(clock);
		}
	}
	// #endregion

}