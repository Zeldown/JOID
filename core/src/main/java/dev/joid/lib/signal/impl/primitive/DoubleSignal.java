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

	public @NonNull DoubleSignal decrement() {
		super.transform(current -> current - 1D);
		return this;
	}

	public @NonNull DoubleSignal increment() {
		super.transform(current -> current + 1D);
		return this;
	}

	public @NonNull DoubleSignal add(final double value) {
		super.transform(current -> current + value);
		return this;
	}

	public @NonNull DoubleSignal subtract(final double value) {
		super.transform(current -> current - value);
		return this;
	}

	public @NonNull DoubleSignal multiply(final double value) {
		super.transform(current -> current * value);
		return this;
	}

	public @NonNull DoubleSignal divide(final double value) {
		super.transform(current -> current / value);
		return this;
	}

}