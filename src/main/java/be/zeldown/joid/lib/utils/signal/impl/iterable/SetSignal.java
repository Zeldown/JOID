package be.zeldown.joid.lib.utils.signal.impl.iterable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import be.zeldown.joid.lib.utils.signal.Signal;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor
public class SetSignal<E> extends Signal<Set<E>> {

	public SetSignal(final Collection<E> value) {
		this(new HashSet<>(value));
	}

	public SetSignal(final Set<E> value) {
		super(value);
	}

	public static <E> @NonNull SetSignal<E> of(final Set<E> defaultValue) {
		final SetSignal<E> instance = new SetSignal<>();
		instance.set(defaultValue);
		return instance;
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

	public boolean contains(final @NonNull E e) {
		return this.getOrDefault().contains(e);
	}

	public boolean isEmpty() {
		return this.getOrDefault().isEmpty();
	}

	public boolean remove(final @NonNull E e) {
		final boolean success = this.getOrDefault().remove(e);
		this.publish();
		return success;
	}

	public int size() {
		return this.getOrDefault().size();
	}

}