package be.zeldown.joid.lib.utils.signal;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import lombok.NonNull;

/**
 * The `Signal` class in Java represents a signal with a generic type, allowing subscribers to receive
 * updates when the signal value changes.
 */
public class Signal<T> implements ISignal<T> {

	private final transient Set<@NonNull SignalSubscriber<@NonNull T>> eventSet;

	private volatile T defaultValue;
	private volatile T value;

	private boolean nextSilent = false;

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

	@Override
	public T getOrDefault() {
		return this.value != null ? this.value : this.defaultValue;
	}

	@Override
	public void reset() {
		this.set(this.defaultValue);
	}

	@Override
	public void set(final T value) {
		final T oldValue = this.value;
		final T newValue = value;

		if (oldValue == null && newValue == null || oldValue != null && oldValue.equals(newValue)) {
			return;
		}

		this.publish();
	}

	@Override
	public void subscribe(final @NonNull SignalSubscriber<@NonNull T> subscriber) {
		this.eventSet.add(subscriber);
	}

	@Override
	public void unsubscribe(final @NonNull SignalSubscriber<@NonNull T> subscriber) {
		this.eventSet.remove(subscriber);
	}

	@Override
	public void publish() {
		if (this.nextSilent) {
			this.nextSilent = false;
			return;
		}

		final Set<SignalSubscriber<T>> outdatedSet = new HashSet<>();
		final Set<SignalSubscriber<T>> copiedSet = new HashSet<>(this.eventSet);
		for (final SignalSubscriber<T> subscriber : copiedSet) {
			if (!subscriber.update(this.value)) {
				outdatedSet.add(subscriber);
			}
		}

		this.eventSet.removeAll(outdatedSet);
	}

	public @NonNull Set<@NonNull SignalSubscriber<@NonNull T>> getEventSet() {
		return this.eventSet;
	}

	@Override
	public void silent() {
		this.nextSilent = true;
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