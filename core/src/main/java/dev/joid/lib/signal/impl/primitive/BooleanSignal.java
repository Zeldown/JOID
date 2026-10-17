package dev.joid.lib.signal.impl.primitive;

import dev.joid.lib.signal.Signal;
import lombok.NonNull;

public class BooleanSignal extends Signal<Boolean> {

	protected BooleanSignal() {
		super(false);
	}

	protected BooleanSignal(final boolean value) {
		super(value);
	}

	public static @NonNull BooleanSignal create() {
		return new BooleanSignal();
	}

	public static @NonNull BooleanSignal create(final boolean value) {
		return new BooleanSignal(value);
	}

	public @NonNull BooleanSignal toggle() {
		super.transform(current -> !current);
		return this;
	}

}