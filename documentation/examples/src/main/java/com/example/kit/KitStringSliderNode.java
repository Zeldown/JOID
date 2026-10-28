package com.example.kit;

import dev.joid.lib.ui.node.impl.control.slider.StringSliderNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class KitStringSliderNode extends StringSliderNode {

	protected KitStringSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.track(RectNode.create(0, 0, 0, 0).color(KitColors.ELEMENT));
		super.thumb(RectNode.create(0, 0, 0, 0).color(KitColors.ACCENT));
	}

	public static KitStringSliderNode create(final double x, final double y, final double width, final double height) {
		return new KitStringSliderNode(x, y, width, height);
	}

}