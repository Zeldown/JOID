package dev.joid.lib.utils.signal.impl.primitive;

import dev.joid.lib.utils.signal.Signal;

public class BooleanSignal extends Signal<Boolean> {

	public BooleanSignal() {
		super(false);
	}

	public BooleanSignal(final boolean value) {
		super(value);
	}

	public static BooleanSignal of(final boolean defaultValue) {
		final BooleanSignal instance = new BooleanSignal();
		instance.set(defaultValue);
		return instance;
	}

	public void toggle() {
		this.set(!this.peek());
	}

	@Override
	public String toString() {
		return this.peek() == null ? "BooleanSignal{null}" : "BooleanSignal{" + this.peek().toString() + "}";
	}

}