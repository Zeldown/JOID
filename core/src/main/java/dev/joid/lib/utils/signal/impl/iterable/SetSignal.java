package dev.joid.lib.utils.signal.impl.iterable;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import dev.joid.lib.utils.signal.Signal;
import lombok.NonNull;

public class SetSignal<E> extends Signal<Set<E>> {

	public SetSignal() {}

	public SetSignal(final Set<E> value) {
		super(value);
	}

	public SetSignal(final Collection<E> value) {
		this(new HashSet<>(value));
	}

	public static <E> SetSignal<E> of(final Set<E> defaultValue) {
		final SetSignal<E> instance = new SetSignal<>();
		instance.set(defaultValue);
		return instance;
	}

	public @NonNull SetSignal<E> clear() {
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

	public boolean contains(final E e) {
		return this.get().contains(e);
	}

	public boolean isEmpty() {
		return this.get().isEmpty();
	}

	public int size() {
		return this.get().size();
	}

	private Set<E> mutable() {
		if (!this.isPresent()) {
			this.silent().set(this.peek() == null ? new LinkedHashSet<>() : new LinkedHashSet<>(this.peek()));
		}

		return this.peek();
	}

	@Override
	public String toString() {
		return this.peek() == null ? "SetSignal{null}" : "SetSignal{" + this.peek().toString() + "}";
	}

}