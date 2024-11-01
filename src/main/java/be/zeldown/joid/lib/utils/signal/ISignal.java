package be.zeldown.joid.lib.utils.signal;

import lombok.NonNull;

public interface ISignal<T> {

	T getOrDefault();

	void reset();
	void set(final T value);

	void subscribe(final @NonNull SignalSubscriber<@NonNull T> subscriber);
	void unsubscribe(final @NonNull SignalSubscriber<@NonNull T> subscriber);

	void publish();
	void silent();

	boolean isPresent();

}