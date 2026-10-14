package dev.joid.lib.signal.impl.iterable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import dev.joid.lib.signal.Signal;
import lombok.NonNull;

public class SetSignal<E> extends Signal<Set<E>> {

	public SetSignal() {
		super(new LinkedHashSet<>());
	}

	public SetSignal(final @NonNull Collection<E> value) {
		super(new LinkedHashSet<>(value));
	}

	public @NonNull SetSignal<E> clear() {
		final Set<E> set = new LinkedHashSet<>();
		this.commit(set, !this.peek().isEmpty());
		return this;
	}

	public boolean add(final E e) {
		final Set<E> set = this.copy();
		final boolean success = set.add(e);
		this.commit(set, success);
		return success;
	}

	public boolean remove(final E e) {
		final Set<E> set = this.copy();
		final boolean success = set.remove(e);
		this.commit(set, success);
		return success;
	}

	public boolean contains(final E e) {
		return this.get().contains(e);
	}

	public boolean isEmpty() {
		return this.get().isEmpty();
	}

	public int size() {
		return this.get().size();
	}

	private Set<E> copy() {
		return new LinkedHashSet<>(this.peek());
	}

	private void commit(final Set<E> set, final boolean changed) {
		if (changed) {
			this.set(set);
		} else {
			this.publishIf(false);
		}
	}

	@Override
	public String toString() {
		return "SetSignal{" + this.peek() + "}";
	}

}