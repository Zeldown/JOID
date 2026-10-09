package dev.joid.showcase;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.structure.slider.SliderThumbNode;
import dev.joid.lib.ui.node.impl.structure.slider.impl.DoubleSliderNode;

public class ShowSliderNode extends DoubleSliderNode {

	private final Color to;
	private final Color from;

	protected ShowSliderNode(final double x, final double y, final double width, final Color from, final Color to) {
		super(x, y, width, 36);
		this.from = from;
		this.to = to;
		super.thumb(new Knob());
	}

	public static ShowSliderNode create(final double x, final double y, final double width, final Color from, final Color to) {
		return new ShowSliderNode(x, y, width, from, to);
	}

	@Override
	public void drawSlider(final double mouseX, final double mouseY) {
		final double center = 18D + super.getProgress() * (super.getWidth() - 36D);
		DrawUtils.SHAPE.drawRoundedRect(super.getX(), super.getY() + 13D, super.getWidth(), 10D, Color.WHITE.copyAlpha(0.12F), 5F);
		DrawUtils.SHAPE.drawShadow(super.getX(), super.getY() + 13D, center, 10D, this.to.copyAlpha(0.6F), 5F, 14F);
		DrawUtils.SHAPE.drawRoundedRect(super.getX(), super.getY() + 13D, center, 10D, this.from.toGradient(this.to), 5F);
	}

	private final class Knob extends SliderThumbNode {

		protected Knob() {
			super(36, 36);
		}

		@Override
		public void drawThumb(final double mouseX, final double mouseY) {
			DrawUtils.SHAPE.drawShadow(super.getX(), super.getY(), 36D, 36D, ShowSliderNode.this.to.copyAlpha(0.9F), 18F, 18F);
			DrawUtils.SHAPE.drawCircle(super.getX() + 18D, super.getY() + 18D, Color.WHITE, 18D);
			DrawUtils.SHAPE.drawCircle(super.getX() + 18D, super.getY() + 18D, ShowSliderNode.this.to, 7D);
		}

	}

}