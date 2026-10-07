package dev.joid.lib.ui.node.impl.design.textfield.impl;

import java.math.BigInteger;
import java.util.function.Supplier;

import dev.joid.lib.ui.node.impl.design.textfield.LineFieldNode;
import dev.joid.lib.utils.signal.Signal;
import lombok.NonNull;

public class IntegerFieldNode extends LineFieldNode<Integer> {

	private int step     = 1;
	private int maxValue = Integer.MAX_VALUE;
	private int minValue = Integer.MIN_VALUE;

	protected IntegerFieldNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.fallback(0);
	}

	public static @NonNull IntegerFieldNode create(final double x, final double y, final double width) {
		return new IntegerFieldNode(x, y, width, 0);
	}

	public static @NonNull IntegerFieldNode create(final double x, final double y, final double width, final double height) {
		return new IntegerFieldNode(x, y, width, height);
	}

	@Override
	protected final Integer parse(final @NonNull String text) {
		if (!text.matches("-?[0-9]+")) {
			return null;
		}

		final BigInteger value = new BigInteger(text);
		if (value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
			return Integer.MAX_VALUE;
		}

		if (value.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0) {
			return Integer.MIN_VALUE;
		}

		return value.intValue();
	}

	@Override
	protected final @NonNull String format(final @NonNull Integer value) {
		return Integer.toString(value);
	}

	@Override
	protected final Integer correct(final @NonNull Integer value) {
		return Math.max(this.minValue, Math.min(this.maxValue, value));
	}

	@Override
	protected final Integer increment(final @NonNull Integer value, final int count) {
		return (int) Math.max(this.minValue, Math.min(this.maxValue, (long) value + (long) this.step * count));
	}

	@Override
	protected final boolean accepts(final @NonNull String text) {
		return text.equals("-") ? this.minValue < 0 : text.matches("-?[0-9]*");
	}

	public final <T extends IntegerFieldNode> @NonNull T max(final int maxValue) {
		return this.max(Signal.from(maxValue));
	}

	public final <T extends IntegerFieldNode> @NonNull T max(final @NonNull Supplier<Integer> maxValue) {
		return super.follow("maxValue", maxValue, value -> {
			this.maxValue = value;
			super.revalidate();
		});
	}

	public final <T extends IntegerFieldNode> @NonNull T min(final int minValue) {
		return this.min(Signal.from(minValue));
	}

	public final <T extends IntegerFieldNode> @NonNull T min(final @NonNull Supplier<Integer> minValue) {
		return super.follow("minValue", minValue, value -> {
			this.minValue = value;
			super.revalidate();
		});
	}

	public final <T extends IntegerFieldNode> @NonNull T step(final int step) {
		return this.step(Signal.from(step));
	}

	public final <T extends IntegerFieldNode> @NonNull T step(final @NonNull Supplier<Integer> step) {
		return super.follow("step", step, value -> this.step = value);
	}

	public final <T extends IntegerFieldNode> @NonNull T value(final int value) {
		return this.value(Signal.from(value));
	}

	public final <T extends IntegerFieldNode> @NonNull T value(final @NonNull Supplier<Integer> value) {
		return super.follow("value", value, number -> super.write(number));
	}

}