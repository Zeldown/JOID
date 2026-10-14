package dev.joid.lib.signal.impl.iterable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

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
		this.mutate(map -> {
			map.clear();
			return null;
		}, map -> !map.isEmpty());
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
		return this.mutate(map -> map.put(key, value), map -> !map.containsKey(key) || !Objects.equals(map.get(key), value));
	}

	public V remove(final K key) {
		return this.mutate(map -> map.remove(key), map -> map.containsKey(key));
	}

	public int size() {
		return this.get().size();
	}

	public Collection<V> values() {
		return Collections.unmodifiableCollection(this.get().values());
	}

	private <R> R mutate(final Function<Map<K, V>, R> mutation, final Predicate<Map<K, V>> changed) {
		final Map<K, V> base = this.peek();
		final Map<K, V> map = new LinkedHashMap<>(base);
		final boolean change = changed.test(base);
		final R result = mutation.apply(map);
		this.write(() -> {
			if (this.peek() == base) {
				this.commit(map, change);
			} else {
				this.mutate(mutation, changed);
			}
		});
		return result;
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