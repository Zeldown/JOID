package com.example.capture;

import dev.joid.lib.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.ui.WindowUIBridge;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIDataOverlay;

@UIDataOverlay(active = true)
public class ReopenUI extends UI {

	private static UI kept;

	private ReopenUI() {
		if (ReopenUI.kept == null) {
			ReopenUI.kept = BridgeHandler.UI.getBridge(WindowUIBridge.class).getUiList().stream().filter(ui -> !(ui instanceof StageUI) && !(ui instanceof ReopenUI)).reduce((first, second) -> second).orElse(null);
		} else {
			JOID.open(ReopenUI.kept);
			ReopenUI.kept = null;
		}
	}

}