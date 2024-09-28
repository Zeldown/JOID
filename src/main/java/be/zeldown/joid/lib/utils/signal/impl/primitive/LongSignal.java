package be.zeldown.joid.lib.utils.signal.impl.primitive;

import be.zeldown.joid.lib.utils.signal.Signal;

public class LongSignal extends Signal<Long> {

	public LongSignal() {
		super(0L);
	}

	public LongSignal(final long value) {
		super(value);
	}

	public static LongSignal of(final long defaultValue) {
		final LongSignal instance = new LongSignal();
		instance.set(defaultValue);
		return instance;
	}

	public void increment() {
		final long updatedValue = this.getOrDefault() + 1;
		this.set(updatedValue);
	}

	public void decrement() {
		final long updatedValue = this.getOrDefault() - 1;
		this.set(updatedValue);
	}

	public void add(final long value) {
		final long updatedValue = this.getOrDefault() + value;
		this.set(updatedValue);
	}

	public void subtract(final long value) {
		final long updatedValue = this.getOrDefault() - value;
		this.set(updatedValue);
	}

	public void multiply(final long value) {
		final long updatedValue = this.getOrDefault() * value;
		this.set(updatedValue);
	}

	public void divide(final long value) {
		if (value == 0) {
			throw new ArithmeticException("Division by zero");
		}

		final long updatedValue = this.getOrDefault() / value;
		this.set(updatedValue);
	}

	public void power(final int exponent) {
		final long updatedValue = (long) Math.pow(this.getOrDefault(), exponent);
		this.set(updatedValue);
	}

}