package com.example.depth;

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

public abstract class DepthDiagramUI extends UI {

	public static final Color INK      = new Color(153, 153, 153).lock();
	public static final Color LABEL    = new Color(85, 85, 85).lock();
	public static final Color ACCENT   = new Color(124, 92, 255).lock();
	public static final Color SURFACE  = Color.WHITE;
	public static final Color BACKDROP = new Color(221, 221, 221).lock();

	protected final RectNode backdrop(final double x, final double y, final double width, final double height) {
		return RectNode.create(x, y, width, height).color(DepthDiagramUI.BACKDROP);
	}

	protected final TextNode label(final double x, final double y, final double width, final double height, final String text, final Align alignX) {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 22F).color(DepthDiagramUI.LABEL);
		return TextNode.create(x, y, width, height).text(Text.create(text, info).alignX(alignX).alignY(Align.CENTER));
	}

	protected final RectNode box(final double x, final double y, final double width, final double height, final String title, final String detail) {
		return DepthDiagramUI.block(x, y, width, height, title, detail, DepthDiagramUI.SURFACE, DepthDiagramUI.LABEL);
	}

	protected final RectNode subject(final double x, final double y, final double width, final double height, final String title, final String detail) {
		return DepthDiagramUI.block(x, y, width, height, title, detail, DepthDiagramUI.ACCENT, DepthDiagramUI.SURFACE);
	}

	protected final void arrow(final Node node, final double fromX, final double fromY, final double toX, final double toY) {
		final double length = Math.hypot(toX - fromX, toY - fromY);
		final double directionX = (toX - fromX) / length;
		final double directionY = (toY - fromY) / length;
		node.layer((layered, mouseX, mouseY) -> {
			final Point end = Point.of(toX, toY);
			final Point left = Point.of(toX - directionX * 14D - directionY * 8D, toY - directionY * 14D + directionX * 8D);
			final Point right = Point.of(toX - directionX * 14D + directionY * 8D, toY - directionY * 14D - directionX * 8D);
			DrawUtils.SHAPE.drawLine(DepthDiagramUI.INK, 3F, Point.of(fromX, fromY), Point.of(toX - directionX * 10D, toY - directionY * 10D));
			DrawUtils.SHAPE.drawPolygon(DepthDiagramUI.INK, left, end, right);
		});
	}

	private static RectNode block(final double x, final double y, final double width, final double height, final String title, final String detail, final Color fill, final Color color) {
		final TextInfo bold = TextInfo.create(ExampleFont.REGULAR, 20F).weight(FontWeight.BOLD).color(color);
		final TextInfo regular = TextInfo.create(ExampleFont.REGULAR, 17F).color(color);
		return RectNode
		.create(x, y, width, height)
		.color(fill)
		.effect(RoundedNodeEffect.create(10F))
		.body(rect -> {
			if (detail.isEmpty()) {
				TextNode.create(0, 0, width, height).text(Text.create(title, bold).align(Align.CENTER)).attach(rect);
			} else {
				TextNode.create(0, 0, width, height / 2D + 2D).text(Text.create(title, bold).alignX(Align.CENTER).alignY(Align.END)).attach(rect);
				TextNode.create(0, height / 2D + 8D, width, height / 2D - 8D).text(Text.create(detail, regular).alignX(Align.CENTER).alignY(Align.START)).attach(rect);
			}
		});
	}

}