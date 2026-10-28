package com.example.depth;

import com.example.ExampleFont;
import com.example.capture.CaptureStyle;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.draw.text.TextMode;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.geom.Point;

public abstract class DepthDiagramUI extends UI {

	public static final Color INK      = CaptureStyle.LINE;
	public static final Color NOTE     = CaptureStyle.MUTED;
	public static final Color LABEL    = CaptureStyle.TEXT;
	public static final Color ACCENT   = CaptureStyle.ACCENT;
	public static final Color ELEMENT  = CaptureStyle.STROKE;
	public static final Color SURFACE  = CaptureStyle.SURFACE;
	public static final Color BACKDROP = Color.TRANSPARENT;

	protected final RectNode backdrop(final double x, final double y, final double width, final double height) {
		return RectNode.create(x, y, width, height).color(DepthDiagramUI.BACKDROP);
	}

	protected final TextNode label(final double x, final double y, final double width, final double height, final String text, final Align alignX) {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 22F).color(DepthDiagramUI.LABEL);
		return TextNode.create(x, y, width, height).text(Text.create(text, info).alignX(alignX).alignY(Align.CENTER));
	}

	protected final RectNode box(final double x, final double y, final double width, final double height, final String title, final String detail) {
		return DepthDiagramUI.block(x, y, width, height, title, detail, DepthDiagramUI.SURFACE, DepthDiagramUI.LABEL, DepthDiagramUI.NOTE);
	}

	protected final RectNode subject(final double x, final double y, final double width, final double height, final String title, final String detail) {
		return DepthDiagramUI.block(x, y, width, height, title, detail, DepthDiagramUI.ACCENT, CaptureStyle.ON_ACCENT, CaptureStyle.ON_ACCENT);
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

	private static RectNode block(final double x, final double y, final double width, final double height, final String title, final String detail, final Color fill, final Color color, final Color note) {
		final TextInfo bold = TextInfo.create(ExampleFont.REGULAR, 24F).weight(FontWeight.BOLD).color(color);
		final TextInfo regular = TextInfo.create(ExampleFont.REGULAR, 20F).color(note);
		final double titleHeight = Text.create(title, bold).getHeight();
		final double detailHeight = detail.isEmpty() ? 0D : Text.create(detail, regular).getHeight() * DepthDiagramUI.lines(detail, regular, width - 20D);
		final double top = detail.isEmpty() ? (height - titleHeight) / 2D : (height - titleHeight - 6D - detailHeight) / 2D;
		return RectNode
		.create(x, y, width, height)
		.color(fill)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(fill == DepthDiagramUI.ACCENT ? DepthDiagramUI.ACCENT : DepthDiagramUI.ELEMENT, 1.5F))
		.body(rect -> {
			TextNode.create(0, top, width, titleHeight).text(Text.create(title, bold).align(Align.CENTER)).attach(rect);
			if (!detail.isEmpty()) {
				TextNode.create(10, top + titleHeight + 6D, width - 20D, detailHeight).text(Text.create(detail, regular).alignX(Align.CENTER).alignY(Align.START)).mode(TextMode.SPLIT).attach(rect);
			}
		});
	}

	private static int lines(final String text, final TextInfo info, final double width) {
		final double space = Text.create(" ", info).getWidth();
		int lines = 1;
		double line = 0D;
		for (final String word : text.split(" ")) {
			final double length = Text.create(word, info).getWidth();
			if (line > 0D && line + space + length > width) {
				lines++;
				line = length;
			} else {
				line += line > 0D ? space + length : length;
			}
		}
		return lines;
	}

}