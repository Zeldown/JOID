package dev.joid.lib.signal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

import dev.joid.internal.JOIDLog;
import dev.joid.lib.signal.replay.SignalReplay;
import dev.joid.lib.utils.thread.ThreadUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

public class Signal<T> implements ISignal<T> {

	private final transient Set<@NonNull ISignalSubscriber<T>> eventSet;
	private final transient List<@NonNull ComputedSignal<?>>   observerList;

	private final transient T defaultValue;

	private volatile T value;

	private transient boolean nextSilent = false;

	@Getter(AccessLevel.PROTECTED)
	private transient int version;

	protected Signal() {
		this(null);
	}

	protected Signal(final T value) {
		this.value = value;
		this.defaultValue = value;
		this.eventSet = new HashSet<>();
		this.observerList = new CopyOnWriteArrayList<>();
	}

	public static <T> @NonNull Signal<T> of(final T value) {
		return new Signal<>(value);
	}

	public static <T> @NonNull Signal<T> of(final @NonNull CompletionStage<T> future) {
		final Signal<T> instance = new Signal<>();
		future.whenComplete((value, exception) -> {
			if (exception == null) {
				ThreadUtils.runOnRenderThread(() -> instance.set(value));
				return;
			}

			final Throwable cause = exception instanceof CompletionException && exception.getCause() != null ? exception.getCause() : exception;
			JOIDLog.warn("The future given to Signal.of(...) failed (" + cause + "), the signal stays empty: handle the failure on the future, with exceptionally(...) for example");
		});
		return instance;
	}

	public static <T> @NonNull ComputedSignal<T> from(final T value) {
		final Supplier<T> replay = SignalReplay.replay(value);
		return replay != null ? new ComputedSignal<>(replay) : ComputedSignal.constant(value);
	}

	public static <T> @NonNull ComputedSignal<T> from(final @NonNull Supplier<T> supplier) {
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

	@Override
	public @NonNull Signal<T> reset() {
		this.set(this.defaultValue);
		return this;
	}

	@Override
	public @NonNull Signal<T> set(final T value) {
		final T oldValue = this.value;
		this.value = value;
		if (oldValue == null && this.value == null || oldValue != null && oldValue.equals(this.value)) {
			this.nextSilent = false;
			return this;
		}

		return this.publish();
	}

	@Override
	public @NonNull Signal<T> subscribe(final @NonNull ISignalSubscriber<T> subscriber) {
		this.eventSet.add(subscriber);
		return this;
	}

	@Override
	public @NonNull Signal<T> unsubscribe(final @NonNull ISignalSubscriber<T> subscriber) {
		this.eventSet.remove(subscriber);
		return this;
	}

	@Override
	public @NonNull Signal<T> publish() {
		final SignalContext context = SignalContext.current();
		this.version++;
		SignalContext.nextEpoch();
		context.open();
		try {
			if (this.nextSilent) {
				this.nextSilent = false;
			} else if (!this.eventSet.isEmpty()) {
				context.schedule(this);
			}
			this.invalidate(context);
		} finally {
			context.close();
		}
		return this;
	}

	@Override
	public <R> @NonNull ComputedSignal<R> map(final @NonNull Function<T, R> function) {
		return Signal.from(() -> function.apply(this.get()));
	}

	@Override
	public T get() {
		final T current = this.peek();
		SignalContext.current().read(this);
		return current;
	}

	@Override
	public T peek() {
		return this.value;
	}

	protected @NonNull Set<@NonNull ISignalSubscriber<T>> getEventSet() {
		return this.eventSet;
	}

	@Override
	public @NonNull Signal<T> silent() {
		this.nextSilent = true;
		return this;
	}

	protected void refresh() {}

	protected void dispatch() {
		this.emit(this.peek());
	}

	protected boolean isObserved() {
		return !this.observerList.isEmpty() || !this.eventSet.isEmpty();
	}

	protected synchronized void addObserver(final ComputedSignal<?> observer) {
		for (final ComputedSignal<?> current : this.observerList) {
			if (current == observer) {
				return;
			}
		}

		this.observerList.add(observer);
	}

	protected synchronized void removeObserver(final ComputedSignal<?> observer) {
		for (int index = 0; index < this.observerList.size(); index++) {
			if (this.observerList.get(index) == observer) {
				this.observerList.remove(index);
				return;
			}
		}
	}

	protected final void invalidate(final SignalContext context) {
		for (final ComputedSignal<?> observer : this.observerList) {
			observer.markStale(context);
		}
	}

	protected final void nextVersion() {
		this.version++;
	}

	protected final void assign(final T value) {
		final boolean silent = this.nextSilent;
		this.nextSilent = true;
		this.set(value);
		this.nextSilent = silent;
	}

	protected final void publishIf(final boolean changed) {
		if (changed) {
			this.publish();
		} else {
			this.nextSilent = false;
		}
	}

	protected final void emit(final T value) {
		if (!this.eventSet.isEmpty()) {
			ThreadUtils.runOnRenderThread(() -> this.notifySubscribers(value));
		}
	}

	private void notifySubscribers(final T value) {
		final Set<ISignalSubscriber<T>> outdatedSet = new HashSet<>();
		final Set<ISignalSubscriber<T>> copiedSet = new HashSet<>(this.eventSet);
		for (final ISignalSubscriber<T> subscriber : copiedSet) {
			if (!subscriber.update(value)) {
				outdatedSet.add(subscriber);
			}
		}

		for (final ISignalSubscriber<T> subscriber : outdatedSet) {
			this.unsubscribe(subscriber);
		}
	}

}