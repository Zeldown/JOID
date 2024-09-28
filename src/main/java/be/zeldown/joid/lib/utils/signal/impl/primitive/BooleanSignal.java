package be.zeldown.joid.lib.utils.signal.impl.primitive;

import be.zeldown.joid.lib.utils.signal.Signal;

public class BooleanSignal extends Signal<Boolean> {

	public BooleanSignal() {
		super(false);
	}

	public BooleanSignal(final boolean value) {
		super(value);
	}

	public void toggle() {
		this.set(!this.getOrDefault());
	}

	public static BooleanSignal of(final boolean defaultValue) {
		final BooleanSignal instance = new BooleanSignal();
		instance.set(defaultValue);
		return instance;
	}

}