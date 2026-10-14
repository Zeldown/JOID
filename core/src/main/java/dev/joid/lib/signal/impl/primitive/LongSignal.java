package dev.joid.lib.signal.impl.primitive;

import dev.joid.lib.signal.Signal;
import lombok.NonNull;

public class LongSignal extends Signal<Long> {

	public LongSignal() {
		super(0L);
	}

	public LongSignal(final long value) {
		super(value);
	}

	public @NonNull LongSignal decrement() {
		this.transform(current -> current - 1);
		return this;
	}

	public @NonNull LongSignal increment() {
		this.transform(current -> current + 1);
		return this;
	}

	public @NonNull LongSignal add(final long value) {
		this.transform(current -> current + value);
		return this;
	}

	public @NonNull LongSignal subtract(final long value) {
		this.transform(current -> current - value);
		return this;
	}

	public @NonNull LongSignal multiply(final long value) {
		this.transform(current -> current * value);
		return this;
	}

	public @NonNull LongSignal divide(final long value) {
		if (value == 0) {
			throw new IllegalArgumentException("The LongSignal of value " + this.peek() + " cannot be divided by 0, check the divisor before calling divide(...)");
		}

		this.transform(current -> current / value);
		return this;
	}

	public @NonNull LongSignal power(final int exponent) {
		this.transform(current -> LongSignal.raise(current, exponent));
		return this;
	}

	private static long raise(final long value, final int exponent) {
		if (exponent < 0) {
			return (long) Math.pow(value, exponent);
		}

		long result = 1L;
		long base = value;
		for (int remaining = exponent; remaining > 0; remaining >>= 1) {
			if ((remaining & 1) == 1) {
				result *= base;
			}
			base *= base;
		}
		return result;
	}

	@Override
	public String toString() {
		return this.peek() == null ? "LongSignal{null}" : "LongSignal{" + this.peek().toString() + "}";
	}

}