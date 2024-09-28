package be.zeldown.joid.lib.utils.signal;

public interface ISignal<T> {

	T getOrDefault();

	void reset();

	void set(T value);

	void subscribe(SignalSubscriber<T> subscriber);

	void unsubscribe(SignalSubscriber<T> subscriber);

	void publish();

	void silent();

	boolean isPresent();

}