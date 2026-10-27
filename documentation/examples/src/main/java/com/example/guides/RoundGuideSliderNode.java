package com.example.guides;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.control.slider.IntegerSliderNode;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class RoundGuideSliderNode extends IntegerSliderNode {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	protected RoundGuideSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.track(RectNode.create(0, 0, 0, 0).color(RoundGuideSliderNode.PLACEHOLDER).effect(RoundedNodeEffect.create((float) height / 2F)));
		super.thumb(CircleNode.create(0, 0, 0).color(() -> RoundGuideSliderNode.INK.withAlpha(super.isEnabled() ? 1F : 0.4F)));
	}

	public static RoundGuideSliderNode create(final double x, final double y, final double width, final double height) {
		return new RoundGuideSliderNode(x, y, width, height);
	}

}