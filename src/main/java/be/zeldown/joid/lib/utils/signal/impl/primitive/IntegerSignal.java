package be.zeldown.joid.lib.utils.signal.impl.primitive;

import be.zeldown.joid.lib.utils.signal.Signal;

public class IntegerSignal extends Signal<Integer> {

	public IntegerSignal() {
		super(0);
	}

	public IntegerSignal(final int value) {
		super(value);
	}

	public static IntegerSignal of(final int defaultValue) {
		final IntegerSignal instance = new IntegerSignal();
		instance.set(defaultValue);
		return instance;
	}

	public void increment() {
		final int updatedValue = this.getOrDefault() + 1;
		this.set(updatedValue);
	}

	public void decrement() {
		final int updatedValue = this.getOrDefault() - 1;
		this.set(updatedValue);
	}

	public void add(final int value) {
		final int updatedValue = this.getOrDefault() + value;
		this.set(updatedValue);
	}

	public void subtract(final int value) {
		final int updatedValue = this.getOrDefault() - value;
		this.set(updatedValue);
	}

	public void multiply(final int value) {
		final int updatedValue = this.getOrDefault() * value;
		this.set(updatedValue);
	}

	public void divide(final int value) {
		if (value == 0) {
			throw new ArithmeticException("Division by zero");
		}

		final int updatedValue = this.getOrDefault() / value;
		this.set(updatedValue);
	}

	public void power(final int exponent) {
		final int updatedValue = (int) Math.pow(this.getOrDefault(), exponent);
		this.set(updatedValue);
	}

}