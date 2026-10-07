package dev.joid.lib.utils.signal.replay;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SignalReplayUnknown {

	private final Object key;
	private final String description;

	public static SignalReplayUnknown create(final String description) {
		return new SignalReplayUnknown(null, description);
	}

	public static SignalReplayUnknown create(final String description, final Object key) {
		return new SignalReplayUnknown(key, description);
	}

}