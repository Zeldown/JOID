package dev.joid.demo.ui.slider.node;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.structure.slider.SliderCursorNode;
import dev.joid.lib.ui.node.impl.structure.slider.impl.IntegerSliderNode;
import lombok.NonNull;

public class DemoIntegerSliderNode extends IntegerSliderNode {

	private static final Color INK = new Color(153, 153, 153);

	protected DemoIntegerSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.cursor(new Cursor(height, height));
	}

	public static @NonNull DemoIntegerSliderNode create(final double x, final double y, final double width, final double height) {
		return new DemoIntegerSliderNode(x, y, width, height);
	}

	@Override
	public void drawSlider(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.WHITE);
	}

	private final class Cursor extends SliderCursorNode {

		protected Cursor(final double width, final double height) {
			super(width, height);
		}

		@Override
		public void drawCursor(final double mouseX, final double mouseY) {
			DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), DemoIntegerSliderNode.INK);
		}

	}

}