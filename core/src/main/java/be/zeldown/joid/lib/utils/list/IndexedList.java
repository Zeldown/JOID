package be.zeldown.joid.lib.utils.list;

import java.util.List;

import lombok.NonNull;

public interface IndexedList<E extends IndexedElement> extends Iterable<E> {

	public void add(final E element);
	public void remove(final E element);

	public void clear();

	public @NonNull IndexedList<E> copy();

	/* [ Getter Methods ] */
	public int size();
	public boolean isEmpty();
	public boolean contains(final E element);

	public E get(final int index);
	public E getFirst();
	public E getLast();

	public @NonNull List<E> ordered();
	public @NonNull List<E> reversed();

	public @NonNull IndexedList<E> recursive();

}