package dev.joid.lib.signal.impl.iterable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import dev.joid.lib.signal.Signal;
import lombok.NonNull;

public class MapSignal<K, V> extends Signal<Map<K, V>> {

	protected MapSignal() {
		super(Collections.emptyMap());
	}

	protected MapSignal(final @NonNull Map<K, V> value) {
		super(value);
	}

	public static <K, V> @NonNull MapSignal<K, V> create() {
		return new MapSignal<>();
	}

	public static <K, V> @NonNull MapSignal<K, V> create(final @NonNull Map<K, V> value) {
		return new MapSignal<>(value);
	}

	public @NonNull MapSignal<K, V> clear() {
		return this.mutate(Map::clear);
	}

	public boolean containsKey(final K key) {
		return super.get().containsKey(key);
	}

	public Set<Entry<K, V>> entrySet() {
		return super.get().entrySet();
	}

	public V get(final K key) {
		return super.get().get(key);
	}

	public boolean isEmpty() {
		return super.get().isEmpty();
	}

	public Set<K> keySet() {
		return super.get().keySet();
	}

	public @NonNull MapSignal<K, V> put(final K key, final V value) {
		return this.mutate(map -> map.put(key, value));
	}

	public @NonNull MapSignal<K, V> remove(final K key) {
		return this.mutate(map -> map.remove(key));
	}

	public int size() {
		return super.get().size();
	}

	public Collection<V> values() {
		return super.get().values();
	}

	@Override
	protected Map<K, V> seal(final Map<K, V> value) {
		return Collections.unmodifiableMap(new LinkedHashMap<>(value));
	}

	private @NonNull MapSignal<K, V> mutate(final @NonNull Consumer<Map<K, V>> mutation) {
		super.transform(current -> {
			final Map<K, V> map = new LinkedHashMap<>(current);
			mutation.accept(map);
			return map;
		});
		return this;
	}

}