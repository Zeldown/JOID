package be.zeldown.joid.lib.utils.list;

import java.util.List;

import lombok.NonNull;

public interface IndexedList<E extends IndexedElement> extends Iterable<E> {

	public int size();
	public E getLast();

	public void clear();

	public E getFirst();

	public boolean isEmpty();
	public E get(final int index);
	public void add(final E element);

	public @NonNull List<E> ordered();
	public @NonNull List<E> reversed();
	public void remove(final E element);

	public @NonNull IndexedList<E> copy();
	public boolean contains(final E element);

	public @NonNull IndexedList<E> recursive();

}