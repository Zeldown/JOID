package dev.joid.lib.utils.signal;

import java.util.function.Function;
import java.util.function.Supplier;

import lombok.NonNull;

public interface ISignal<T> extends Supplier<T> {

	public ISignal<T> reset();
	public ISignal<T> set(final T value);

	public ISignal<T> subscribe(final @NonNull SignalSubscriber<@NonNull T> subscriber);
	public ISignal<T> unsubscribe(final @NonNull SignalSubscriber<@NonNull T> subscriber);

	public ISignal<T> silent();
	public ISignal<T> publish();

	public <R> ComputedSignal<R> map(final @NonNull Function<T, R> function);

	public T peek();
	public T getOrDefault();
	public boolean isPresent();

}