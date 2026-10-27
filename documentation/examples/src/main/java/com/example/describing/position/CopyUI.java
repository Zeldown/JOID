package com.example.describing.position;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class CopyUI extends UI {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color DARK        = new Color(85, 85, 85);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		RectNode
		.create(560, 390, 800, 300)
		.color(CopyUI.PLACEHOLDER)
		.body(card -> {
			// #region copy
			final RectNode tile = RectNode
			.create(40, 50, 200, 200)
			.color(Color.WHITE)
			.body(rect -> CircleNode.create(60, 60, 80).color(CopyUI.INK).attach(rect));
			tile.attach(card);
			tile.copy().x(300).attach(card);
			tile.<RectNode>copy().color(CopyUI.DARK).x(560).attach(card);
			// #endregion
		})
		.attach(this);
	}

}