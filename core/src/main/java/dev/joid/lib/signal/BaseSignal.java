package dev.joid.lib.signal;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.utils.thread.ThreadUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

public abstract class BaseSignal<T> implements ISignal<T> {

	private final transient Set<@NonNull ISignalListener<? super T>> listenerSet;
	private final transient List<@NonNull ComputedSignal<?>>          observerList;

	@Getter(AccessLevel.PROTECTED)
	private transient int version;

	protected BaseSignal() {
		this.listenerSet = new LinkedHashSet<>();
		this.observerList = new CopyOnWriteArrayList<>();
	}

	@Override
	public @NonNull BaseSignal<T> onChange(final @NonNull ISignalListener<? super T> listener) {
		synchronized (this.listenerSet) {
			this.listenerSet.add(listener);
		}
		return this;
	}

	@Override
	public @NonNull BaseSignal<T> removeOnChange(final @NonNull ISignalListener<? super T> listener) {
		synchronized (this.listenerSet) {
			this.listenerSet.remove(listener);
		}
		return this;
	}

	@Override
	public <R> @NonNull ComputedSignal<R> map(final @NonNull Function<? super T, ? extends R> function) {
		return Signal.from(() -> function.apply(this.get()));
	}

	@Override
	public T get() {
		final T current = this.peek();
		SignalContext.current().read(this);
		return current;
	}

	protected void refresh() {}

	protected void dispatch() {
		this.emit(this.peek());
	}

	protected boolean isObserved() {
		return !this.observerList.isEmpty() || this.hasListeners();
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

	protected @NonNull BaseSignal<T> write(final @NonNull Runnable task) {
		if (BridgeHandler.THREAD.get().isRenderThread()) {
			task.run();
		} else {
			SignalContext.current().defer(task);
		}
		return this;
	}

	protected final boolean hasListeners() {
		synchronized (this.listenerSet) {
			return !this.listenerSet.isEmpty();
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

	protected final void propagate(final boolean notify) {
		final SignalContext context = SignalContext.current();
		this.version++;
		SignalContext.nextEpoch();
		context.open();
		try {
			if (notify && this.hasListeners()) {
				context.schedule(this);
			}
			this.invalidate(context);
		} finally {
			context.close();
		}
	}

	protected final void emit(final T value) {
		if (this.hasListeners()) {
			ThreadUtils.runOnRenderThread(() -> this.notifyListeners(value));
		}
	}

	private void notifyListeners(final T value) {
		final List<ISignalListener<? super T>> listenerList;
		synchronized (this.listenerSet) {
			listenerList = new ArrayList<>(this.listenerSet);
		}

		RuntimeException failure = null;
		for (final ISignalListener<? super T> listener : listenerList) {
			try {
				listener.onChange(value);
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

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{" + this.peek() + "}";
	}

}