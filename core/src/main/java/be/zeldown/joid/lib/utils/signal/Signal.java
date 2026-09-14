package be.zeldown.joid.lib.utils.signal;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import lombok.NonNull;

public class Signal<T> implements ISignal<T> {

	private final transient Set<@NonNull SignalSubscriber<@NonNull T>> eventSet;

	private volatile T defaultValue;
	private volatile T value;

	private transient boolean nextSilent = false;

	public Signal() {
		this(null);
	}

	public Signal(final T defaultValue) {
		this.defaultValue = defaultValue;
		this.eventSet = new HashSet<>();
	}

	public static <T> @NonNull Signal<T> of(final T defaultValue) {
		final Signal<T> instance = new Signal<>();
		instance.set(defaultValue);
		return instance;
	}

	public static <T> @NonNull Signal<T> of(final @NonNull CompletionStage<T> future) {
		final Signal<T> instance = new Signal<>();
		future.thenAccept(instance::set);
		return instance;
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
			return this;
		}

		return this.publish();
	}

	@Override
	public @NonNull Signal<T> subscribe(final @NonNull SignalSubscriber<@NonNull T> subscriber) {
		this.eventSet.add(subscriber);
		return this;
	}

	@Override
	public @NonNull Signal<T> unsubscribe(final @NonNull SignalSubscriber<@NonNull T> subscriber) {
		this.eventSet.remove(subscriber);
		return this;
	}

	@Override
	public @NonNull Signal<T> publish() {
		if (this.nextSilent) {
			this.nextSilent = false;
			return this;
		}

		final Set<SignalSubscriber<T>> outdatedSet = new HashSet<>();
		final Set<SignalSubscriber<T>> copiedSet = new HashSet<>(this.eventSet);
		for (final SignalSubscriber<T> subscriber : copiedSet) {
			if (!subscriber.update(this.value)) {
				outdatedSet.add(subscriber);
			}
		}

		this.eventSet.removeAll(outdatedSet);
		return this;
	}

	public @NonNull Set<@NonNull SignalSubscriber<@NonNull T>> getEventSet() {
		return this.eventSet;
	}

	@Override
	public @NonNull Signal<T> silent() {
		this.nextSilent = true;
		return this;
	}

	@Override
	public T getOrDefault() {
		return this.value != null ? this.value : this.defaultValue;
	}

	@Override
	public boolean isPresent() {
		return this.value != null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.value);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}

		final Signal<?> other = (Signal<?>) obj;
		return Objects.equals(this.value, other.value);
	}

}