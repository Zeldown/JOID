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

	public static @NonNull BooleanSignal of() {
		return new BooleanSignal();
	}

	public static @NonNull BooleanSignal of(final boolean value) {
		return new BooleanSignal(value);
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