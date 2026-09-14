package be.zeldown.joid.lib.utils.list;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.Lists;

import lombok.NonNull;

public class IndexedConcurrentList<E extends IndexedElement> implements IndexedList<E> {

	private final List<E> orderedList;
	private final List<E> reversedList;

	public IndexedConcurrentList() {
		this.orderedList = new CopyOnWriteArrayList<>();
		this.reversedList = Lists.reverse(this.orderedList);
	}

	public IndexedConcurrentList(final List<E> list) {
		this.orderedList = new CopyOnWriteArrayList<>(list);
		this.reversedList = Lists.reverse(this.orderedList);
	}

	@Override
	public void add(final E element) {
		for (int i = 0; i < this.orderedList.size(); i++) {
			if (this.orderedList.get(i).getIndex() > element.getIndex()) {
				this.orderedList.add(i, element);
				break;
			}
		}

		if (!this.orderedList.contains(element)) {
			this.orderedList.add(element);
		}
	}

	@Override
	public void remove(final E element) {
		this.orderedList.remove(element);
		this.reversedList.remove(element);
	}

	@Override
	public void clear() {
		this.orderedList.clear();
		this.reversedList.clear();
	}

	@Override
	public @NonNull IndexedConcurrentList<E> copy() {
		return new IndexedConcurrentList<>(this.orderedList);
	}

	/* [ Getter Methods ] */
	@Override
	public int size() {
		return Math.min(this.orderedList.size(), this.reversedList.size());
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
	public E get(final int index) {
		return this.orderedList.get(index);
	}

	@Override
	public E getFirst() {
		return this.orderedList.get(0);
	}

	@Override
	public E getLast() {
		if (this.isEmpty()) {
			return null;
		}

		return this.orderedList.get(this.orderedList.size() - 1);
	}

	@Override
	public @NonNull List<E> ordered() {
		return this.orderedList;
	}

	@Override
	public @NonNull List<E> reversed() {
		return this.reversedList;
	}

	@Override
	public @NonNull IndexedConcurrentList<E> recursive() {
		if (this.isEmpty()) {
			return this;
		}

		final E first = this.orderedList.get(0);
		if (!(first instanceof RecursiveIndexedElement)) {
			return this;
		}

		final IndexedConcurrentList<E> list = new IndexedConcurrentList<>();
		for (final E element : this) {
			list.orderedList.addAll(this.recursive((RecursiveIndexedElement) element));
		}

		return list;
	}

	@SuppressWarnings("unchecked")
	private @NonNull List<E> recursive(final RecursiveIndexedElement element) {
		final List<E> list = new LinkedList<>();
		list.add((E) element);

		if (!(element instanceof RecursiveIndexedElement)) {
			return list;
		}

		for (final RecursiveIndexedElement child : element.getChildren()) {
			list.addAll(this.recursive(child));
		}

		return list;
	}

	@Override
	public Iterator<E> iterator() {
		return this.orderedList.iterator();
	}

}