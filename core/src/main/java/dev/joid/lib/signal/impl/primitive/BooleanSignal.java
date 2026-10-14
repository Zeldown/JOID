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

	public static @NonNull BooleanSignal of(final boolean defaultValue) {
		final BooleanSignal instance = new BooleanSignal(defaultValue);
		instance.set(defaultValue);
		return instance;
	}

	public @NonNull BooleanSignal toggle() {
		this.set(!this.peek());
		return this;
	}

	@Override
	public String toString() {
		return this.peek() == null ? "BooleanSignal{null}" : "BooleanSignal{" + this.peek().toString() + "}";
	}

}