package dev.joid.demo.ui.node.impl.control.slider;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.impl.control.slider.StringSliderNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import lombok.NonNull;

public class DemoStringSliderNode extends StringSliderNode {

	private static final Color INK = new Color(153, 153, 153).lock();

	protected DemoStringSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.track(RectNode.create(0, 0, 0, 0).color(() -> Color.WHITE.withAlpha(super.isEnabled() ? 1F : 0.4F)));
		super.thumb(RectNode.create(0, 0, 0, 0).color(() -> DemoStringSliderNode.INK.withAlpha(super.isEnabled() ? 1F : 0.4F)));
	}

	public static @NonNull DemoStringSliderNode create(final double x, final double y, final double width, final double height) {
		return new DemoStringSliderNode(x, y, width, height);
	}

}