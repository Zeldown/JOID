package be.zeldown.joid.lib.utils.signal;

import lombok.NonNull;

public interface ISignal<T> {

	ISignal<T> reset();
	ISignal<T> set(final T value);

	ISignal<T> subscribe(final @NonNull SignalSubscriber<@NonNull T> subscriber);
	ISignal<T> unsubscribe(final @NonNull SignalSubscriber<@NonNull T> subscriber);

	ISignal<T> publish();
	ISignal<T> silent();

	T getOrDefault();
	boolean isPresent();

}