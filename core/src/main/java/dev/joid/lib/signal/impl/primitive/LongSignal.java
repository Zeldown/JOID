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

	public static @NonNull LongSignal of(final long defaultValue) {
		final LongSignal instance = new LongSignal(defaultValue);
		instance.set(defaultValue);
		return instance;
	}

	public @NonNull LongSignal decrement() {
		final long updatedValue = this.peek() - 1;
		this.set(updatedValue);
		return this;
	}

	public @NonNull LongSignal increment() {
		final long updatedValue = this.peek() + 1;
		this.set(updatedValue);
		return this;
	}

	public @NonNull LongSignal add(final long value) {
		final long updatedValue = this.peek() + value;
		this.set(updatedValue);
		return this;
	}

	public @NonNull LongSignal subtract(final long value) {
		final long updatedValue = this.peek() - value;
		this.set(updatedValue);
		return this;
	}

	public @NonNull LongSignal multiply(final long value) {
		final long updatedValue = this.peek() * value;
		this.set(updatedValue);
		return this;
	}

	public @NonNull LongSignal divide(final long value) {
		if (value == 0) {
			throw new ArithmeticException("Division by zero");
		}

		final long updatedValue = this.peek() / value;
		this.set(updatedValue);
		return this;
	}

	public @NonNull LongSignal power(final int exponent) {
		if (exponent < 0) {
			this.set((long) Math.pow(this.peek(), exponent));
			return this;
		}

		long result = 1L;
		long base = this.peek();
		for (int remaining = exponent; remaining > 0; remaining >>= 1) {
			if ((remaining & 1) == 1) {
				result *= base;
			}
			base *= base;
		}
		this.set(result);
		return this;
	}

	@Override
	public String toString() {
		return this.peek() == null ? "LongSignal{null}" : "LongSignal{" + this.peek().toString() + "}";
	}

}