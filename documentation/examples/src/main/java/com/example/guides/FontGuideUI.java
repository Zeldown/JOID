package com.example.guides;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.ui.WindowUIBridge;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class FontGuideUI extends UI {

	private static final Color INK = new Color(153, 153, 153).lock();

	@Override
	public void init() {
		// #region tooltips
		BridgeHandler.UI.getBridge(WindowUIBridge.class).hoverInfo(FontGuideStyle.TOOLTIP);
		// #endregion

		// #region use
		RectNode
		.create(660, 340, 600, 400)
		.color(Color.WHITE)
		.body(card -> {
			TextNode.create(50, 50).text(Text.create("Settings", FontGuideStyle.TITLE)).attach(card);
			TextNode.create(50, 130).text(Text.create("Every text uses one font.", FontGuideStyle.BODY)).attach(card);
			RectNode
			.create(50, 290, 200, 60)
			.color(FontGuideUI.INK)
			.label(Text.create("Save", FontGuideStyle.BODY.withColor(Color.WHITE)))
			.hover(() -> "Saves the settings")
			.attach(card);
		})
		.attach(this);
		// #endregion
	}

}