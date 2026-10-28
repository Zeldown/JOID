package com.example.describing.position;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class OrderUI extends UI {

	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color MUTED   = new Color(139, 147, 163).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		RectNode
		.create(660, 340, 600, 400)
		.color(OrderUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(OrderUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region order
			RectNode.create(60, 60, 200, 200).color(OrderUI.MUTED).zindex(1).attach(card);
			RectNode.create(160, 100, 200, 200).color(OrderUI.ACCENT).attach(card);
			RectNode
			.create(340, 140, 200, 200)
			.color(OrderUI.ELEMENT)
			.body(front -> RectNode.create(-40, -40, 120, 120).color(OrderUI.MUTED).zindex(-1).attach(front))
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}