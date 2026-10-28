package com.example.describing.position;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class CopyUI extends UI {

	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color MUTED   = new Color(139, 147, 163).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		RectNode
		.create(560, 390, 800, 300)
		.color(CopyUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(CopyUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region copy
			final RectNode tile = RectNode
			.create(40, 50, 200, 200)
			.color(CopyUI.ELEMENT)
			.body(rect -> CircleNode.create(60, 60, 80).color(CopyUI.ACCENT).attach(rect));
			tile.attach(card);
			tile.copy().x(300).attach(card);
			tile.<RectNode>copy().color(CopyUI.MUTED).x(560).attach(card);
			// #endregion
		})
		.attach(this);
	}

}