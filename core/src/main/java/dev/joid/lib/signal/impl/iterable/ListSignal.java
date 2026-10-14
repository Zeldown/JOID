package dev.joid.lib.signal.impl.iterable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import dev.joid.lib.signal.Signal;
import lombok.NonNull;

public class ListSignal<E> extends Signal<List<E>> {

	public ListSignal() {
		super(new ArrayList<>());
	}

	public ListSignal(final @NonNull Collection<E> value) {
		super(new ArrayList<>(value));
	}

	public @NonNull ListSignal<E> clear() {
		this.mutate(list -> {
			final boolean changed = !list.isEmpty();
			list.clear();
			return changed;
		}, Boolean::booleanValue);
		return this;
	}

	public boolean add(final E e) {
		return this.mutate(list -> list.add(e), Boolean::booleanValue);
	}

	public boolean remove(final E e) {
		return this.mutate(list -> list.remove(e), Boolean::booleanValue);
	}

	public E remove(final int index) {
		return this.mutate(list -> list.remove(index), result -> true);
	}

	public boolean contains(final E e) {
		return this.get().contains(e);
	}

	public E get(final int index) {
		return this.get().get(index);
	}

	public int indexOf(final E e) {
		return this.get().indexOf(e);
	}

	public boolean isEmpty() {
		return this.get().isEmpty();
	}

	public E set(final int index, final E element) {
		return this.mutate(list -> list.set(index, element), result -> !Objects.equals(result, element));
	}

	public int size() {
		return this.get().size();
	}

	private <R> R mutate(final Function<List<E>, R> mutation, final Predicate<R> changed) {
		final List<E> base = this.peek();
		final List<E> list = new ArrayList<>(base);
		final R result = mutation.apply(list);
		this.write(() -> {
			if (this.peek() == base) {
				this.commit(list, changed.test(result));
			} else {
				this.mutate(mutation, changed);
			}
		});
		return result;
	}

	private void commit(final List<E> list, final boolean changed) {
		if (changed) {
			this.set(list);
		} else {
			this.publishIf(false);
		}
	}

	@Override
	public String toString() {
		return "ListSignal{" + this.peek() + "}";
	}

}