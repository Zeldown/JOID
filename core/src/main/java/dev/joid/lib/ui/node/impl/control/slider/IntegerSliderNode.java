package dev.joid.lib.ui.node.impl.control.slider;

import java.util.List;
import java.util.Objects;

import dev.joid.internal.RangeList;
import dev.joid.lib.signal.Signal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

public abstract class IntegerSliderNode extends SliderNode<Integer> {

	private IntegerRangeList range;

	protected IntegerSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public final <T extends IntegerSliderNode> @NonNull T range(final int min, final int max) {
		return this.range(min, max, 1);
	}

	public final <T extends IntegerSliderNode> @NonNull T range(final int min, final int max, final int step) {
		if (step <= 0) {
			throw new IllegalArgumentException("The step " + step + " of the slider range must be positive, pass a step of 1 or more");
		}

		if (min > max) {
			throw new IllegalArgumentException("The min " + min + " of the slider range is above its max " + max + ", pass a min of at most " + max);
		}

		final long size = ((long) max - min) / step + 1L;
		if (size > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("The slider range from " + min + " to " + max + " by " + step + " holds more than " + Integer.MAX_VALUE + " values, pass a narrower range or a larger step");
		}

		this.range = new IntegerRangeList(min, step, (int) size);
		return super.values(Signal.<List<Integer>>from(this.range));
	}

	@Override
	protected Integer fallback(final Integer value) {
		if (value == null) {
			return super.fallback(value);
		}

		final List<Integer> values = super.getValueList();
		if (this.range != null && values.equals(this.range)) {
			return this.range.nearest(value);
		}

		Integer nearest = null;
		for (final Integer option : values) {
			final long distance = Math.abs((long) option - value);
			if (nearest == null || distance < Math.abs((long) nearest - value) || distance == Math.abs((long) nearest - value) && option < nearest) {
				nearest = option;
			}
		}
		return nearest;
	}

	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class IntegerRangeList extends RangeList<Integer> {

		private final int min;
		private final int step;
		private final int size;

		@Override
		public int size() {
			return this.size;
		}

		@Override
		public Integer get(final int index) {
			if (index < 0 || index >= this.size) {
				throw new IndexOutOfBoundsException("The index " + index + " is out of the " + this.size + " values of the slider range");
			}

			return (int) (this.min + (long) index * this.step);
		}

		@Override
		public int indexOf(final Object value) {
			if (!(value instanceof Integer)) {
				return -1;
			}

			final long offset = (long) (Integer) value - this.min;
			return offset < 0L || offset % this.step != 0L || offset / this.step >= this.size ? -1 : (int) (offset / this.step);
		}

		@Override
		public Integer nearest(final Integer value) {
			final long offset = Math.max(0L, Math.min((long) value - this.min, (long) (this.size - 1) * this.step));
			return this.get((int) ((offset * 2L + this.step - 1L) / (this.step * 2L)));
		}

		@Override
		public boolean equals(final Object object) {
			if (!(object instanceof IntegerRangeList)) {
				return super.equals(object);
			}

			final IntegerRangeList range = (IntegerRangeList) object;
			return this.min == range.min && this.size == range.size && (this.size == 1 || this.step == range.step);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.min, this.size, this.size == 1 ? 0 : this.step);
		}

	}

}