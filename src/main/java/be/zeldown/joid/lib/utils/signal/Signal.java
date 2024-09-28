package be.zeldown.joid.lib.utils.signal;

import java.util.HashSet;
import java.util.Set;

public class Signal<T> implements ISignal<T> {

	private volatile T defaultValue;
	private volatile T value;
	private final Set<SignalSubscriber<T>> eventSet;

	private boolean nextSilent = false;

	public Signal() {
		this(null);
	}

	public Signal(final T defaultValue) {
		this.defaultValue = defaultValue;
		this.eventSet = new HashSet<>();
	}

	public static <T> Signal<T> of(final T defaultValue) {
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
		this.value = this.defaultValue;
	}

	@Override
	public void set(final T value) {
		this.value = value;
		this.publish();
	}

	@Override
	public void subscribe(final SignalSubscriber<T> subscriber) {
		this.eventSet.add(subscriber);
	}

	@Override
	public void unsubscribe(final SignalSubscriber<T> subscriber) {
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

	public Set<SignalSubscriber<T>> getEventSet() {
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

}