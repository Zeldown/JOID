package dev.joid.lib.signal.impl.primitive;

import dev.joid.lib.signal.Signal;

public class FloatSignal extends Signal<Float> {

	public FloatSignal() {
		super(0F);
	}

	public FloatSignal(final float value) {
		super(value);
	}

	public static FloatSignal of(final float defaultValue) {
		final FloatSignal instance = new FloatSignal();
		instance.set(defaultValue);
		return instance;
	}

	public void add(final float value) {
		final float updatedValue = this.peek() + value;
		this.set(updatedValue);
	}

	public void subtract(final float value) {
		final float updatedValue = this.peek() - value;
		this.set(updatedValue);
	}

	public void multiply(final float value) {
		final float updatedValue = this.peek() * value;
		this.set(updatedValue);
	}

	public void divide(final float value) {
		if (value == 0F) {
			throw new ArithmeticException("Division by zero");
		}
		final float updatedValue = this.peek() / value;
		this.set(updatedValue);
	}

	public void decrement() {
		final float updatedValue = this.peek() - 1F;
		this.set(updatedValue);
	}

	public void increment() {
		final float updatedValue = this.peek() + 1F;
		this.set(updatedValue);
	}

	@Override
	public String toString() {
		return this.peek() == null ? "FloatSignal{null}" : "FloatSignal{" + this.peek().toString() + "}";
	}

}