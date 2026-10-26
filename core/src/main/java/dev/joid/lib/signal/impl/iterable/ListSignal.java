package dev.joid.lib.signal.impl.iterable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import dev.joid.lib.signal.Signal;
import lombok.NonNull;

public class ListSignal<E> extends Signal<List<E>> {

	public ListSignal() {
		super(Collections.emptyList());
	}

	public ListSignal(final @NonNull Collection<E> value) {
		super(new ArrayList<>(value));
	}

	public @NonNull ListSignal<E> clear() {
		return this.mutate(List::clear);
	}

	public @NonNull ListSignal<E> add(final E e) {
		return this.mutate(list -> list.add(e));
	}

	public @NonNull ListSignal<E> remove(final E e) {
		return this.mutate(list -> list.remove(e));
	}

	public @NonNull ListSignal<E> remove(final int index) {
		return this.mutate(list -> list.remove(index));
	}

	public @NonNull ListSignal<E> set(final int index, final E element) {
		return this.mutate(list -> list.set(index, element));
	}

	public boolean contains(final E e) {
		return super.get().contains(e);
	}

	public E get(final int index) {
		return super.get().get(index);
	}

	public int indexOf(final E e) {
		return super.get().indexOf(e);
	}

	public boolean isEmpty() {
		return super.get().isEmpty();
	}

	public int size() {
		return super.get().size();
	}

	@Override
	protected List<E> seal(final List<E> value) {
		return Collections.unmodifiableList(new ArrayList<>(value));
	}

	private @NonNull ListSignal<E> mutate(final Consumer<List<E>> mutation) {
		return super.transform(current -> {
			final List<E> list = new ArrayList<>(current);
			mutation.accept(list);
			return list;
		});
	}

}