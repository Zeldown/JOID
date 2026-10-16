package dev.joid.lib.ui.node.impl.control.field;

import java.math.BigInteger;
import java.util.function.Supplier;

import dev.joid.lib.signal.Signal;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class IntegerFieldNode extends LineFieldNode<Integer> {

	private int max;
	private int min;
	private int step;

	protected IntegerFieldNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.max  = Integer.MAX_VALUE;
		this.min  = Integer.MIN_VALUE;
		this.step = 1;

		super.fallback(0);
	}

	public static @NonNull IntegerFieldNode create(final double x, final double y, final double width) {
		return new IntegerFieldNode(x, y, width, 0D);
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
	protected final boolean accepts(final @NonNull String text) {
		return text.matches(this.min < 0 ? "-?[0-9]*" : "[0-9]*");
	}

	@Override
	protected final Integer correct(final @NonNull Integer value) {
		return Math.max(this.min, Math.min(this.max, value));
	}

	@Override
	protected final Integer increment(final @NonNull Integer value, final int count) {
		return (int) Math.max(this.min, Math.min(this.max, (long) value + (long) this.step * count));
	}

	public final <T extends IntegerFieldNode> @NonNull T max(final int max) {
		return this.max(Signal.from(max));
	}

	public final <T extends IntegerFieldNode> @NonNull T max(final @NonNull Supplier<Integer> max) {
		return super.follow("max", max, value -> {
			this.max = value;
			this.revalidate();
		});
	}

	public final <T extends IntegerFieldNode> @NonNull T min(final int min) {
		return this.min(Signal.from(min));
	}

	public final <T extends IntegerFieldNode> @NonNull T min(final @NonNull Supplier<Integer> min) {
		return super.follow("min", min, value -> {
			this.min = value;
			this.revalidate();
		});
	}

	public final <T extends IntegerFieldNode> @NonNull T step(final int step) {
		return this.step(Signal.from(step));
	}

	public final <T extends IntegerFieldNode> @NonNull T step(final @NonNull Supplier<Integer> step) {
		return super.follow("step", step, value -> this.step = value);
	}

	public final <T extends IntegerFieldNode> @NonNull T value(final int value) {
		return super.startValue(value);
	}

	public final <T extends IntegerFieldNode> @NonNull T value(final @NonNull Signal<Integer> value) {
		return super.bindValue(value);
	}

	public final <T extends IntegerFieldNode> @NonNull T change(final int value) {
		super.write(value, true);
		return (T) this;
	}

}