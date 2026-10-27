package com.example.describing.overlays;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIDataOverlay;
import dev.joid.lib.ui.node.impl.shape.RectNode;

// #region class
@UIDataOverlay(active = true)
public class HudOverlay extends UI {

	private static final Color INK = new Color(153, 153, 153);

	@Override
	public void init() {
		RectNode.create(40, 40, 400, 40).color(HudOverlay.INK).attach(this);
	}

}
// #endregion