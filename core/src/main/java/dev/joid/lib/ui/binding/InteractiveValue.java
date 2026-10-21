package dev.joid.lib.ui.binding;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.event.Event;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.ICancellableEvent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

public final class InteractiveValue<V> {

	private final String       property;
	private final Supplier<V>  getter;
	private final Consumer<V>  setter;
	private final NodeBindings bindings;

	private final Map<V, ValueEvents<V>> eventMap;

	private ValueEvents<V>   events;
	private UnaryOperator<V> clamp;
	private BiConsumer<V, V> then;

	private V       pending;
	private V       requested;
	private boolean changed;
	private boolean tracked;

	private InteractiveValue(final NodeBindings bindings, final String property, final Supplier<V> getter, final Consumer<V> setter) {
		this.property = property;
		this.getter = getter;
		this.setter = setter;
		this.bindings = bindings;
		this.eventMap = new HashMap<>();
		this.clamp = UnaryOperator.identity();
		this.then = (previous, next) -> {};
		this.bindings.track(this);
	}

	public static <V> @NonNull InteractiveValue<V> create(final @NonNull NodeBindings bindings, final @NonNull String property, final @NonNull Supplier<V> getter, final @NonNull Consumer<V> setter) {
		return new InteractiveValue<>(bindings, property, getter, setter);
	}

	public @NonNull InteractiveValue<V> clamp(final @NonNull UnaryOperator<V> clamp) {
		this.clamp = clamp;
		return this;
	}

	public @NonNull InteractiveValue<V> then(final @NonNull BiConsumer<V, V> then) {
		this.then = then;
		return this;
	}

	public @NonNull InteractiveValue<V> events(final @NonNull EventType<?> beforeType, final @NonNull BiFunction<V, V, ? extends Event> before, final @NonNull EventType<?> onType, final @NonNull BiFunction<V, V, ? extends Event> on) {
		this.events = new ValueEvents<>(onType, beforeType, on, before);
		return this;
	}

	public @NonNull InteractiveValue<V> events(final V target, final @NonNull EventType<?> beforeType, final @NonNull BiFunction<V, V, ? extends Event> before, final @NonNull EventType<?> onType, final @NonNull BiFunction<V, V, ? extends Event> on) {
		this.eventMap.put(target, new ValueEvents<>(onType, beforeType, on, before));
		return this;
	}

	public <T> @NonNull T start(final V value) {
		return this.bindings.start(this.property, value, this::update);
	}

	public <T> @NonNull T bind(final @NonNull Signal<V> signal) {
		return this.bindings.bind(this.property, signal, this::update, (final V value) -> value);
	}

	public boolean change(final V value) {
		if (this.tracked) {
			this.set(value);
			return true;
		}

		final V next = this.clamp.apply(value);
		final V previous = this.getter.get();
		if (InteractiveValue.same(previous, next)) {
			this.requested = next;
			this.bindings.see(this.property);
			this.bindings.sync(this.property, next);
			return true;
		}

		final ValueEvents<V> events = this.getEvents(next);
		if (events != null) {
			final Event event = this.bindings.fireSubscribed(events.beforeType, () -> events.before.apply(previous, next));
			if (event != null && ((ICancellableEvent) event).isCancelled()) {
				return false;
			}
		}

		this.requested = next;
		this.bindings.see(this.property);
		this.bindings.batch(() -> {
			this.bindings.external(() -> this.store(next));
			this.fireChange(previous, next);
		});
		return true;
	}

	public void update(final V value) {
		this.requested = value;
		final V next = this.clamp.apply(value);
		final V previous = this.getter.get();
		if (InteractiveValue.same(previous, next)) {
			this.bindings.sync(this.property, next);
		} else {
			this.apply(previous, next);
		}
	}

	public void reapply() {
		this.update(this.getRequested());
	}

	public V get() {
		return this.changed ? this.clamp.apply(this.pending) : this.getter.get();
	}

	public V getRequested() {
		return this.requested != null ? this.requested : this.getter.get();
	}

	public void set(final V value) {
		if (!this.tracked) {
			this.change(value);
			return;
		}

		this.pending = value;
		this.changed = true;
	}

	public void mark() {
		this.tracked = true;
	}

	public void commit() {
		this.tracked = false;
		if (!this.changed) {
			return;
		}

		final V value = this.pending;
		this.pending = null;
		this.changed = false;
		this.change(value);
	}

	public <T> @NonNull T before(final @NonNull EventListener<?> listener) {
		return this.bindings.listen(this.events.beforeType, listener);
	}

	public <T> @NonNull T before(final V target, final @NonNull EventListener<?> listener) {
		return this.bindings.listen(this.eventMap.get(target).beforeType, listener);
	}

	public <T> @NonNull T on(final @NonNull EventListener<?> listener) {
		return this.bindings.listen(this.events.onType, listener);
	}

	public <T> @NonNull T on(final V target, final @NonNull EventListener<?> listener) {
		return this.bindings.listen(this.eventMap.get(target).onType, listener);
	}

	private void apply(final V previous, final V next) {
		this.store(next);
		this.fireChange(previous, next);
	}

	private void store(final V next) {
		this.setter.accept(next);
		this.bindings.sync(this.property, next);
	}

	private void fireChange(final V previous, final V next) {
		final ValueEvents<V> events = this.getEvents(next);
		if (events != null) {
			this.bindings.fireSubscribed(events.onType, () -> events.on.apply(previous, next));
		}
		this.then.accept(previous, next);
	}

	private ValueEvents<V> getEvents(final V value) {
		final ValueEvents<V> events = this.eventMap.get(value);
		return events != null ? events : this.events;
	}

	private static boolean same(final Object previous, final Object next) {
		return Objects.equals(previous, next) || previous instanceof Number && next instanceof Number && ((Number) previous).doubleValue() == ((Number) next).doubleValue();
	}

	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class ValueEvents<V> {

		private final EventType<?>                      onType;
		private final EventType<?>                      beforeType;
		private final BiFunction<V, V, ? extends Event> on;
		private final BiFunction<V, V, ? extends Event> before;

	}

}