package com.example.style;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class LiveColorsUI extends UI {

	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();

	@Override
	public void init() {
		RectNode
		.create(560, 400, 800, 280)
		.color(LiveColorsUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(LiveColorsUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region live
			RectNode.create(60, 60, 160, 160).color(Color.RAINBOW).attach(card);
			RectNode.create(260, 60, 160, 160).color(Color.RAINBOW.darker()).attach(card);
			RectNode.create(460, 60, 280, 160).color(Color.LOADING).attach(card);
			// #endregion
		})
		.attach(this);
	}

}