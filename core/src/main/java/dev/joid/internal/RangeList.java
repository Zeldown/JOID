package dev.joid.internal;

import java.util.AbstractList;
import java.util.RandomAccess;

public abstract class RangeList<E> extends AbstractList<E> implements RandomAccess {

	public abstract E nearest(final E value);

	@Override
	public int lastIndexOf(final Object value) {
		return this.indexOf(value);
	}

	@Override
	public boolean contains(final Object value) {
		return this.indexOf(value) >= 0;
	}

	@Override
	public String toString() {
		return this.size() <= 10 ? super.toString() : "[" + this.get(0) + ", " + this.get(1) + ", ..., " + this.get(this.size() - 1) + "]";
	}

}