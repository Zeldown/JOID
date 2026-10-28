package com.example.reference;

import dev.joid.lib.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.shape.RectNode;

// #region ui
@UIData(background = true)
public class ProfileUI extends UI {

	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();

	@Override
	public void init() {
		RectNode
		.create(760, 390, 400, 300)
		.color(ProfileUI.SURFACE)
		.hoveredColor(ProfileUI.ELEMENT)
		.onClick(event -> JOID.close(this))
		.attach(this);
	}

}
// #endregion