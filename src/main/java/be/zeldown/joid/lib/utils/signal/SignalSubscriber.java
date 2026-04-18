package be.zeldown.joid.lib.utils.signal;

import lombok.NonNull;

@FunctionalInterface
public interface SignalSubscriber<T> {

	boolean update(final @NonNull T value);

}