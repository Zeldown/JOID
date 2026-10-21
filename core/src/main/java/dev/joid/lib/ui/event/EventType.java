package dev.joid.lib.ui.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventType<E extends Event> {

	private final String name;

	public static <E extends Event> @NonNull EventType<E> create(final @NonNull String name) {
		return new EventType<>(name);
	}

}