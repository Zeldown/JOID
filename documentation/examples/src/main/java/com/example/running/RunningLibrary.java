package com.example.running;

import dev.joid.lib.JOID;

public final class RunningLibrary {

	private RunningLibrary() {}

	// #region ready
	public static void install() {
		JOID.onReady(() -> JOID.open(new RunningStartUI()));
	}
	// #endregion

}