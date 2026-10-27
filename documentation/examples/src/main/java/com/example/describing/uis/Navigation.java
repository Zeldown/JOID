package com.example.describing.uis;

import dev.joid.internal.JOID;

public final class Navigation {

	private Navigation() {}

	public static void open() {
		// #region open
		JOID.open(new MenuUI());
		// #endregion
	}

	public static void close() {
		// #region close
		final MenuUI menu = JOID.getUi(MenuUI.class);
		if (menu != null) {
			JOID.close(menu);
		}
		// #endregion
	}

	public static void replace() {
		// #region replace
		JOID.open(new MenuUI());
		JOID.open(new EditorUI());
		// #endregion
	}

	public static void force() {
		// #region force
		JOID.forceOpen(new MenuUI());
		// #endregion
	}

}