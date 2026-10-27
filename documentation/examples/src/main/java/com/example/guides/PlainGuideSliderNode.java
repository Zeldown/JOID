package com.example.guides;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.impl.control.slider.IntegerSliderNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class PlainGuideSliderNode extends IntegerSliderNode {

	private static final Color INK = new Color(153, 153, 153).lock();

	protected PlainGuideSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.track(RectNode.create(0, 0, 0, 0).color(Color.WHITE));
		super.thumb(RectNode.create(0, 0, 0, 0).color(() -> PlainGuideSliderNode.INK.withAlpha(super.isEnabled() ? 1F : 0.4F)));
	}

	public static PlainGuideSliderNode create(final double x, final double y, final double width, final double height) {
		return new PlainGuideSliderNode(x, y, width, height);
	}

}