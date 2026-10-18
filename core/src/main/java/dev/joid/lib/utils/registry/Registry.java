package dev.joid.lib.utils.registry;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import lombok.NonNull;

public final class Registry<T> {

	private final List<T> list = new CopyOnWriteArrayList<>();

	public void register(final @NonNull T value) {
		this.list.remove(value);
		this.list.add(0, value);
	}

	public void unregister(final @NonNull T value) {
		this.list.remove(value);
	}

	public T find(final @NonNull Predicate<T> predicate) {
		for (final T value : this.list) {
			if (predicate.test(value)) {
				return value;
			}
		}
		return null;
	}

	public @NonNull List<T> getAll() {
		return Collections.unmodifiableList(this.list);
	}

}