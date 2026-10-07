package dev.joid.lib.utils.signal;

@FunctionalInterface
public interface SignalSubscriber<T> {

	public boolean update(final T value);

}