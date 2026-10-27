package com.example.reference;

import dev.joid.internal.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.shape.RectNode;

// #region ui
@UIData(background = true)
public class ProfileUI extends UI {

	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		RectNode
		.create(760, 390, 400, 300)
		.color(ProfileUI.PLACEHOLDER)
		.hoveredColor(Color.WHITE)
		.onClick(event -> JOID.close(this))
		.attach(this);
	}

}
// #endregion