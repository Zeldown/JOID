package com.example.describing.position;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.utils.align.Align;

public class AnchorUI extends UI {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color DARK        = new Color(85, 85, 85);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		RectNode
		.create(660, 340, 600, 400)
		.color(AnchorUI.PLACEHOLDER)
		.body(card -> {
			RectNode.create(299, 20, 2, 360).color(AnchorUI.DARK).attach(card);
			// #region anchors
			RectNode.create(300, 60, 160, 80).color(Color.WHITE).anchorX(Align.START).attach(card);
			RectNode.create(300, 160, 160, 80).color(AnchorUI.INK).anchorX(Align.CENTER).attach(card);
			RectNode.create(300, 260, 160, 80).color(Color.WHITE).anchorX(Align.END).attach(card);
			// #endregion
		})
		.attach(this);
	}

}