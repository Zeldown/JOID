package dev.joid.lib.signal.impl.primitive;

import dev.joid.lib.signal.Signal;
import lombok.NonNull;

public class FloatSignal extends Signal<Float> {

	protected FloatSignal() {
		super(0F);
	}

	protected FloatSignal(final float value) {
		super(value);
	}

	public static @NonNull FloatSignal of() {
		return new FloatSignal();
	}

	public static @NonNull FloatSignal of(final float value) {
		return new FloatSignal(value);
	}

	public @NonNull FloatSignal add(final float value) {
		final float updatedValue = this.peek() + value;
		this.set(updatedValue);
		return this;
	}

	public @NonNull FloatSignal subtract(final float value) {
		final float updatedValue = this.peek() - value;
		this.set(updatedValue);
		return this;
	}

	public @NonNull FloatSignal multiply(final float value) {
		final float updatedValue = this.peek() * value;
		this.set(updatedValue);
		return this;
	}

	public @NonNull FloatSignal divide(final float value) {
		final float updatedValue = this.peek() / value;
		this.set(updatedValue);
		return this;
	}

	public @NonNull FloatSignal decrement() {
		final float updatedValue = this.peek() - 1F;
		this.set(updatedValue);
		return this;
	}

	public @NonNull FloatSignal increment() {
		final float updatedValue = this.peek() + 1F;
		this.set(updatedValue);
		return this;
	}

	@Override
	public String toString() {
		return this.peek() == null ? "FloatSignal{null}" : "FloatSignal{" + this.peek().toString() + "}";
	}

}