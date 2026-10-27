package com.example.describing;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.geom.Point;

public abstract class DiagramUI extends UI {

	public static final Color INK      = new Color(153, 153, 153);
	public static final Color LABEL    = new Color(85, 85, 85);
	public static final Color ACCENT   = new Color(124, 92, 255);
	public static final Color SURFACE  = Color.WHITE;
	public static final Color BACKDROP = new Color(221, 221, 221);

	protected final RectNode backdrop(final double x, final double y, final double width, final double height) {
		return RectNode.create(x, y, width, height).color(DiagramUI.BACKDROP);
	}

	protected final TextNode label(final double x, final double y, final double width, final double height, final String text, final float size, final Color color) {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, size).color(color);
		return TextNode.create(x, y, width, height).text(Text.create(text, info).align(Align.CENTER));
	}

	protected final RectNode box(final double x, final double y, final double width, final double height, final String title, final String detail) {
		return DiagramUI.block(x, y, width, height, title, detail, DiagramUI.SURFACE, DiagramUI.LABEL);
	}

	protected final RectNode subject(final double x, final double y, final double width, final double height, final String title, final String detail) {
		return DiagramUI.block(x, y, width, height, title, detail, DiagramUI.ACCENT, DiagramUI.SURFACE);
	}

	protected final void arrow(final Node node, final double fromX, final double fromY, final double toX, final double toY) {
		final double length = Math.hypot(toX - fromX, toY - fromY);
		final double directionX = (toX - fromX) / length;
		final double directionY = (toY - fromY) / length;
		node.layer((layered, mouseX, mouseY) -> {
			final Point end = Point.of(toX, toY);
			DrawUtils.SHAPE.drawLine(DiagramUI.INK, 3F, Point.of(fromX, fromY), end);
			DrawUtils.SHAPE.drawLine(DiagramUI.INK, 3F, Point.of(toX - directionX * 14D - directionY * 9D, toY - directionY * 14D + directionX * 9D), end, Point.of(toX - directionX * 14D + directionY * 9D, toY - directionY * 14D - directionX * 9D));
		});
	}

	protected final void line(final Node node, final double fromX, final double fromY, final double toX, final double toY) {
		node.layer((layered, mouseX, mouseY) -> DrawUtils.SHAPE.drawLine(DiagramUI.INK, 3F, Point.of(fromX, fromY), Point.of(toX, toY)));
	}

	private static RectNode block(final double x, final double y, final double width, final double height, final String title, final String detail, final Color fill, final Color color) {
		final TextInfo bold = TextInfo.create(ExampleFont.REGULAR, 24F).weight(FontWeight.BOLD).color(color);
		final TextInfo regular = TextInfo.create(ExampleFont.REGULAR, 22F).color(color);
		return RectNode
		.create(x, y, width, height)
		.color(fill)
		.effect(RoundedNodeEffect.create(10F))
		.body(rect -> {
			TextNode.create(0, 0, width, detail.isEmpty() ? height : height / 2D + 4D).text(Text.create(title, bold).alignX(Align.CENTER).alignY(detail.isEmpty() ? Align.CENTER : Align.END)).attach(rect);
			TextNode.create(0, height / 2D + 10D, width, height / 2D - 10D).text(Text.create(detail, regular).alignX(Align.CENTER).alignY(Align.START)).attach(rect);
		});
	}

}