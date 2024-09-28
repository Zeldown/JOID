package be.zeldown.joid.lib.utils.signal.impl.primitive;

import be.zeldown.joid.lib.utils.signal.Signal;

public class DoubleSignal extends Signal<Double> {

	public DoubleSignal() {
		super(0.0);
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
		final double updatedValue = this.getOrDefault() + value;
		this.set(updatedValue);
	}

	public void subtract(final double value) {
		final double updatedValue = this.getOrDefault() - value;
		this.set(updatedValue);
	}

	public void multiply(final double value) {
		final double updatedValue = this.getOrDefault() * value;
		this.set(updatedValue);
	}

	public void divide(final double value) {
		if (value == 0) {
			throw new ArithmeticException("Division by zero");
		}
		final double updatedValue = this.getOrDefault() / value;
		this.set(updatedValue);
	}

	public void increment() {
		final double updatedValue = this.getOrDefault() + 1;
		this.set(updatedValue);
	}

	public void decrement() {
		final double updatedValue = this.getOrDefault() - 1;
		this.set(updatedValue);
	}

}