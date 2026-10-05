package be.zeldown.joid.lib.utils.signal;

import lombok.NonNull;

@FunctionalInterface
public interface SignalSubscriber<T> {

	public boolean update(final @NonNull T value);

}