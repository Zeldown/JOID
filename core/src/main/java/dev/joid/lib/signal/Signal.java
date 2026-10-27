package dev.joid.lib.signal;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import dev.joid.internal.JOIDLog;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.signal.replay.SignalReplay;
import lombok.NonNull;

public class Signal<T> implements Supplier<T> {

	private final transient Set<@NonNull SignalListener<? super T>>          listenerSet;
	private final transient CopyOnWriteArrayList<@NonNull ComputedSignal<?>> observerList;

	private final transient T defaultValue;

	private volatile T value;

	private transient int version;
	private transient T   sealedValue;

	public Signal() {
		this(null);
	}

	public Signal(final T value) {
		this.listenerSet = new CopyOnWriteArraySet<>();
		this.observerList = new CopyOnWriteArrayList<>();
		this.value = value;
		this.defaultValue = value;
	}

	public static <T> @NonNull ComputedSignal<T> from(final T value) {
		final Supplier<T> replay = SignalReplay.replay(value);
		return replay != null ? ComputedSignal.computed(replay) : ComputedSignal.constant(value);
	}

	public static <T> @NonNull ComputedSignal<T> from(final @NonNull Supplier<? extends T> supplier) {
		return ComputedSignal.computed(supplier);
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

	public static void silent(final @NonNull Runnable runnable) {
		final SignalContext context = SignalContext.current();
		final boolean silent = context.silent(true);
		try {
			runnable.run();
		} finally {
			context.silent(silent);
		}
	}

	@Override
	public T get() {
		final T current = this.peek();
		SignalContext.current().read(this);
		return current;
	}

	public @NonNull Signal<T> set(final T value) {
		if (!this.isWritable()) {
			throw new IllegalStateException(this.refuse("set to " + value));
		}

		return this.write(() -> this.store(value));
	}

	public @NonNull Signal<T> reset() {
		if (!this.isWritable()) {
			throw new IllegalStateException(this.refuse("reset"));
		}

		this.set(this.defaultValue);
		return this;
	}

	public @NonNull Signal<T> onChange(final @NonNull SignalListener<? super T> listener) {
		this.listenerSet.add(listener);
		return this;
	}

	public @NonNull Signal<T> removeOnChange(final @NonNull SignalListener<? super T> listener) {
		this.listenerSet.remove(listener);
		return this;
	}

	public final <S extends Signal<T>> @NonNull S silent(final Consumer<@NonNull S> action) {
		Signal.silent(() -> action.accept((S) this));
		return (S) this;
	}

	public @NonNull Signal<T> fireChange() {
		return this.write(this::fire);
	}

	public <R> @NonNull ComputedSignal<R> map(final @NonNull Function<? super T, ? extends R> function) {
		return Signal.from(() -> function.apply(this.get()));
	}

	public T peek() {
		final T current = this.value;
		return current == this.sealedValue ? current : this.reseal(current);
	}

	public boolean isPresent() {
		return this.get() != null;
	}

	public boolean isWritable() {
		return true;
	}

	protected @NonNull String refuse(final @NonNull String action) {
		return "The " + JOIDLog.nameOf(this.getClass()) + " is read-only and cannot be " + action;
	}

	protected T seal(final T value) {
		return value;
	}

	protected void refresh() {}

	protected void dispatch() {
		this.fireChange(this.peek());
	}

	protected final int getVersion() {
		return this.version;
	}

	protected boolean isObserved() {
		return !this.observerList.isEmpty() || this.hasListeners();
	}

	protected void addObserver(final ComputedSignal<?> observer) {
		this.observerList.addIfAbsent(observer);
	}

	protected void removeObserver(final ComputedSignal<?> observer) {
		this.observerList.remove(observer);
	}

	protected final @NonNull Signal<T> write(final @NonNull Runnable task) {
		if (BridgeHandler.THREAD.get().isRenderThread()) {
			task.run();
		} else {
			SignalContext.current().defer(task);
		}
		return this;
	}

	protected final <S extends Signal<T>> @NonNull S transform(final @NonNull UnaryOperator<T> operator) {
		return (S) this.write(() -> this.store(operator.apply(this.peek())));
	}

	protected final boolean hasListeners() {
		return !this.listenerSet.isEmpty();
	}

	protected final void copyListeners(final @NonNull Signal<T> signal) {
		signal.listenerSet.addAll(this.listenerSet);
	}

	protected final void invalidate(final SignalContext context) {
		for (final ComputedSignal<?> observer : this.observerList) {
			observer.markStale(context);
		}
	}

	protected final void nextVersion() {
		this.version++;
	}

	protected final void fireChange(final T value) {
		RuntimeException failure = null;
		for (final SignalListener<? super T> listener : this.listenerSet) {
			try {
				listener.apply(value);
			} catch (final RuntimeException exception) {
				if (failure == null) {
					failure = exception;
				} else {
					failure.addSuppressed(exception);
				}
			}
		}

		if (failure != null) {
			throw failure;
		}
	}

	protected final void fire() {
		final SignalContext context = SignalContext.current();
		this.version++;
		SignalContext.nextEpoch();
		context.open();
		try {
			if (!context.isSilent() && this.hasListeners()) {
				context.schedule(this);
			}
			this.invalidate(context);
		} finally {
			context.close();
		}
	}

	private void store(final T value) {
		final T oldValue;
		synchronized (this) {
			oldValue = this.value;
			this.value = this.seal(value);
			this.sealedValue = this.value;
		}

		if (Objects.equals(oldValue, this.value)) {
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

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{" + this.peek() + "}";
	}

}