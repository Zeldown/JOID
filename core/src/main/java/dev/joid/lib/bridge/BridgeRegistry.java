package dev.joid.lib.bridge;

import java.util.function.Predicate;

import dev.joid.lib.utils.list.IndexedLinkedList;
import lombok.NonNull;

public class BridgeRegistry<T extends IBridge> {

	@NonNull private final String               name;
	@NonNull private final IndexedLinkedList<T> bridgeList;

	protected BridgeRegistry(final @NonNull String name) {
		this.name       = name;
		this.bridgeList = new IndexedLinkedList<>();
	}

	public static <T extends IBridge> @NonNull BridgeRegistry<T> create(final @NonNull String name) {
		return new BridgeRegistry<>(name);
	}

	public final void register(final @NonNull T bridge) {
		this.bridgeList.add(bridge);
	}

	public final void unregister(final @NonNull T bridge) {
		this.bridgeList.remove(bridge);
	}

	public final @NonNull T get() {
		if (this.bridgeList.isEmpty()) {
			throw new IllegalStateException("No " + this.name.toLowerCase() + " bridge registered, call BridgeHandler." + this.name + ".register before using JOID");
		}
		return this.bridgeList.getLast();
	}

	public final <B extends T> B getBridge(final @NonNull Class<B> bridgeClass) {
		return bridgeClass.cast(this.find(bridgeClass::isInstance));
	}

	public final T find(final @NonNull Predicate<@NonNull T> filter) {
		for (final T bridge : this.bridgeList.reversed()) {
			if (filter.test(bridge)) {
				return bridge;
			}
		}
		return null;
	}

}