package dev.joid.lib.bridge.clock;

import dev.joid.lib.bridge.IBridge;

public interface IClockBridge extends IBridge {

	public long nanoTime();

	public long currentTimeMillis();

}