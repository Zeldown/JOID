package com.example.describing.position;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class OrderUI extends UI {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color DARK        = new Color(85, 85, 85);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		RectNode
		.create(660, 340, 600, 400)
		.color(OrderUI.PLACEHOLDER)
		.body(card -> {
			// #region order
			RectNode.create(60, 60, 200, 200).color(OrderUI.DARK).zindex(1).attach(card);
			RectNode.create(160, 100, 200, 200).color(OrderUI.INK).attach(card);
			RectNode
			.create(340, 140, 200, 200)
			.color(Color.WHITE)
			.body(front -> RectNode.create(-40, -40, 120, 120).color(OrderUI.DARK).zindex(-1).attach(front))
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}