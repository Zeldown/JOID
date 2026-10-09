package dev.joid.demo.ui.slider.node;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.structure.slider.SliderThumbNode;
import dev.joid.lib.ui.node.impl.structure.slider.impl.IntegerSliderNode;
import lombok.NonNull;

public class DemoIntegerSliderNode extends IntegerSliderNode {

	private static final Color INK = new Color(153, 153, 153);

	protected DemoIntegerSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.thumb(new Thumb(height, height));
	}

	public static @NonNull DemoIntegerSliderNode create(final double x, final double y, final double width, final double height) {
		return new DemoIntegerSliderNode(x, y, width, height);
	}

	@Override
	public void drawSlider(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.WHITE.copyAlpha(super.isEnabled() ? 1F : 0.4F));
	}

	private final class Thumb extends SliderThumbNode {

		protected Thumb(final double width, final double height) {
			super(width, height);
		}

		@Override
		public void drawThumb(final double mouseX, final double mouseY) {
			DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), DemoIntegerSliderNode.INK.copyAlpha(super.isEnabled() ? 1F : 0.4F));
		}

	}

}