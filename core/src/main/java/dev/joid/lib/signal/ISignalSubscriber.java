package dev.joid.lib.signal;

@FunctionalInterface
public interface ISignalSubscriber<T> {

	public boolean update(final T value);

}