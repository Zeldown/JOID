package dev.joid.lib.ui.node.impl.control.slider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

import dev.joid.internal.RangeList;
import dev.joid.lib.signal.Signal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

public abstract class DoubleSliderNode extends SliderNode<Double> {

	private DoubleRangeList range;

	protected DoubleSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public final <T extends DoubleSliderNode> @NonNull T range(final double min, final double max, final double step) {
		if (step <= 0D) {
			throw new IllegalArgumentException("The step " + step + " of the slider range must be positive, pass a step above 0");
		}

		if (min > max) {
			throw new IllegalArgumentException("The min " + min + " of the slider range is above its max " + max + ", pass a min of at most " + max);
		}

		final BigDecimal start = BigDecimal.valueOf(min);
		final BigDecimal by = BigDecimal.valueOf(step);
		final BigDecimal steps = BigDecimal.valueOf(max).subtract(start).divide(by, 0, RoundingMode.FLOOR);
		if (steps.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) >= 0) {
			throw new IllegalArgumentException("The slider range from " + min + " to " + max + " by " + step + " holds more than " + Integer.MAX_VALUE + " values, pass a narrower range or a larger step");
		}

		int size = steps.intValue() + 1;
		while (size < Integer.MAX_VALUE && DoubleRangeList.valueAt(start, by, size) <= max) {
			size++;
		}
		while (size > 1 && DoubleRangeList.valueAt(start, by, size - 1) > max) {
			size--;
		}

		this.range = new DoubleRangeList(size, start, by);
		return super.values(Signal.<List<Double>>from(this.range));
	}

	@Override
	protected Double fallback(final Double value) {
		if (value == null) {
			return super.fallback(value);
		}

		final List<Double> values = super.getValueList();
		if (this.range != null && values.equals(this.range)) {
			return this.range.nearest(value);
		}

		Double nearest = null;
		for (final Double option : values) {
			final double distance = Math.abs(option - value);
			if (nearest == null || distance < Math.abs(nearest - value) || distance == Math.abs(nearest - value) && option < nearest) {
				nearest = option;
			}
		}
		return nearest;
	}

	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class DoubleRangeList extends RangeList<Double> {

		private final int        size;
		private final BigDecimal min;
		private final BigDecimal step;

		@Override
		public int size() {
			return this.size;
		}

		@Override
		public Double get(final int index) {
			if (index < 0 || index >= this.size) {
				throw new IndexOutOfBoundsException("The index " + index + " is out of the " + this.size + " values of the slider range");
			}

			return DoubleRangeList.valueAt(this.min, this.step, index);
		}

		@Override
		public int indexOf(final Object value) {
			if (!(value instanceof Double) || ((Double) value).isNaN() || ((Double) value).isInfinite()) {
				return -1;
			}

			final long index = BigDecimal.valueOf((Double) value).subtract(this.min).divide(this.step, 0, RoundingMode.HALF_DOWN).longValue();
			for (long candidate = Math.max(0L, index - 1L); candidate <= Math.min(this.size - 1L, index + 1L); candidate++) {
				if (this.get((int) candidate).equals(value)) {
					return (int) candidate;
				}
			}
			return -1;
		}

		@Override
		public Double nearest(final Double value) {
			if (!(value > this.get(0))) {
				return this.get(0);
			}

			if (value >= this.get(this.size - 1)) {
				return this.get(this.size - 1);
			}

			return this.get(BigDecimal.valueOf(value).subtract(this.min).divide(this.step, 0, RoundingMode.HALF_DOWN).intValue());
		}

		private static double valueAt(final BigDecimal min, final BigDecimal step, final int index) {
			return min.add(step.multiply(BigDecimal.valueOf(index))).doubleValue();
		}

		@Override
		public boolean equals(final Object object) {
			if (!(object instanceof DoubleRangeList)) {
				return super.equals(object);
			}

			final DoubleRangeList range = (DoubleRangeList) object;
			return this.min.equals(range.min) && this.size == range.size && (this.size == 1 || this.step.equals(range.step));
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.min, this.size, this.size == 1 ? BigDecimal.ZERO : this.step);
		}

	}

}