package dev.joid.lib.signal.impl.iterable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

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
		this.mutate(set -> {
			final boolean changed = !set.isEmpty();
			set.clear();
			return changed;
		}, Boolean::booleanValue);
		return this;
	}

	public boolean add(final E e) {
		return this.mutate(set -> set.add(e), Boolean::booleanValue);
	}

	public boolean remove(final E e) {
		return this.mutate(set -> set.remove(e), Boolean::booleanValue);
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

	private <R> R mutate(final Function<Set<E>, R> mutation, final Predicate<R> changed) {
		final Set<E> base = this.peek();
		final Set<E> set = new LinkedHashSet<>(base);
		final R result = mutation.apply(set);
		this.write(() -> {
			if (this.peek() == base) {
				this.commit(set, changed.test(result));
			} else {
				this.mutate(mutation, changed);
			}
		});
		return result;
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