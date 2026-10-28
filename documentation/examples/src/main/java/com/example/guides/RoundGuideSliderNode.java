package com.example.guides;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.control.slider.IntegerSliderNode;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class RoundGuideSliderNode extends IntegerSliderNode {

	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	protected RoundGuideSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.track(RectNode.create(0, 0, 0, 0).color(RoundGuideSliderNode.ELEMENT).effect(RoundedNodeEffect.create((float) height / 2F)));
		super.thumb(CircleNode.create(0, 0, 0).color(() -> RoundGuideSliderNode.ACCENT.withAlpha(super.isEnabled() ? 1F : 0.4F)));
	}

	public static RoundGuideSliderNode create(final double x, final double y, final double width, final double height) {
		return new RoundGuideSliderNode(x, y, width, height);
	}

}