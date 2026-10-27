package com.example.describing.layout;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.property.OverflowProperty;

public class OverflowUI extends UI {

	private static final Color PLACEHOLDER = new Color(221, 221, 221);
	private static final Color INK         = new Color(153, 153, 153);

	@Override
	public void init() {
		RectNode
		.create(530, 390, 860, 300)
		.color(OverflowUI.PLACEHOLDER)
		.body(card -> {
			// #region hidden
			RectNode
			.create(20, 20, 360, 260)
			.color(Color.WHITE)
			.overflow(OverflowProperty.HIDDEN)
			.body(area -> RectNode.create(40, 60, 360, 140).color(OverflowUI.INK).attach(area))
			.attach(card);
			// #endregion
			// #region none
			RectNode
			.create(440, 20, 360, 260)
			.color(Color.WHITE)
			.body(area -> RectNode.create(40, 60, 360, 140).color(OverflowUI.INK).attach(area))
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}