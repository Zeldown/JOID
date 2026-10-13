package dev.joid.lib.ui.event;

public enum EventPriority {

	HIGHEST,
	HIGH,
	NORMAL,
	LOW,
	LOWEST,
	MONITOR;

	public final boolean isMonitor() {
		return this == EventPriority.MONITOR;
	}

}