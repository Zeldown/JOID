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
		return super.transform(current -> current - 1D);
	}

	public @NonNull DoubleSignal increment() {
		return super.transform(current -> current + 1D);
	}

	public @NonNull DoubleSignal add(final double value) {
		return super.transform(current -> current + value);
	}

	public @NonNull DoubleSignal subtract(final double value) {
		return super.transform(current -> current - value);
	}

	public @NonNull DoubleSignal multiply(final double value) {
		return super.transform(current -> current * value);
	}

	public @NonNull DoubleSignal divide(final double value) {
		return super.transform(current -> current / value);
	}

}