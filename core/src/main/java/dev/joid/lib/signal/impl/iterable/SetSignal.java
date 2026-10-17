package dev.joid.lib.signal.impl.iterable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import dev.joid.lib.signal.Signal;
import lombok.NonNull;

public class SetSignal<E> extends Signal<Set<E>> {

	protected SetSignal() {
		super(Collections.emptySet());
	}

	protected SetSignal(final @NonNull Collection<E> value) {
		super(new LinkedHashSet<>(value));
	}

	public static <E> @NonNull SetSignal<E> create() {
		return new SetSignal<>();
	}

	public static <E> @NonNull SetSignal<E> create(final @NonNull Collection<E> value) {
		return new SetSignal<>(value);
	}

	public @NonNull SetSignal<E> clear() {
		return this.mutate(Set::clear);
	}

	public @NonNull SetSignal<E> add(final E e) {
		return this.mutate(set -> set.add(e));
	}

	public @NonNull SetSignal<E> remove(final E e) {
		return this.mutate(set -> set.remove(e));
	}

	public boolean contains(final E e) {
		return super.get().contains(e);
	}

	public boolean isEmpty() {
		return super.get().isEmpty();
	}

	public int size() {
		return super.get().size();
	}

	@Override
	protected Set<E> seal(final Set<E> value) {
		return Collections.unmodifiableSet(new LinkedHashSet<>(value));
	}

	private @NonNull SetSignal<E> mutate(final @NonNull Consumer<Set<E>> mutation) {
		super.transform(current -> {
			final Set<E> set = new LinkedHashSet<>(current);
			mutation.accept(set);
			return set;
		});
		return this;
	}

}