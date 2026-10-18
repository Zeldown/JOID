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
		return super.transform(current -> current - 1);
	}

	public @NonNull IntegerSignal increment() {
		return super.transform(current -> current + 1);
	}

	public @NonNull IntegerSignal add(final int value) {
		return super.transform(current -> current + value);
	}

	public @NonNull IntegerSignal subtract(final int value) {
		return super.transform(current -> current - value);
	}

	public @NonNull IntegerSignal multiply(final int value) {
		return super.transform(current -> current * value);
	}

	public @NonNull IntegerSignal divide(final int value) {
		if (value == 0) {
			throw new IllegalArgumentException("The IntegerSignal of value " + super.peek() + " cannot be divided by 0, check the divisor before calling divide(...)");
		}

		return super.transform(current -> current / value);
	}

}