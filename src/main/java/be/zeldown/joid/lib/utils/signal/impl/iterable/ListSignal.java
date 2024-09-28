package be.zeldown.joid.lib.utils.signal.impl.iterable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import be.zeldown.joid.lib.utils.signal.Signal;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ListSignal<E> extends Signal<List<E>> {

	public ListSignal(final Collection<E> value) {
		this(new ArrayList<>(value));
	}

	public static <E> ListSignal<E> of(final List<E> defaultValue) {
		final ListSignal<E> instance = new ListSignal<>();
		instance.set(defaultValue);
		return instance;
	}

	public ListSignal(final List<E> value) {
		super(value);
	}

	public boolean add(final E e) {
		final boolean success = this.getOrDefault().add(e);
		this.publish();
		return success;
	}

	public void clear() {
		this.getOrDefault().clear();
		this.publish();
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

	public boolean remove(final E e) {
		final boolean success = this.getOrDefault().remove(e);
		this.publish();
		return success;
	}

	public E remove(final int index) {
		final E result = this.getOrDefault().remove(index);
		this.publish();
		return result;
	}

	public E set(final int index, final E element) {
		final E result = this.getOrDefault().set(index, element);
		this.publish();
		return result;
	}

	public int size() {
		return this.getOrDefault().size();
	}

}