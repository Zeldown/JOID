package dev.joid.lib.ui.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("unchecked")
public final class EventBus {

	private final Supplier<String>                           owner;
	private final Map<EventType<?>, List<EventRegistration>> listenerMap;

	private EventBus(final Supplier<String> owner) {
		this.owner = owner;
		this.listenerMap = new LinkedHashMap<>();
	}

	public static @NonNull EventBus create(final @NonNull Supplier<@NonNull String> owner) {
		return new EventBus(owner);
	}

	public @NonNull EventBus listen(final @NonNull EventType<?> type, final @NonNull EventPriority priority, final @NonNull EventListener<?> listener) {
		final List<EventRegistration> registrationList = this.listenerMap.computeIfAbsent(type, key -> new ArrayList<>());
		int index = registrationList.size();
		while (index > 0 && registrationList.get(index - 1).priority.ordinal() > priority.ordinal()) {
			index--;
		}

		registrationList.add(index, new EventRegistration(priority, listener));
		return this;
	}

	public @NonNull EventBus addAll(final @NonNull EventBus bus) {
		for (final Entry<EventType<?>, List<EventRegistration>> entry : bus.listenerMap.entrySet()) {
			for (final EventRegistration registration : entry.getValue()) {
				this.listen(entry.getKey(), registration.priority, registration.listener);
			}
		}
		return this;
	}

	public @NonNull EventBus keep() {
		this.listenerMap.values().forEach(registrationList -> registrationList.forEach(registration -> registration.kept = true));
		return this;
	}

	public @NonNull EventBus reset() {
		this.listenerMap.values().forEach(registrationList -> registrationList.removeIf(registration -> !registration.kept));
		this.listenerMap.values().removeIf(List::isEmpty);
		return this;
	}

	public @NonNull EventBus clear() {
		this.listenerMap.clear();
		return this;
	}

	public void fire(final @NonNull EventType<?> type, final @NonNull Event event) {
		this.fireListeners(type, event);
		this.fireMonitor(type, event);
	}

	public void fireListeners(final @NonNull EventType<?> type, final @NonNull Event event) {
		for (final EventRegistration registration : this.getRegistrationList(type)) {
			if (registration.priority.isMonitor() || EventBus.isStopped(event)) {
				return;
			}

			this.run(type, registration, event);
		}
	}

	public void fireMonitor(final @NonNull EventType<?> type, final @NonNull Event event) {
		for (final EventRegistration registration : this.getRegistrationList(type)) {
			if (!registration.priority.isMonitor()) {
				continue;
			}

			event.monitor(true);
			try {
				this.run(type, registration, event);
			} finally {
				event.monitor(false);
			}
		}
	}

	public boolean has(final @NonNull EventType<?> type) {
		return this.listenerMap.containsKey(type);
	}

	public boolean isEmpty() {
		return this.listenerMap.isEmpty();
	}

	public int count(final @NonNull EventType<?> type) {
		final List<EventRegistration> registrationList = this.listenerMap.get(type);
		return registrationList == null ? 0 : registrationList.size();
	}

	public @NonNull Set<@NonNull EventType<?>> getTypes() {
		return Collections.unmodifiableSet(this.listenerMap.keySet());
	}

	private List<EventRegistration> getRegistrationList(final EventType<?> type) {
		final List<EventRegistration> registrationList = this.listenerMap.get(type);
		return registrationList == null ? Collections.emptyList() : new ArrayList<>(registrationList);
	}

	private void run(final EventType<?> type, final EventRegistration registration, final Event event) {
		try {
			((EventListener<Event>) registration.listener).apply(event);
		} catch (final RuntimeException exception) {
			final String failure = exception.toString();
			if (failure.equals(registration.failure)) {
				return;
			}

			registration.failure = failure;
			System.err.println("[JOID] The " + type.getName() + " listener of " + this.owner.get() + " failed: " + failure);
			exception.printStackTrace();
		}
	}

	private static boolean isStopped(final Event event) {
		return event instanceof CancellableEvent && ((CancellableEvent) event).isCancelled() || event instanceof ConsumableEvent && ((ConsumableEvent) event).isConsumed();
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class EventRegistration {

		private final EventPriority    priority;
		private final EventListener<?> listener;

		private boolean kept;
		private String  failure;

	}

}