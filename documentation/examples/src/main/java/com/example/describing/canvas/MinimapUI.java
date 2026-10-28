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

	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		RectNode
		.create(1560, 40, 320, 320)
		.color(MinimapUI.SURFACE)
		.body(map -> CircleNode.create(140, 140, 40).color(MinimapUI.ACCENT).attach(map))
		.attach(this);
	}

}
// #endregion