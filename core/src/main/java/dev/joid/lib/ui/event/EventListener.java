package dev.joid.lib.ui.event;

import lombok.NonNull;

@FunctionalInterface
public interface EventListener<E extends Event> {

	public static <E extends Event> @NonNull EventListener<E> priority(final @NonNull EventPriority priority, final @NonNull EventListener<E> listener) {
		return new EventListener<E>() {

			@Override
			public void apply(final @NonNull E event) {
				listener.apply(event);
			}

			@Override
			public @NonNull EventPriority getPriority() {
				return priority;
			}

		};
	}

	public void apply(final @NonNull E event);

	public default @NonNull EventPriority getPriority() {
		return EventPriority.NORMAL;
	}

}