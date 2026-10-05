package dev.joid.lib.utils.signal.impl.iterable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dev.joid.lib.utils.signal.Signal;
import lombok.NonNull;

public class ListSignal<E> extends Signal<List<E>> {

	public ListSignal() {}

	public ListSignal(final List<E> value) {
		super(value);
	}

	public ListSignal(final Collection<E> value) {
		this(new ArrayList<>(value));
	}

	public static <E> ListSignal<E> of(final List<E> defaultValue) {
		final ListSignal<E> instance = new ListSignal<>();
		instance.set(defaultValue);
		return instance;
	}

	public @NonNull ListSignal<E> clear() {
		this.mutable().clear();
		this.publish();
		return this;
	}

	public boolean add(final E e) {
		final boolean success = this.mutable().add(e);
		this.publish();
		return success;
	}

	public boolean remove(final E e) {
		final boolean success = this.mutable().remove(e);
		this.publish();
		return success;
	}

	public E remove(final int index) {
		final E result = this.mutable().remove(index);
		this.publish();
		return result;
	}

	public boolean contains(final E e) {
		return this.getOrDefault().contains(e);
	}

	public E get(final int index) {
		return this.getOrDefault().get(index);
	}

	public int indexOf(final E e) {
		return this.getOrDefault().indexOf(e);
	}

	public boolean isEmpty() {
		return this.getOrDefault().isEmpty();
	}

	public E set(final int index, final E element) {
		final E result = this.mutable().set(index, element);
		this.publish();
		return result;
	}

	public int size() {
		return this.getOrDefault().size();
	}

	private List<E> mutable() {
		if (!this.isPresent()) {
			this.silent().set(this.getOrDefault() == null ? new ArrayList<>() : new ArrayList<>(this.getOrDefault()));
		}

		return this.getOrDefault();
	}

	@Override
	public String toString() {
		return this.getOrDefault() == null ? "ListSignal{null}" : "ListSignal{" + this.getOrDefault().toString() + "}";
	}

}