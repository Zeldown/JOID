package dev.joid.lib.utils.signal.replay;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReplayUnknown {

	private final String description;
	private final Object key;

	public static ReplayUnknown create(final String description) {
		return new ReplayUnknown(description, null);
	}

	public static ReplayUnknown create(final String description, final Object key) {
		return new ReplayUnknown(description, key);
	}

}