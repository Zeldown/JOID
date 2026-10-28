package com.example.describing.flex;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.layout.FlexNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.utils.align.Align;

public class ToolbarUI extends UI {

	private static final Color ACCENT = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		// #region toolbar
		FlexNode
		.horizontal(20, 20, 100)
		.margin(15D)
		.align(Align.CENTER)
		.body(flex -> {
			for (int i = 0; i < 5; i++) {
				RectNode.create(0, 0, 60, 60).color(ToolbarUI.ACCENT).attach(flex);
			}
		})
		.attach(this);
		// #endregion
	}

}