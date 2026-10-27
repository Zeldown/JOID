package com.example.describing.canvas;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.utils.align.Align;

// #region class
@UIData(anchorX = Align.END, anchorY = Align.START)
public class MinimapUI extends UI {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		RectNode
		.create(0, 0, 0, 0)
		.color(MinimapUI.PLACEHOLDER)
		.x(() -> super.getViewX())
		.y(() -> super.getViewY())
		.width(() -> super.getViewWidth())
		.height(() -> super.getViewHeight())
		.attach(this);

		RectNode
		.create(1560, 40, 320, 320)
		.color(Color.WHITE)
		.body(map -> CircleNode.create(140, 140, 40).color(MinimapUI.INK).attach(map))
		.attach(this);
	}

}
// #endregion