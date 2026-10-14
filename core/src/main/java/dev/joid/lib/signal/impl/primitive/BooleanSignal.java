package dev.joid.lib.signal.impl.primitive;

import dev.joid.lib.signal.Signal;
import lombok.NonNull;

public class BooleanSignal extends Signal<Boolean> {

	public BooleanSignal() {
		super(false);
	}

	public BooleanSignal(final boolean value) {
		super(value);
	}

	public @NonNull BooleanSignal toggle() {
		this.transform(current -> !current);
		return this;
	}

}