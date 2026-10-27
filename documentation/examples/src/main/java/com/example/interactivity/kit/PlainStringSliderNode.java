package com.example.interactivity.kit;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.impl.control.slider.StringSliderNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class PlainStringSliderNode extends StringSliderNode {

	private static final Color INK = new Color(153, 153, 153).lock();

	protected PlainStringSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.track(RectNode.create(0, 0, 0, 0).color(Color.WHITE));
		super.thumb(RectNode.create(0, 0, 0, 0).color(PlainStringSliderNode.INK));
	}

	public static PlainStringSliderNode create(final double x, final double y, final double width, final double height) {
		return new PlainStringSliderNode(x, y, width, height);
	}

}