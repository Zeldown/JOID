package dev.joid.lib.utils.signal.impl.iterable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import dev.joid.lib.utils.signal.Signal;
import lombok.NonNull;

public class MapSignal<K, V> extends Signal<Map<K, V>> {

	public MapSignal() {}

	public MapSignal(final Map<K, V> value) {
		super(value);
	}

	public static <K, V> MapSignal<K, V> of(final Map<K, V>defaultValue) {
		final MapSignal<K, V> instance = new MapSignal<>();
		instance.set(defaultValue);
		return instance;
	}

	public @NonNull MapSignal<K, V> clear() {
		this.mutable().clear();
		this.publish();
		return this;
	}

	public boolean containsKey(final K key) {
		return this.get().containsKey(key);
	}

	public Set<Entry<K, V>> entrySet() {
		return this.get().entrySet();
	}

	public V get(final K key) {
		return this.get().get(key);
	}

	public boolean isEmpty() {
		return this.get().isEmpty();
	}

	public Set<K> keySet() {
		return this.get().keySet();
	}

	public V put(final K key, final V value) {
		final V result = this.mutable().put(key, value);
		this.publish();
		return result;
	}

	public V remove(final K key) {
		final V result = this.mutable().remove(key);
		this.publish();
		return result;
	}

	public int size() {
		return this.get().size();
	}

	public Collection<V> values() {
		return this.get().values();
	}

	private Map<K, V> mutable() {
		if (!this.isPresent()) {
			this.silent().set(this.peek() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(this.peek()));
		}

		return this.peek();
	}

	@Override
	public String toString() {
		return this.peek() == null ? "MapSignal{null}" : "MapSignal{" + this.peek().toString() + "}";
	}

}