package dev.joid.lib.signal;

@FunctionalInterface
public interface SignalListener<T> {

	public void onChange(final T value);

}