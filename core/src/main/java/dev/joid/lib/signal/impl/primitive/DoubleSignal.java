package dev.joid.lib.signal.impl.primitive;

import dev.joid.lib.signal.Signal;
import lombok.NonNull;

public class DoubleSignal extends Signal<Double> {

	public DoubleSignal() {
		super(0D);
	}

	public DoubleSignal(final double value) {
		super(value);
	}

	public @NonNull DoubleSignal add(final double value) {
		this.transform(current -> current + value);
		return this;
	}

	public @NonNull DoubleSignal subtract(final double value) {
		this.transform(current -> current - value);
		return this;
	}

	public @NonNull DoubleSignal multiply(final double value) {
		this.transform(current -> current * value);
		return this;
	}

	public @NonNull DoubleSignal divide(final double value) {
		this.transform(current -> current / value);
		return this;
	}

	public @NonNull DoubleSignal decrement() {
		this.transform(current -> current - 1);
		return this;
	}

	public @NonNull DoubleSignal increment() {
		this.transform(current -> current + 1);
		return this;
	}

	@Override
	public String toString() {
		return this.peek() == null ? "DoubleSignal{null}" : "DoubleSignal{" + this.peek().toString() + "}";
	}

}