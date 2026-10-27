package com.example.describing.canvas;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class EdgesUI extends UI {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color DARK        = new Color(85, 85, 85);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		// #region view
		RectNode
		.create(0, 0, 0, 0)
		.color(EdgesUI.PLACEHOLDER)
		.x(() -> super.getViewX())
		.y(() -> super.getViewY())
		.width(() -> super.getViewWidth())
		.height(() -> super.getViewHeight())
		.attach(this);
		// #endregion

		// #region canvas
		RectNode.create(0, 0, 1920, 1080).color(Color.WHITE).attach(this);
		RectNode.create(660, 340, 600, 400).color(EdgesUI.INK).attach(this);
		// #endregion

		// #region edges
		RectNode
		.create(0, 0, 0, 120)
		.color(EdgesUI.DARK)
		.x(() -> super.getViewX())
		.y(() -> super.getViewY())
		.width(() -> super.getViewWidth())
		.attach(this);

		RectNode
		.create(0, 0, 80, 80)
		.color(EdgesUI.INK)
		.x(() -> super.getViewX() + super.getViewWidth() - 100D)
		.y(() -> super.getViewY() + 20D)
		.attach(this);
		// #endregion
	}

}