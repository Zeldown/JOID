package dev.joid.lib.signal;

@FunctionalInterface
public interface ISignalListener<T> {

	public void onChange(final T value);

}