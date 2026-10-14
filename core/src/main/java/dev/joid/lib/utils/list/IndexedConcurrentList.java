package dev.joid.lib.utils.list;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.Lists;

import lombok.NonNull;

public class IndexedConcurrentList<E extends IndexedElement> implements IIndexedList<E> {

	private final List<E> orderedList;
	private final List<E> orderedView;
	private final List<E> reversedList;

	public IndexedConcurrentList() {
		this.orderedList = new CopyOnWriteArrayList<>();
		this.orderedView = Collections.unmodifiableList(this.orderedList);
		this.reversedList = Collections.unmodifiableList(Lists.reverse(this.orderedList));
	}

	public IndexedConcurrentList(final List<E> list) {
		this.orderedList = new CopyOnWriteArrayList<>(list);
		this.orderedView = Collections.unmodifiableList(this.orderedList);
		this.reversedList = Collections.unmodifiableList(Lists.reverse(this.orderedList));
	}

	@Override
	public void add(final E element) {
		this.sort();
		final int position = this.orderedList.indexOf(element);
		if (position >= 0 && this.isInPlace(position)) {
			return;
		}

		this.orderedList.remove(element);
		for (int i = 0; i < this.orderedList.size(); i++) {
			if (this.orderedList.get(i).getIndex() > element.getIndex()) {
				this.orderedList.add(i, element);
				return;
			}
		}

		this.orderedList.add(element);
	}

	@Override
	public void remove(final E element) {
		this.orderedList.remove(element);
	}

	@Override
	public void sort() {
		int index = Integer.MIN_VALUE;
		for (final E element : this.orderedList) {
			if (element.getIndex() < index) {
				this.orderedList.sort(Comparator.comparingInt(IndexedElement::getIndex));
				return;
			}

			index = element.getIndex();
		}
	}

	@Override
	public void clear() {
		this.orderedList.clear();
	}

	@Override
	public @NonNull IndexedConcurrentList<E> copy() {
		return new IndexedConcurrentList<>(this.orderedList);
	}

	@Override
	public int size() {
		return this.orderedList.size();
	}

	@Override
	public boolean isEmpty() {
		return this.orderedList.isEmpty();
	}

	@Override
	public boolean contains(final E element) {
		return this.orderedList.contains(element);
	}

	@Override
	public E getLast() {
		this.sort();
		if (this.isEmpty()) {
			return null;
		}

		return this.orderedList.get(this.orderedList.size() - 1);
	}

	@Override
	public E getFirst() {
		this.sort();
		if (this.isEmpty()) {
			return null;
		}

		return this.orderedList.get(0);
	}

	@Override
	public E get(final int index) {
		this.sort();
		return this.orderedList.get(index);
	}

	@Override
	public @NonNull List<E> ordered() {
		this.sort();
		return this.orderedView;
	}

	@Override
	public @NonNull List<E> reversed() {
		this.sort();
		return this.reversedList;
	}

	@Override
	public Iterator<E> iterator() {
		this.sort();
		return this.orderedList.iterator();
	}

	private boolean isInPlace(final int position) {
		final int index = this.orderedList.get(position).getIndex();
		return (position == 0 || this.orderedList.get(position - 1).getIndex() <= index) && (position == this.orderedList.size() - 1 || this.orderedList.get(position + 1).getIndex() >= index);
	}

}