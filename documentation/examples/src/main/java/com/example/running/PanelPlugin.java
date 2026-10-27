package com.example.running;

import com.example.ExampleFont;

import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.TextInfo;

public final class PanelPlugin {

	private PanelPlugin() {}

	// #region ready
	public static void install() {
		JOID.onReady(() -> {
			final PanelUIBridge panels = PanelUIBridge.create();
			panels.hoverInfo(TextInfo.create(ExampleFont.REGULAR, 20F).color(Color.WHITE));
			BridgeHandler.UI.register(panels, 10);
			JOID.open(new RunningPanelUI());
		});
	}
	// #endregion

}