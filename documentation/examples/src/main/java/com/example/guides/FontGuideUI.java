package com.example.guides;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.ui.WindowUIBridge;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class FontGuideUI extends UI {

	private static final Color ELEMENT   = new Color(38, 43, 53).lock();
	private static final Color SURFACE   = new Color(23, 26, 33).lock();
	private static final Color ACCENT    = new Color(91, 140, 255).lock();
	private static final Color ON_ACCENT = new Color(14, 16, 20).lock();

	@Override
	public void init() {
		// #region tooltips
		BridgeHandler.UI.getBridge(WindowUIBridge.class).hoverInfo(FontGuideStyle.TOOLTIP);
		// #endregion

		// #region use
		RectNode
		.create(660, 340, 600, 400)
		.color(FontGuideUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(FontGuideUI.ELEMENT, 1.5F))
		.body(card -> {
			TextNode.create(50, 50).text(Text.create("Settings", FontGuideStyle.TITLE)).attach(card);
			TextNode.create(50, 130).text(Text.create("Every text uses one font.", FontGuideStyle.BODY)).attach(card);
			RectNode
			.create(50, 290, 200, 60)
			.color(FontGuideUI.ACCENT)
			.label(Text.create("Save", FontGuideStyle.BODY.withColor(FontGuideUI.ON_ACCENT)))
			.hover(() -> "Saves the settings")
			.attach(card);
		})
		.attach(this);
		// #endregion
	}

}