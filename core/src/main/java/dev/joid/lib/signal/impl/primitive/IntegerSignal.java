package dev.joid.lib.signal.impl.primitive;

import dev.joid.lib.signal.Signal;
import lombok.NonNull;

public class IntegerSignal extends Signal<Integer> {

	public IntegerSignal() {
		super(0);
	}

	public IntegerSignal(final int value) {
		super(value);
	}

	public @NonNull IntegerSignal decrement() {
		super.transform(current -> current - 1);
		return this;
	}

	public @NonNull IntegerSignal increment() {
		super.transform(current -> current + 1);
		return this;
	}

	public @NonNull IntegerSignal add(final int value) {
		super.transform(current -> current + value);
		return this;
	}

	public @NonNull IntegerSignal subtract(final int value) {
		super.transform(current -> current - value);
		return this;
	}

	public @NonNull IntegerSignal multiply(final int value) {
		super.transform(current -> current * value);
		return this;
	}

	public @NonNull IntegerSignal divide(final int value) {
		if (value == 0) {
			throw new IllegalArgumentException("The IntegerSignal of value " + super.peek() + " cannot be divided by 0, check the divisor before calling divide(...)");
		}

		super.transform(current -> current / value);
		return this;
	}

}