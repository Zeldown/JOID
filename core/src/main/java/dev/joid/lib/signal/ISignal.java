package dev.joid.lib.signal;

import java.util.function.Function;
import java.util.function.Supplier;

import lombok.NonNull;

public interface ISignal<T> extends Supplier<T> {

	public ISignal<T> onChange(final @NonNull ISignalListener<? super T> listener);
	public ISignal<T> removeOnChange(final @NonNull ISignalListener<? super T> listener);

	public <R> ComputedSignal<R> map(final @NonNull Function<? super T, ? extends R> function);

	public T peek();

}