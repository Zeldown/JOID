package com.example.describing.uis;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.shape.RectNode;

// #region class
@UIData(background = true, closeable = false, zoomable = false)
public class PauseUI extends UI {

	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		RectNode
		.create(810, 390, 300, 300)
		.color(PauseUI.PLACEHOLDER)
		.onClick(event -> super.getData().closeable(true))
		.attach(this);
	}

}
// #endregion