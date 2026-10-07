package dev.joid.lib.utils.signal.impl.primitive;

import dev.joid.lib.utils.signal.Signal;

public class DoubleSignal extends Signal<Double> {

	public DoubleSignal() {
		super(0D);
	}

	public DoubleSignal(final double value) {
		super(value);
	}

	public static DoubleSignal of(final double defaultValue) {
		final DoubleSignal instance = new DoubleSignal();
		instance.set(defaultValue);
		return instance;
	}

	public void add(final double value) {
		final double updatedValue = this.peek() + value;
		this.set(updatedValue);
	}

	public void subtract(final double value) {
		final double updatedValue = this.peek() - value;
		this.set(updatedValue);
	}

	public void multiply(final double value) {
		final double updatedValue = this.peek() * value;
		this.set(updatedValue);
	}

	public void divide(final double value) {
		if (value == 0) {
			throw new ArithmeticException("Division by zero");
		}
		final double updatedValue = this.peek() / value;
		this.set(updatedValue);
	}

	public void decrement() {
		final double updatedValue = this.peek() - 1;
		this.set(updatedValue);
	}

	public void increment() {
		final double updatedValue = this.peek() + 1;
		this.set(updatedValue);
	}

	@Override
	public String toString() {
		return this.peek() == null ? "DoubleSignal{null}" : "DoubleSignal{" + this.peek().toString() + "}";
	}

}