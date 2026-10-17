package dev.joid.lib.signal.impl.primitive;

import dev.joid.lib.signal.Signal;
import lombok.NonNull;

public class FloatSignal extends Signal<Float> {

	public FloatSignal() {
		super(0F);
	}

	public FloatSignal(final float value) {
		super(value);
	}

	public @NonNull FloatSignal decrement() {
		super.transform(current -> current - 1F);
		return this;
	}

	public @NonNull FloatSignal increment() {
		super.transform(current -> current + 1F);
		return this;
	}

	public @NonNull FloatSignal add(final float value) {
		super.transform(current -> current + value);
		return this;
	}

	public @NonNull FloatSignal subtract(final float value) {
		super.transform(current -> current - value);
		return this;
	}

	public @NonNull FloatSignal multiply(final float value) {
		super.transform(current -> current * value);
		return this;
	}

	public @NonNull FloatSignal divide(final float value) {
		super.transform(current -> current / value);
		return this;
	}

}