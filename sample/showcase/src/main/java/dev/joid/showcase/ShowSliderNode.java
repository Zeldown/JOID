package dev.joid.showcase;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.control.slider.DoubleSliderNode;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;

public class ShowSliderNode extends DoubleSliderNode {

	private final Color to;
	private final Color from;

	protected ShowSliderNode(final double x, final double y, final double width, final Color from, final Color to) {
		super(x, y, width, 36);
		this.from = from;
		this.to = to;
		super.track(ContainerNode.create(0, 0, 0, 0).layer((node, mouseX, mouseY) -> {
			final double center = 18D + super.getProgress() * (node.getWidth() - 36D);
			DrawUtils.SHAPE.drawRoundedRect(0D, 13D, node.getWidth(), 10D, Color.WHITE.withAlpha(0.12F), 5F);
			DrawUtils.SHAPE.drawShadow(0D, 13D, center, 10D, this.to.withAlpha(0.6F), 5F, 14F);
			DrawUtils.SHAPE.drawRoundedRect(0D, 13D, center, 10D, this.from.toGradient(this.to), 5F);
		}));
		super.thumb(ContainerNode.create(0, 0, 36, 36).layer((node, mouseX, mouseY) -> {
			DrawUtils.SHAPE.drawShadow(0D, 0D, 36D, 36D, this.to.withAlpha(0.9F), 18F, 18F);
			DrawUtils.SHAPE.drawCircle(18D, 18D, Color.WHITE, 18D);
			DrawUtils.SHAPE.drawCircle(18D, 18D, this.to, 7D);
		}));
	}

	public static ShowSliderNode create(final double x, final double y, final double width, final Color from, final Color to) {
		return new ShowSliderNode(x, y, width, from, to);
	}

}