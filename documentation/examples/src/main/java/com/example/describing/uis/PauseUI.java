package com.example.describing.uis;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;

// #region class
@UIData(background = true, closeable = false, zoomable = false)
public class PauseUI extends UI {

	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();

	@Override
	public void init() {
		RectNode
		.create(810, 390, 300, 300)
		.color(PauseUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(PauseUI.ELEMENT, 1.5F))
		.onClick(event -> super.getData().closeable(true))
		.attach(this);
	}

}
// #endregion