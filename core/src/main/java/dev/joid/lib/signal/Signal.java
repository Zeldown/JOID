package dev.joid.lib.signal;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.signal.replay.SignalReplay;
import lombok.NonNull;

public class Signal<T> extends BaseSignal<T> {

	private final transient T defaultValue;

	private volatile T value;

	private transient T       sealedValue;
	private transient boolean nextSilent = false;

	public Signal() {
		this(null);
	}

	public Signal(final T value) {
		this.value = this.seal(value);
		this.sealedValue = this.value;
		this.defaultValue = this.value;
	}

	public static <T> @NonNull ComputedSignal<T> from(final T value) {
		final Supplier<T> replay = SignalReplay.replay(value);
		return replay != null ? new ComputedSignal<>(replay) : ComputedSignal.constant(value);
	}

	public static <T> @NonNull ComputedSignal<T> from(final @NonNull Supplier<? extends T> supplier) {
		return new ComputedSignal<>(supplier);
	}

	public static void batch(final @NonNull Runnable runnable) {
		final SignalContext context = SignalContext.current();
		context.open();
		try {
			runnable.run();
		} finally {
			context.close();
		}
	}

	public @NonNull Signal<T> reset() {
		this.set(this.defaultValue);
		return this;
	}

	public @NonNull Signal<T> set(final T value) {
		return this.write(() -> this.store(value));
	}

	@Override
	public @NonNull Signal<T> onChange(final @NonNull ISignalListener<? super T> listener) {
		super.onChange(listener);
		return this;
	}

	@Override
	public @NonNull Signal<T> removeOnChange(final @NonNull ISignalListener<? super T> listener) {
		super.removeOnChange(listener);
		return this;
	}

	public @NonNull Signal<T> silent() {
		this.nextSilent = true;
		return this;
	}

	public @NonNull Signal<T> fireChange() {
		return this.write(this::fire);
	}

	@Override
	public T peek() {
		final T current = this.value;
		return current == this.sealedValue ? current : this.reseal(current);
	}

	@Override
	protected final @NonNull Signal<T> write(final @NonNull Runnable task) {
		if (BridgeHandler.THREAD.get().isRenderThread()) {
			task.run();
			return this;
		}

		final boolean silent = this.nextSilent;
		this.nextSilent = false;
		super.write(() -> {
			final boolean previous = this.nextSilent;
			this.nextSilent = silent;
			try {
				task.run();
			} finally {
				this.nextSilent = previous;
			}
		});
		return this;
	}

	protected T seal(final T value) {
		return value;
	}

	protected final @NonNull Signal<T> transform(final @NonNull UnaryOperator<T> operator) {
		return this.write(() -> this.store(operator.apply(this.peek())));
	}

	private void store(final T value) {
		final T oldValue = this.value;
		this.value = this.seal(value);
		this.sealedValue = this.value;
		if (oldValue == null && this.value == null || oldValue != null && oldValue.equals(this.value)) {
			this.nextSilent = false;
			return;
		}

		this.fire();
	}

	private synchronized T reseal(final T current) {
		if (this.value == current) {
			this.value = this.seal(current);
			this.sealedValue = this.value;
		}
		return this.value;
	}

	private void fire() {
		final boolean silent = this.nextSilent;
		this.nextSilent = false;
		super.propagate(!silent);
	}

}