package be.zeldown.joid.lib.utils.signal;

import lombok.NonNull;

public interface ISignal<T> {

	public ISignal<T> reset();
	public ISignal<T> set(final T value);

	public ISignal<T> subscribe(final @NonNull SignalSubscriber<@NonNull T> subscriber);
	public ISignal<T> unsubscribe(final @NonNull SignalSubscriber<@NonNull T> subscriber);

	public ISignal<T> silent();
	public ISignal<T> publish();

	public T getOrDefault();
	public boolean isPresent();

}