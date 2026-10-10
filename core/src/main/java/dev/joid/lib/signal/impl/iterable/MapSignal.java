package dev.joid.lib.signal.impl.iterable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import dev.joid.lib.signal.Signal;
import lombok.NonNull;

public class MapSignal<K, V> extends Signal<Map<K, V>> {

	public MapSignal() {}

	public MapSignal(final Map<K, V> value) {
		super(value);
	}

	public static <K, V> MapSignal<K, V> of(final Map<K, V>defaultValue) {
		final MapSignal<K, V> instance = new MapSignal<>(defaultValue);
		instance.set(defaultValue);
		return instance;
	}

	public @NonNull MapSignal<K, V> clear() {
		final Map<K, V> map = this.mutable();
		final boolean changed = !map.isEmpty();
		map.clear();
		this.publishIf(changed);
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
		final Map<K, V> map = this.mutable();
		final boolean changed = !map.containsKey(key) || !Objects.equals(map.get(key), value);
		final V result = map.put(key, value);
		this.publishIf(changed);
		return result;
	}

	public V remove(final K key) {
		final Map<K, V> map = this.mutable();
		final boolean changed = map.containsKey(key);
		final V result = map.remove(key);
		this.publishIf(changed);
		return result;
	}

	public int size() {
		return this.get().size();
	}

	public Collection<V> values() {
		return this.get().values();
	}

	private Map<K, V> mutable() {
		if (this.peek() == this.getDefaultValue()) {
			this.assign(this.peek() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(this.peek()));
		}

		return this.peek();
	}

	@Override
	public String toString() {
		return this.peek() == null ? "MapSignal{null}" : "MapSignal{" + this.peek().toString() + "}";
	}

}