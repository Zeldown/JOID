package com.example.running;

import dev.joid.lib.bridge.ui.WindowUIBridge;
import dev.joid.lib.ui.core.UI;

public class PanelUIBridge extends WindowUIBridge {

	protected PanelUIBridge() {}

	public static PanelUIBridge create() {
		return new PanelUIBridge();
	}

	@Override
	public boolean canHandle(final Class<? extends UI> clazz) {
		return RunningPanelUI.class.isAssignableFrom(clazz);
	}

}