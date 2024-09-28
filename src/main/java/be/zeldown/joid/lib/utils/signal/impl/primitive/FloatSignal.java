package be.zeldown.joid.lib.utils.signal.impl.primitive;

import be.zeldown.joid.lib.utils.signal.Signal;

public class FloatSignal extends Signal<Float> {

	public FloatSignal() {
		super(0.0f);
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
		final float updatedValue = this.getOrDefault() + value;
		this.set(updatedValue);
	}

	public void subtract(final float value) {
		final float updatedValue = this.getOrDefault() - value;
		this.set(updatedValue);
	}

	public void multiply(final float value) {
		final float updatedValue = this.getOrDefault() * value;
		this.set(updatedValue);
	}

	public void divide(final float value) {
		if (value == 0.0f) {
			throw new ArithmeticException("Division by zero");
		}
		final float updatedValue = this.getOrDefault() / value;
		this.set(updatedValue);
	}

	public void increment() {
		final float updatedValue = this.getOrDefault() + 1.0f;
		this.set(updatedValue);
	}

	public void decrement() {
		final float updatedValue = this.getOrDefault() - 1.0f;
		this.set(updatedValue);
	}

}