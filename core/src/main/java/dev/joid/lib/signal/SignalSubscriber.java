package dev.joid.lib.signal;

@FunctionalInterface
public interface SignalSubscriber<T> {

	public boolean update(final T value);

}