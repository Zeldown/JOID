package dev.joid.lib.bridge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

import lombok.NonNull;

public class BridgeRegistry<T extends IBridge> {

	@NonNull private final String          name;
	@NonNull private final Map<T, Integer> priorityMap;

	@NonNull
	private volatile List<@NonNull T> bridgeList;

	protected BridgeRegistry(final @NonNull String name) {
		this.name        = name;
		this.priorityMap = new HashMap<>();
		this.bridgeList  = Collections.emptyList();
	}

	public static <T extends IBridge> @NonNull BridgeRegistry<T> create(final @NonNull String name) {
		return new BridgeRegistry<>(name);
	}

	public final void register(final @NonNull T bridge) {
		this.register(bridge, 0);
	}

	public final synchronized void register(final @NonNull T bridge, final int priority) {
		final List<T> bridgeList = new ArrayList<>(this.bridgeList);
		bridgeList.remove(bridge);
		this.priorityMap.put(bridge, priority);

		int index = bridgeList.size();
		while (index > 0 && this.priorityMap.get(bridgeList.get(index - 1)) > priority) {
			index--;
		}

		bridgeList.add(index, bridge);
		this.bridgeList = Collections.unmodifiableList(bridgeList);
	}

	public final synchronized void unregister(final @NonNull T bridge) {
		final List<T> bridgeList = new ArrayList<>(this.bridgeList);
		bridgeList.remove(bridge);
		this.priorityMap.remove(bridge);
		this.bridgeList = Collections.unmodifiableList(bridgeList);
	}

	public final synchronized void registerDefault(final @NonNull T bridge) {
		if (this.bridgeList.isEmpty()) {
			this.register(bridge, Integer.MIN_VALUE);
		}
	}

	public final @NonNull T get() {
		final List<T> bridgeList = this.bridgeList;
		if (bridgeList.isEmpty()) {
			throw new IllegalStateException("No " + this.name.toLowerCase(Locale.ROOT) + " bridge registered, call BridgeHandler." + this.name + ".register before using JOID");
		}
		return bridgeList.get(bridgeList.size() - 1);
	}

	public final <B extends T> B getBridge(final @NonNull Class<B> bridgeClass) {
		return bridgeClass.cast(this.find(bridgeClass::isInstance));
	}

	public final T find(final @NonNull Predicate<@NonNull T> filter) {
		final List<T> bridgeList = this.bridgeList;
		for (int i = bridgeList.size() - 1; i >= 0; i--) {
			if (filter.test(bridgeList.get(i))) {
				return bridgeList.get(i);
			}
		}
		return null;
	}

	public final @NonNull List<@NonNull T> getBridgeList() {
		return this.bridgeList;
	}

}