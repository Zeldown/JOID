package dev.joid.lib.utils.list;

import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.Lists;

import lombok.NonNull;

public final class IndexedList<E extends IIndexedElement> implements Iterable<E> {

	private final List<E> orderedList;
	private final List<E> orderedView;
	private final List<E> reversedList;
	private final List<E> insertionList;

	private int[] indexes = new int[0];

	private IndexedList(final List<E> orderedList, final List<E> insertionList) {
		this.orderedList   = orderedList;
		this.orderedView   = Collections.unmodifiableList(orderedList);
		this.reversedList  = Collections.unmodifiableList(Lists.reverse(orderedList));
		this.insertionList = insertionList;
	}

	public static <E extends IIndexedElement> @NonNull IndexedList<E> linked() {
		return new IndexedList<>(new LinkedList<>(), new LinkedList<>());
	}

	public static <E extends IIndexedElement> @NonNull IndexedList<E> concurrent() {
		return new IndexedList<>(new CopyOnWriteArrayList<>(), new CopyOnWriteArrayList<>());
	}

	public void add(final E element) {
		this.sort();
		if (this.orderedList.contains(element)) {
			return;
		}

		this.insertionList.add(element);
		this.insert(element);
		this.snapshot();
	}

	public void remove(final E element) {
		this.sort();
		this.orderedList.remove(element);
		this.insertionList.remove(element);
		this.snapshot();
	}

	public void clear() {
		this.orderedList.clear();
		this.insertionList.clear();
		this.indexes = new int[0];
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

	private void insert(final E element) {
		for (int i = 0; i < this.orderedList.size(); i++) {
			if (this.orderedList.get(i).getIndex() > element.getIndex()) {
				this.orderedList.add(i, element);
				return;
			}
		}

		this.orderedList.add(element);
	}

	private void sort() {
		final int[] indexes = this.indexes;
		int position = 0;
		for (final E element : this.orderedList) {
			if (position >= indexes.length || element.getIndex() != indexes[position++]) {
				final Map<E, Integer> rankMap = new IdentityHashMap<>();
				for (final E inserted : this.insertionList) {
					rankMap.put(inserted, rankMap.size());
				}

				this.orderedList.sort(Comparator.<E>comparingInt(IIndexedElement::getIndex).thenComparingInt(sorted -> rankMap.getOrDefault(sorted, Integer.MAX_VALUE)));
				this.snapshot();
				return;
			}
		}
	}

	private void snapshot() {
		final int[] indexes = this.indexes.length == this.orderedList.size() ? this.indexes : new int[this.orderedList.size()];
		int position = 0;
		for (final E element : this.orderedList) {
			if (position < indexes.length) {
				indexes[position++] = element.getIndex();
			}
		}

		this.indexes = indexes;
	}

}