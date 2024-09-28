package be.zeldown.joid.lib.utils.signal;

@FunctionalInterface
public interface SignalSubscriber<T> {

	boolean update(T value);

}