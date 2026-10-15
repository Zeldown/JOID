package dev.joid.lib.bridge.clock;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SystemClockBridge implements IClockBridge {

	public static @NonNull SystemClockBridge create() {
		return new SystemClockBridge();
	}

	@Override
	public long nanoTime() {
		return System.nanoTime();
	}

	@Override
	public long currentTimeMillis() {
		return System.currentTimeMillis();
	}

}