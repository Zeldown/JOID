package be.zeldown.joid.lib.bridge.clock;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ManualClockBridge implements IClockBridge {

	@Setter
	private long time;

	public static @NonNull ManualClockBridge create(final long time) {
		return new ManualClockBridge(time);
	}

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