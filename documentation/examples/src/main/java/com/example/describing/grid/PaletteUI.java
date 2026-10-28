package com.example.describing.grid;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.layout.GridNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class PaletteUI extends UI {

	private static final Color ACCENT = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		// #region palette
		GridNode
		.create(20, 20, 184, 0)
		.margin(8D)
		.body(grid -> {
			for (int i = 0; i < 16; i++) {
				RectNode.create(0, 0, 40, 40).color(PaletteUI.ACCENT).attach(grid);
			}
		})
		.attach(this);
		// #endregion
	}

}