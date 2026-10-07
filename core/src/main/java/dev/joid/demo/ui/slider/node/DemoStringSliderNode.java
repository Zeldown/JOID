package dev.joid.demo.ui.slider.node;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.structure.slider.SliderCursorNode;
import dev.joid.lib.ui.node.impl.structure.slider.impl.StringSliderNode;
import lombok.NonNull;

public class DemoStringSliderNode extends StringSliderNode {

	private static final Color INK = new Color(153, 153, 153);

	protected DemoStringSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.cursor(new Cursor(height, height));
	}

	public static @NonNull DemoStringSliderNode create(final double x, final double y, final double width, final double height) {
		return new DemoStringSliderNode(x, y, width, height);
	}

	@Override
	public void drawSlider(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.WHITE.copyAlpha(super.isEnabled() ? 1F : 0.4F));
	}

	private final class Cursor extends SliderCursorNode {

		protected Cursor(final double width, final double height) {
			super(width, height);
		}

		@Override
		public void drawCursor(final double mouseX, final double mouseY) {
			DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), DemoStringSliderNode.INK.copyAlpha(super.isEnabled() ? 1F : 0.4F));
		}

	}

}