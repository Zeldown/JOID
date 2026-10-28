package com.example.describing.nodes;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class ToolbarUI extends UI {

	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		// #region opener
		RectNode.create(660, 490, 600, 100).color(ToolbarUI.SURFACE).attach(this);
		// #endregion

		// #region append
		final RectNode toolbar = RectNode.create(660, 290, 600, 100).color(ToolbarUI.SURFACE);
		final RectNode back = RectNode.create(20, 20, 60, 60).color(ToolbarUI.ELEMENT);
		final RectNode close = RectNode.create(520, 20, 60, 60).color(ToolbarUI.ELEMENT);
		toolbar.append(back, close).attach(this);
		// #endregion

		// #region remove
		toolbar.remove(close);
		// #endregion

		// #region front
		final RectNode first = RectNode.create(660, 690, 300, 100).color(ToolbarUI.ACCENT).attach(this);
		RectNode.create(760, 720, 300, 100).color(ToolbarUI.ELEMENT).attach(this);
		first.attach(this);
		// #endregion

		// #region query
		for (final RectNode button : toolbar.getChildren(RectNode.class)) {
			button.hoveredColor(ToolbarUI.ACCENT);
		}
		toolbar.getChild(0, RectNode.class).color(ToolbarUI.ACCENT);
		// #endregion

		// #region self
		RectNode
		.create(1300, 290, 200, 0)
		.color(ToolbarUI.ELEMENT)
		.self(bar -> bar.height(() -> bar.getWidth() / 4D))
		.attach(this);
		// #endregion
	}

}