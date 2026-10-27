package com.example.describing.position;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.property.PositionProperty;

public class PanelUI extends UI {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		// #region parent
		RectNode
		.create(560, 290, 800, 500)
		.color(PanelUI.PLACEHOLDER)
		.body(panel -> {
			RectNode.create(0, 0, panel.getWidth(), 80).color(PanelUI.INK).attach(panel);
			RectNode.create(panel.dw(2D) - 150D, panel.ah(-100D), 300, 60).color(Color.WHITE).attach(panel);
		})
		.attach(this);
		// #endregion

		// #region follow
		final RectNode bar = RectNode.create(560, 820, 400, 40).color(PanelUI.PLACEHOLDER).attach(this);
		RectNode.create(0, 0, 0, 40).color(PanelUI.INK).width(() -> bar.getWidth() / 2D).attach(bar);
		// #endregion

		// #region ratio
		RectNode.create(1000, 820, 160, 0).color(PanelUI.INK).aspectRatio(16D / 9D).attach(this);
		// #endregion

		// #region absolute
		RectNode
		.create(560, 290, 800, 500)
		.body(panel -> RectNode.create(20, 20, 60, 60).color(Color.WHITE).position(PositionProperty.ABSOLUTE).attach(panel))
		.attach(this);
		// #endregion
	}

}