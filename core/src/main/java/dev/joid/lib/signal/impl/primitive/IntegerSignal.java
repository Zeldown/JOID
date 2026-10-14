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
		this.transform(current -> current - 1);
		return this;
	}

	public @NonNull IntegerSignal increment() {
		this.transform(current -> current + 1);
		return this;
	}

	public @NonNull IntegerSignal add(final int value) {
		this.transform(current -> current + value);
		return this;
	}

	public @NonNull IntegerSignal subtract(final int value) {
		this.transform(current -> current - value);
		return this;
	}

	public @NonNull IntegerSignal multiply(final int value) {
		this.transform(current -> current * value);
		return this;
	}

	public @NonNull IntegerSignal divide(final int value) {
		if (value == 0) {
			throw new IllegalArgumentException("The IntegerSignal of value " + this.peek() + " cannot be divided by 0, check the divisor before calling divide(...)");
		}

		this.transform(current -> current / value);
		return this;
	}

}