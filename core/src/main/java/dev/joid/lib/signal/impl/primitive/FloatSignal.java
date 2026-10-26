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

	public @NonNull FloatSignal increment() {
		return super.transform(current -> current + 1F);
	}

	public @NonNull FloatSignal decrement() {
		return super.transform(current -> current - 1F);
	}

	public @NonNull FloatSignal add(final float value) {
		return super.transform(current -> current + value);
	}

	public @NonNull FloatSignal subtract(final float value) {
		return super.transform(current -> current - value);
	}

	public @NonNull FloatSignal multiply(final float value) {
		return super.transform(current -> current * value);
	}

	public @NonNull FloatSignal divide(final float value) {
		return super.transform(current -> current / value);
	}

}