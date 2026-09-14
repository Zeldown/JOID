package be.zeldown.joid.lib.bridge.clock;

import be.zeldown.joid.lib.bridge.IBridge;

public interface IClockBridge extends IBridge {

	public long currentTimeMillis();
	public long nanoTime();

}