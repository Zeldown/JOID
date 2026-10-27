package com.example.kit;

import dev.joid.lib.ui.node.impl.control.slider.IntegerSliderNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

// #region slider
public class KitIntegerSliderNode extends IntegerSliderNode {

	protected KitIntegerSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.track(RectNode.create(0, 0, 0, 0).color(KitColors.SURFACE));
		super.thumb(RectNode.create(0, 0, 0, 0).color(() -> super.isSliding() ? KitColors.DARK : KitColors.INK));
	}

	public static KitIntegerSliderNode create(final double x, final double y, final double width, final double height) {
		return new KitIntegerSliderNode(x, y, width, height);
	}

}
// #endregion