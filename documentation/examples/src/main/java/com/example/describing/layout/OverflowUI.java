package com.example.describing.layout;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.property.OverflowProperty;

public class OverflowUI extends UI {

	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		RectNode
		.create(530, 390, 860, 300)
		.color(OverflowUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(OverflowUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region hidden
			RectNode
			.create(20, 20, 360, 260)
			.color(OverflowUI.ELEMENT)
			.overflow(OverflowProperty.HIDDEN)
			.body(area -> RectNode.create(40, 60, 360, 140).color(OverflowUI.ACCENT).attach(area))
			.attach(card);
			// #endregion
			// #region none
			RectNode
			.create(440, 20, 360, 260)
			.color(OverflowUI.ELEMENT)
			.body(area -> RectNode.create(40, 60, 360, 140).color(OverflowUI.ACCENT).attach(area))
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}