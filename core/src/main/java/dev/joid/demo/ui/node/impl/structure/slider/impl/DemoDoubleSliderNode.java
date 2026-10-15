package dev.joid.demo.ui.node.impl.structure.slider.impl;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.structure.slider.impl.DoubleSliderNode;
import lombok.NonNull;

public class DemoDoubleSliderNode extends DoubleSliderNode {

	private static final Color INK = new Color(153, 153, 153).lock();

	protected DemoDoubleSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.track(RectNode.create(0, 0, 0, 0).color(() -> Color.WHITE.withAlpha(super.isEnabled() ? 1F : 0.4F)));
		super.thumb(RectNode.create(0, 0, 0, 0).color(() -> DemoDoubleSliderNode.INK.withAlpha(super.isEnabled() ? 1F : 0.4F)));
	}

	public static @NonNull DemoDoubleSliderNode create(final double x, final double y, final double width, final double height) {
		return new DemoDoubleSliderNode(x, y, width, height);
	}

}