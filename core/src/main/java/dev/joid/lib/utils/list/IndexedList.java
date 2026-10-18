package dev.joid.lib.utils.list;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.Lists;

import lombok.NonNull;

public final class IndexedList<E extends IIndexedElement> implements Iterable<E> {

	private final List<E> orderedList;
	private final List<E> orderedView;
	private final List<E> reversedList;

	private IndexedList(final List<E> orderedList) {
		this.orderedList  = orderedList;
		this.orderedView  = Collections.unmodifiableList(orderedList);
		this.reversedList = Collections.unmodifiableList(Lists.reverse(orderedList));
	}

	public static <E extends IIndexedElement> @NonNull IndexedList<E> concurrent() {
		return new IndexedList<>(new CopyOnWriteArrayList<>());
	}

	public void add(final E element) {
		this.sort();
		if (this.orderedList.contains(element)) {
			return;
		}

		for (int i = 0; i < this.orderedList.size(); i++) {
			if (this.orderedList.get(i).getIndex() > element.getIndex()) {
				this.orderedList.add(i, element);
				return;
			}
		}

		this.orderedList.add(element);
	}

	public void remove(final E element) {
		this.orderedList.remove(element);
	}

	public void clear() {
		this.orderedList.clear();
	}

	public int size() {
		return this.orderedList.size();
	}

	public boolean isEmpty() {
		return this.orderedList.isEmpty();
	}

	public boolean contains(final E element) {
		return this.orderedList.contains(element);
	}

	public E getLast() {
		this.sort();
		return this.orderedList.isEmpty() ? null : this.orderedList.get(this.orderedList.size() - 1);
	}

	public E getFirst() {
		this.sort();
		return this.orderedList.isEmpty() ? null : this.orderedList.get(0);
	}

	public E get(final int index) {
		this.sort();
		return this.orderedList.get(index);
	}

	public @NonNull List<E> ordered() {
		this.sort();
		return this.orderedView;
	}

	public @NonNull List<E> reversed() {
		this.sort();
		return this.reversedList;
	}

	@Override
	public Iterator<E> iterator() {
		this.sort();
		return this.orderedList.iterator();
	}

	private void sort() {
		int index = Integer.MIN_VALUE;
		for (final E element : this.orderedList) {
			if (element.getIndex() < index) {
				this.orderedList.sort(Comparator.comparingInt(IIndexedElement::getIndex));
				return;
			}

			index = element.getIndex();
		}
	}

}