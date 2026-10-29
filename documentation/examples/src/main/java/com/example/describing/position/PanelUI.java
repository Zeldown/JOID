package com.example.describing.position;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.property.PositionProperty;

public class PanelUI extends UI {

	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		// #region parent
		RectNode
		.create(560, 290, 800, 500)
		.color(PanelUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(PanelUI.ELEMENT, 1.5F))
		.body(panel -> {
			RectNode.create(20, 20, panel.getWidth() - 40D, 60).color(PanelUI.ACCENT).attach(panel);
			RectNode.create(panel.dw(2D) - 150D, panel.ah(-100D), 300, 60).color(PanelUI.ELEMENT).attach(panel);
		})
		.attach(this);
		// #endregion

		// #region follow
		final RectNode bar = RectNode.create(560, 820, 400, 40).color(PanelUI.ELEMENT).attach(this);
		RectNode.create(0, 0, 0, 40).color(PanelUI.ACCENT).width(() -> bar.getWidth() / 2D).attach(bar);
		// #endregion

		// #region ratio
		RectNode.create(1000, 820, 160, 0).color(PanelUI.ACCENT).aspectRatio(16D / 9D).attach(this);
		// #endregion

		// #region absolute
		RectNode
		.create(560, 290, 800, 500)
		.body(panel -> RectNode.create(20, 20, 60, 60).color(PanelUI.ELEMENT).position(PositionProperty.ABSOLUTE).attach(panel))
		.attach(this);
		// #endregion
	}

}