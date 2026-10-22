package dev.joid.lib.signal;

@FunctionalInterface
public interface SignalListener<T> {

	public void apply(final T value);

}