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

	public @NonNull LongSignal increment() {
		return super.transform(current -> current + 1);
	}

	public @NonNull LongSignal decrement() {
		return super.transform(current -> current - 1);
	}

	public @NonNull LongSignal add(final long value) {
		return super.transform(current -> current + value);
	}

	public @NonNull LongSignal subtract(final long value) {
		return super.transform(current -> current - value);
	}

	public @NonNull LongSignal multiply(final long value) {
		return super.transform(current -> current * value);
	}

	public @NonNull LongSignal divide(final long value) {
		if (value == 0) {
			throw new IllegalArgumentException("The LongSignal of value " + super.peek() + " cannot be divided by 0, check the divisor before calling divide(...)");
		}

		return super.transform(current -> current / value);
	}

}