package be.zeldown.joid.demo.ui.slider.node;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.ui.node.impl.structure.slider.SliderCursorNode;
import be.zeldown.joid.lib.ui.node.impl.structure.slider.impl.IntegerSliderNode;
import lombok.NonNull;

public class DemoIntegerSliderNode extends IntegerSliderNode {

	protected DemoIntegerSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.cursor(new Cursor(height, height));
	}

	public static @NonNull DemoIntegerSliderNode create(final double x, final double y, final double width, final double height) {
		return new DemoIntegerSliderNode(x, y, width, height);
	}

	@Override
	public void drawSlider(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.BLACK);
	}

	private final class Cursor extends SliderCursorNode {

		protected Cursor(final double width, final double height) {
			super(width, height);
		}

		@Override
		public void drawCursor(final double mouseX, final double mouseY) {
			DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.RED);
		}

	}

}