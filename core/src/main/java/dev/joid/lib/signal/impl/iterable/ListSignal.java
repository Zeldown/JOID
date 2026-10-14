package dev.joid.lib.signal.impl.iterable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import dev.joid.lib.signal.Signal;
import lombok.NonNull;

public class ListSignal<E> extends Signal<List<E>> {

	protected ListSignal() {
		super(new ArrayList<>());
	}

	protected ListSignal(final @NonNull Collection<E> value) {
		super(new ArrayList<>(value));
	}

	public static <E> @NonNull ListSignal<E> of() {
		return new ListSignal<>();
	}

	public static <E> @NonNull ListSignal<E> of(final @NonNull Collection<E> value) {
		return new ListSignal<>(value);
	}

	public @NonNull ListSignal<E> clear() {
		final List<E> list = new ArrayList<>();
		this.commit(list, !this.peek().isEmpty());
		return this;
	}

	public boolean add(final E e) {
		final List<E> list = this.copy();
		final boolean success = list.add(e);
		this.commit(list, success);
		return success;
	}

	public boolean remove(final E e) {
		final List<E> list = this.copy();
		final boolean success = list.remove(e);
		this.commit(list, success);
		return success;
	}

	public E remove(final int index) {
		final List<E> list = this.copy();
		final E result = list.remove(index);
		this.commit(list, true);
		return result;
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
		final List<E> list = this.copy();
		final E result = list.set(index, element);
		this.commit(list, !Objects.equals(result, element));
		return result;
	}

	public int size() {
		return this.get().size();
	}

	private List<E> copy() {
		return new ArrayList<>(this.peek());
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