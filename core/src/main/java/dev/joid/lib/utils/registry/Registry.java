package dev.joid.lib.utils.registry;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Registry<T> {

	private final List<T> list = new CopyOnWriteArrayList<>();

	public static <T> @NonNull Registry<T> create() {
		return new Registry<>();
	}

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

}