package dev.joid.demo.ui.node.impl.control.slider;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.impl.control.slider.IntegerSliderNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import lombok.NonNull;

public class DemoIntegerSliderNode extends IntegerSliderNode {

	private static final Color INK = new Color(153, 153, 153).lock();

	protected DemoIntegerSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.track(RectNode.create(0, 0, 0, 0).color(() -> Color.WHITE.withAlpha(super.isEnabled() ? 1F : 0.4F)));
		super.thumb(RectNode.create(0, 0, 0, 0).color(() -> DemoIntegerSliderNode.INK.withAlpha(super.isEnabled() ? 1F : 0.4F)));
	}

	public static @NonNull DemoIntegerSliderNode create(final double x, final double y, final double width, final double height) {
		return new DemoIntegerSliderNode(x, y, width, height);
	}

}