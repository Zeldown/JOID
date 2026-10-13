package dev.joid.lib.ui.event;

import lombok.NonNull;

@FunctionalInterface
public interface EventListener<E extends Event> {

	public void apply(final @NonNull E event);

}