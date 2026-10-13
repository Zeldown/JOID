package dev.joid.lib.ui.event;

import lombok.NonNull;

public abstract class Event {

	private boolean monitor;

	protected final void monitor(final boolean monitor) {
		this.monitor = monitor;
	}

	protected final void checkWritable(final @NonNull String verb) {
		if (this.monitor) {
			throw new IllegalStateException("A MONITOR listener only reads " + this.getClass().getSimpleName() + ", it cannot call " + verb + "(): listen with another priority to " + verb + " it");
		}
	}

}