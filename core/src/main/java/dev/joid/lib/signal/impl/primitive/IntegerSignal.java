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

	public static @NonNull IntegerSignal of(final int defaultValue) {
		final IntegerSignal instance = new IntegerSignal(defaultValue);
		instance.set(defaultValue);
		return instance;
	}

	public @NonNull IntegerSignal decrement() {
		final int updatedValue = this.peek() - 1;
		this.set(updatedValue);
		return this;
	}

	public @NonNull IntegerSignal increment() {
		final int updatedValue = this.peek() + 1;
		this.set(updatedValue);
		return this;
	}

	public @NonNull IntegerSignal add(final int value) {
		final int updatedValue = this.peek() + value;
		this.set(updatedValue);
		return this;
	}

	public @NonNull IntegerSignal subtract(final int value) {
		final int updatedValue = this.peek() - value;
		this.set(updatedValue);
		return this;
	}

	public @NonNull IntegerSignal multiply(final int value) {
		final int updatedValue = this.peek() * value;
		this.set(updatedValue);
		return this;
	}

	public @NonNull IntegerSignal divide(final int value) {
		if (value == 0) {
			throw new ArithmeticException("Division by zero");
		}

		final int updatedValue = this.peek() / value;
		this.set(updatedValue);
		return this;
	}

	public @NonNull IntegerSignal power(final int exponent) {
		final int updatedValue = (int) Math.pow(this.peek(), exponent);
		this.set(updatedValue);
		return this;
	}

	@Override
	public String toString() {
		return this.peek() == null ? "IntegerSignal{null}" : "IntegerSignal{" + this.peek().toString() + "}";
	}

}