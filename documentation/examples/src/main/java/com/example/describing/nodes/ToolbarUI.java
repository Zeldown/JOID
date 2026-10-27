package com.example.describing.nodes;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class ToolbarUI extends UI {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		// #region opener
		RectNode.create(660, 490, 600, 100).color(ToolbarUI.PLACEHOLDER).attach(this);
		// #endregion

		// #region append
		final RectNode toolbar = RectNode.create(660, 290, 600, 100).color(ToolbarUI.PLACEHOLDER);
		final RectNode back = RectNode.create(20, 20, 60, 60).color(Color.WHITE);
		final RectNode close = RectNode.create(520, 20, 60, 60).color(Color.WHITE);
		toolbar.append(back, close).attach(this);
		// #endregion

		// #region remove
		toolbar.remove(close);
		// #endregion

		// #region front
		final RectNode first = RectNode.create(660, 690, 300, 100).color(ToolbarUI.INK).attach(this);
		RectNode.create(760, 720, 300, 100).color(Color.WHITE).attach(this);
		first.attach(this);
		// #endregion

		// #region query
		for (final RectNode button : toolbar.getChildren(RectNode.class)) {
			button.hoveredColor(ToolbarUI.INK);
		}
		toolbar.getChild(0, RectNode.class).color(ToolbarUI.INK);
		// #endregion

		// #region self
		RectNode.create(1300, 290, 200, 0).color(Color.WHITE).self(bar -> bar.height(() -> bar.getWidth() / 4D)).attach(this);
		// #endregion
	}

}