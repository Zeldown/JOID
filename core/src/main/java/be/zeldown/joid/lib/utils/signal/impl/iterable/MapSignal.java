package be.zeldown.joid.lib.utils.signal.impl.iterable;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import be.zeldown.joid.lib.utils.signal.Signal;
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
		this.getOrDefault().clear();
		this.publish();
		return this;
	}

	public boolean containsKey(final K key) {
		return this.getOrDefault().containsKey(key);
	}

	public Set<Entry<K, V>> entrySet() {
		return this.getOrDefault().entrySet();
	}

	public V get(final K key) {
		return this.getOrDefault().get(key);
	}

	public boolean isEmpty() {
		return this.getOrDefault().isEmpty();
	}

	public Set<K> keySet() {
		return this.getOrDefault().keySet();
	}

	public V put(final K key, final V value) {
		final V result = this.getOrDefault().put(key, value);
		this.publish();
		return result;
	}

	public V remove(final K key) {
		final V result = this.getOrDefault().remove(key);
		this.publish();
		return result;
	}

	public int size() {
		return this.getOrDefault().size();
	}

	public Collection<V> values() {
		return this.getOrDefault().values();
	}

	@Override
	public String toString() {
		return this.getOrDefault() == null ? "MapSignal{null}" : "MapSignal{" + this.getOrDefault().toString() + "}";
	}

}