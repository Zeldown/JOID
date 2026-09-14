package be.zeldown.joid.lib.bridge.clock;

public final class SystemClockBridge implements IClockBridge {

	@Override
	public long nanoTime() {
		return System.nanoTime();
	}

	@Override
	public long currentTimeMillis() {
		return System.currentTimeMillis();
	}

}