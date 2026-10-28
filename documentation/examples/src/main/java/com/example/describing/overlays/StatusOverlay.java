package com.example.describing.overlays;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIDataOverlay;
import dev.joid.lib.ui.node.impl.shape.RectNode;

// #region class
@UIDataOverlay(active = true)
public class StatusOverlay extends UI {

	private static final Color ACCENT = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		RectNode.create(40, 40, 400, 40).color(StatusOverlay.ACCENT).attach(this);
	}

}
// #endregion