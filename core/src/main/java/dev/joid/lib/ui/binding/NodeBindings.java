package dev.joid.lib.ui.binding;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import dev.joid.internal.JOIDLog;
import dev.joid.lib.signal.ComputedSignal;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.SignalContext;
import dev.joid.lib.signal.SignalListener;
import dev.joid.lib.ui.event.Event;
import dev.joid.lib.ui.event.EventBus;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

public final class NodeBindings {

	private final Object             owner;
	private final EventBus           eventBus;
	private final Supplier<String>   name;
	private final Consumer<Runnable> runner;

	private final List<NodeWatch>             watchList;
	private final List<InteractiveValue<?>>   valueList;
	private final Map<String, NodeBinding<?>> bindingMap;

	@Getter
	private boolean subscribed;

	public NodeBindings(final @NonNull Object owner, final @NonNull EventBus eventBus, final @NonNull Supplier<@NonNull String> name, final @NonNull Consumer<@NonNull Runnable> runner) {
		this.owner = owner;
		this.eventBus = eventBus;
		this.name = name;
		this.runner = runner;

		this.watchList = new ArrayList<>();
		this.valueList = new ArrayList<>();
		this.bindingMap = new LinkedHashMap<>();
	}

	public void subscribe(final boolean loaded) {
		if (this.subscribed) {
			return;
		}

		this.subscribed = true;
		for (final NodeBinding<?> binding : new ArrayList<>(this.bindingMap.values())) {
			binding.subscribe();
		}

		for (final NodeWatch watch : new ArrayList<>(this.watchList)) {
			watch.subscribe(loaded);
		}
	}

	public void unsubscribe() {
		if (!this.subscribed) {
			return;
		}

		this.subscribed = false;
		this.bindingMap.values().forEach(NodeBinding::unsubscribe);
		this.watchList.forEach(NodeWatch::unsubscribe);
	}

	public <T, V> @NonNull T start(final @NonNull String property, final V value, final @NonNull Consumer<V> applier) {
		this.release(property);
		applier.accept(value);
		return (T) this.owner;
	}

	public <T, V> @NonNull T follow(final @NonNull String property, final @NonNull Supplier<V> supplier, final @NonNull Consumer<V> applier) {
		this.release(property);
		final V value = SignalContext.current().untracked(supplier);
		if (!(supplier instanceof ComputedSignal) || !((ComputedSignal<V>) supplier).isConstant()) {
			this.bindingMap.put(property, new NodeBinding<>(supplier, applier, null, value));
		}

		applier.accept(value);
		return (T) this.owner;
	}

	public <T, V, M> @NonNull T bind(final @NonNull String property, final @NonNull Signal<V> signal, final @NonNull Consumer<@NonNull V> applier, final @NonNull Function<@NonNull M, V> writer) {
		return this.bindNullable(property, signal, value -> {
			if (value != null) {
				applier.accept(value);
			}
		}, writer);
	}

	public <T, V, M> @NonNull T bindNullable(final @NonNull String property, final @NonNull Signal<V> signal, final @NonNull Consumer<V> applier, final @NonNull Function<@NonNull M, V> writer) {
		if (!signal.isWritable()) {
			throw new IllegalArgumentException("The " + property + " of " + this.name.get() + " is changed by the user, so it cannot follow " + signal + ", which cannot be written: pass a writable Signal, or its value as a start value");
		}

		this.release(property);
		final NodeBinding<V> binding = new NodeBinding<>(signal, applier, (Function<Object, V>) writer, signal.peek());
		this.bindingMap.put(property, binding);
		if (this.subscribed) {
			signal.onChange(binding);
		}

		binding.applier.accept(binding.value);
		return (T) this.owner;
	}

	public void sync(final @NonNull String property, final @NonNull Object value) {
		final NodeBinding<?> binding = this.bindingMap.get(property);
		if (binding != null && binding.writer != null) {
			binding.write(value);
		}
	}

	public void release(final @NonNull String property) {
		final NodeBinding<?> binding = this.bindingMap.remove(property);
		if (binding != null && this.subscribed) {
			binding.unsubscribe();
		}
	}

	public void releaseFollows() {
		this.bindingMap.values().removeIf(binding -> binding.writer == null);
	}

	public boolean isBound(final @NonNull String property) {
		final NodeBinding<?> binding = this.bindingMap.get(property);
		return binding != null && binding.writer != null;
	}

	public boolean isFollowed(final @NonNull String property) {
		final NodeBinding<?> binding = this.bindingMap.get(property);
		return binding != null && binding.writer == null;
	}

	public <T> @NonNull T watch(final @NonNull Signal<?> signal, final @NonNull BooleanSupplier condition, final @NonNull Runnable action) {
		final NodeWatch watch = new NodeWatch(action, (Signal<Object>) signal, condition, signal.peek());
		this.watchList.add(watch);
		if (this.subscribed) {
			signal.onChange(watch);
		}
		return (T) this.owner;
	}

	public void pull() {
		if (this.bindingMap.isEmpty()) {
			return;
		}

		for (final Entry<String, NodeBinding<?>> binding : new ArrayList<>(this.bindingMap.entrySet())) {
			try {
				binding.getValue().pull();
			} catch (final RuntimeException exception) {
				JOIDLog.warnOnce("NodeBindings.pull." + this.name.get() + "." + binding.getKey() + "." + exception.getClass().getName(), "The " + binding.getKey() + " of " + this.name.get() + " cannot take its new value (" + exception + "), it keeps the previous one: check the value its signal gives", exception);
			}
		}
	}

	public void track(final @NonNull InteractiveValue<?> value) {
		this.valueList.add(value);
	}

	public void mark() {
		this.valueList.forEach(InteractiveValue::mark);
	}

	public void commit() {
		this.valueList.forEach(InteractiveValue::commit);
	}

	public <T> @NonNull T listen(final @NonNull EventType<?> type, final @NonNull EventListener<?> listener) {
		this.eventBus.listen(type, listener);
		return (T) this.owner;
	}

	public <E extends Event> E fire(final @NonNull EventType<?> type, final @NonNull Supplier<@NonNull E> event) {
		if (!this.eventBus.has(type)) {
			return null;
		}

		final E fired = event.get();
		this.runner.accept(() -> this.eventBus.fire(type, fired));
		return fired;
	}

	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	private final class NodeBinding<V> implements SignalListener<V> {

		private final Supplier<V>         source;
		private final Consumer<V>         applier;
		private final Function<Object, V> writer;

		private V value;

		@Override
		public void onChange(final V value) {
			if (NodeBindings.this.subscribed && !Objects.equals(this.value, value)) {
				this.value = value;
				this.applier.accept(value);
			}
		}

		private void pull() {
			final V next = this.source.get();
			if (!Objects.equals(this.value, next)) {
				this.value = next;
				this.applier.accept(next);
			}
		}

		private void subscribe() {
			if (this.writer == null) {
				return;
			}

			final Signal<V> signal = (Signal<V>) this.source;
			signal.onChange(this);
			final V current = signal.peek();
			if (!Objects.equals(this.value, current)) {
				this.onChange(current);
			}
		}

		private void unsubscribe() {
			if (this.writer == null) {
				return;
			}

			((Signal<V>) this.source).removeOnChange(this);
		}

		private void write(final Object value) {
			final Signal<V> signal = (Signal<V>) this.source;
			final V written = this.writer.apply(value);
			if (Objects.equals(written, this.value) || !Objects.equals(this.value, signal.peek())) {
				return;
			}

			final int count = SignalContext.current().countWriteBack(signal);
			if (count > 16) {
				if (count == 17) {
					signal.set((V) SignalContext.current().getWriteBackOrigin(signal));
				}

				JOIDLog.devWarnOnce("NodeBindings.bounds." + System.identityHashCode(signal), () -> NodeBindings.this.name.get() + " and the other controls bound to " + signal + " keep correcting each other: their bounds are incompatible, give them bounds that agree");
				return;
			}

			this.value = written;
			signal.set(written);
		}

	}

	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	private final class NodeWatch implements SignalListener<Object> {

		private final Runnable        action;
		private final Signal<Object>  signal;
		private final BooleanSupplier condition;

		private Object value;

		@Override
		public void onChange(final Object value) {
			if (!NodeBindings.this.subscribed) {
				return;
			}

			this.value = value;
			if (this.condition.getAsBoolean()) {
				this.action.run();
			}
		}

		private void subscribe(final boolean loaded) {
			this.signal.onChange(this);
			final Object current = this.signal.peek();
			if (loaded && !Objects.equals(this.value, current)) {
				this.onChange(current);
			}
		}

		private void unsubscribe() {
			this.value = this.signal.peek();
			this.signal.removeOnChange(this);
		}

	}

}