package dev.joid.lib.utils.signal.replay;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReplayUnknown {

	private final Object key;
	private final String description;

	public static ReplayUnknown create(final String description) {
		return new ReplayUnknown(null, description);
	}

	public static ReplayUnknown create(final String description, final Object key) {
		return new ReplayUnknown(key, description);
	}

}