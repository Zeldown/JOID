package com.example.interactivity.kit;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.impl.control.slider.IntegerSliderNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class PlainIntegerSliderNode extends IntegerSliderNode {

	private static final Color INK = new Color(153, 153, 153).lock();

	protected PlainIntegerSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.track(RectNode.create(0, 0, 0, 0).color(() -> Color.WHITE.withAlpha(super.isEnabled() ? 1F : 0.4F)));
		super.thumb(RectNode.create(0, 0, 0, 0).color(() -> PlainIntegerSliderNode.INK.withAlpha(super.isEnabled() ? 1F : 0.4F)));
	}

	public static PlainIntegerSliderNode create(final double x, final double y, final double width, final double height) {
		return new PlainIntegerSliderNode(x, y, width, height);
	}

}