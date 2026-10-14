package dev.joid.lib.signal.impl.iterable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import dev.joid.lib.signal.Signal;
import lombok.NonNull;

public class MapSignal<K, V> extends Signal<Map<K, V>> {

	public MapSignal() {
		super(new LinkedHashMap<>());
	}

	public MapSignal(final @NonNull Map<K, V> value) {
		super(new LinkedHashMap<>(value));
	}

	public @NonNull MapSignal<K, V> clear() {
		final Map<K, V> map = new LinkedHashMap<>();
		this.commit(map, !this.peek().isEmpty());
		return this;
	}

	public boolean containsKey(final K key) {
		return this.get().containsKey(key);
	}

	public Set<Entry<K, V>> entrySet() {
		return Collections.unmodifiableSet(this.get().entrySet());
	}

	public V get(final K key) {
		return this.get().get(key);
	}

	public boolean isEmpty() {
		return this.get().isEmpty();
	}

	public Set<K> keySet() {
		return Collections.unmodifiableSet(this.get().keySet());
	}

	public V put(final K key, final V value) {
		final Map<K, V> map = this.copy();
		final boolean changed = !map.containsKey(key) || !Objects.equals(map.get(key), value);
		final V result = map.put(key, value);
		this.commit(map, changed);
		return result;
	}

	public V remove(final K key) {
		final Map<K, V> map = this.copy();
		final boolean changed = map.containsKey(key);
		final V result = map.remove(key);
		this.commit(map, changed);
		return result;
	}

	public int size() {
		return this.get().size();
	}

	public Collection<V> values() {
		return Collections.unmodifiableCollection(this.get().values());
	}

	private Map<K, V> copy() {
		return new LinkedHashMap<>(this.peek());
	}

	private void commit(final Map<K, V> map, final boolean changed) {
		if (changed) {
			this.set(map);
		} else {
			this.publishIf(false);
		}
	}

	@Override
	public String toString() {
		return "MapSignal{" + this.peek() + "}";
	}

}