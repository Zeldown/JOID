package com.example.kit;

import dev.joid.lib.ui.node.impl.control.slider.DoubleSliderNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class KitDoubleSliderNode extends DoubleSliderNode {

	protected KitDoubleSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.track(RectNode.create(0, 0, 0, 0).color(KitColors.SURFACE));
		super.thumb(RectNode.create(0, 0, 0, 0).color(() -> super.isSliding() ? KitColors.DARK : KitColors.INK));
	}

	public static KitDoubleSliderNode create(final double x, final double y, final double width, final double height) {
		return new KitDoubleSliderNode(x, y, width, height);
	}

}