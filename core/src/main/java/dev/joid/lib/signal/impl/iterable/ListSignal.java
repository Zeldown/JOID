package dev.joid.lib.signal.impl.iterable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import dev.joid.lib.signal.Signal;
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
		final List<E> list = this.mutable();
		final boolean changed = !list.isEmpty();
		list.clear();
		this.publishIf(changed);
		return this;
	}

	public boolean add(final E e) {
		final boolean success = this.mutable().add(e);
		this.publishIf(success);
		return success;
	}

	public boolean remove(final E e) {
		final boolean success = this.mutable().remove(e);
		this.publishIf(success);
		return success;
	}

	public E remove(final int index) {
		final E result = this.mutable().remove(index);
		this.publish();
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
		final E result = this.mutable().set(index, element);
		this.publishIf(!Objects.equals(result, element));
		return result;
	}

	public int size() {
		return this.get().size();
	}

	private List<E> mutable() {
		if (!this.isPresent()) {
			this.assign(this.peek() == null ? new ArrayList<>() : new ArrayList<>(this.peek()));
		}

		return this.peek();
	}

	@Override
	public String toString() {
		return this.peek() == null ? "ListSignal{null}" : "ListSignal{" + this.peek().toString() + "}";
	}

}