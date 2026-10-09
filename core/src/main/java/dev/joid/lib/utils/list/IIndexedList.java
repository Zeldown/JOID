package dev.joid.lib.utils.list;

import java.util.List;

import lombok.NonNull;

public interface IIndexedList<E extends IndexedElement> extends Iterable<E> {

	public void add(final E element);
	public void remove(final E element);

	public void sort();
	public void clear();

	public @NonNull IIndexedList<E> copy();

	public int size();
	public boolean isEmpty();
	public boolean contains(final E element);

	public E getLast();
	public E getFirst();
	public E get(final int index);

	public @NonNull List<E> ordered();
	public @NonNull List<E> reversed();

	public @NonNull IIndexedList<E> recursive();

}