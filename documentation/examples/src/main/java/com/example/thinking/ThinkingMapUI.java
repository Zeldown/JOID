package com.example.thinking;

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

public class ThinkingMapUI extends UI {

	private static final Color INK      = new Color(153, 153, 153).lock();
	private static final Color LABEL    = new Color(85, 85, 85).lock();
	private static final Color ACCENT   = new Color(124, 92, 255).lock();
	private static final Color BACKDROP = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		RectNode
		.create(390, 250, 1140, 580)
		.color(ThinkingMapUI.BACKDROP)
		.body(card -> {
			ThinkingMapUI.block(40, 40, 300, 100, "Your loop", "events, frame(), present", Color.WHITE, ThinkingMapUI.LABEL).attach(card);
			ThinkingMapUI.block(420, 40, 300, 100, "Backend", "LWJGL 3, Vulkan or LWJGL 2", Color.WHITE, ThinkingMapUI.LABEL).attach(card);
			ThinkingMapUI.block(800, 40, 300, 100, "UI bridge", "routes input, draws the UIs", Color.WHITE, ThinkingMapUI.LABEL).attach(card);
			RectNode
			.create(400, 200, 360, 340)
			.color(ThinkingMapUI.INK)
			.body(frame -> RectNode.create(3, 3, 354, 334).color(ThinkingMapUI.BACKDROP).attach(frame))
			.attach(card);
			ThinkingMapUI.block(490, 220, 180, 56, "UI", "", Color.WHITE, ThinkingMapUI.LABEL).attach(card);
			ThinkingMapUI.block(420, 320, 150, 56, "RectNode", "", Color.WHITE, ThinkingMapUI.LABEL).attach(card);
			ThinkingMapUI.block(590, 320, 150, 56, "TextNode", "", Color.WHITE, ThinkingMapUI.LABEL).attach(card);
			ThinkingMapUI.block(420, 420, 150, 56, "Clicks: 2", "", Color.WHITE, ThinkingMapUI.LABEL).attach(card);
			ThinkingMapUI.label(400, 486, 360, 40, "Design canvas, 1920 x 1080").attach(card);
			ThinkingMapUI.block(40, 420, 260, 56, "clicks = 2", "", ThinkingMapUI.ACCENT, Color.WHITE).attach(card);
			ThinkingMapUI.label(40, 486, 260, 40, "Signal").attach(card);
			RectNode
			.create(800, 200, 260, 180)
			.color(Color.WHITE)
			.effect(RoundedNodeEffect.create(10F))
			.body(screen -> ThinkingMapUI.label(0, 10, 260, 40, "Screen").attach(screen))
			.attach(card);
			ThinkingMapUI.block(880, 300, 220, 120, "Popup", "", ThinkingMapUI.ACCENT, Color.WHITE).attach(card);
			ThinkingMapUI.label(800, 486, 300, 40, "Open UIs, stacked").attach(card);
			ThinkingMapUI.arrow(card, 340D, 90D, 420D, 90D);
			ThinkingMapUI.arrow(card, 720D, 90D, 800D, 90D);
			ThinkingMapUI.arrow(card, 950D, 140D, 950D, 200D);
			ThinkingMapUI.arrow(card, 800D, 248D, 670D, 248D);
			ThinkingMapUI.arrow(card, 580D, 276D, 495D, 320D);
			ThinkingMapUI.arrow(card, 580D, 276D, 665D, 320D);
			ThinkingMapUI.arrow(card, 495D, 376D, 495D, 420D);
			ThinkingMapUI.arrow(card, 300D, 448D, 420D, 448D);
		})
		.attach(this);
	}

	private static TextNode label(final double x, final double y, final double width, final double height, final String text) {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 22F).color(ThinkingMapUI.LABEL);
		return TextNode.create(x, y, width, height).text(Text.create(text, info).align(Align.CENTER));
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

	private static void arrow(final Node node, final double fromX, final double fromY, final double toX, final double toY) {
		final double length = Math.hypot(toX - fromX, toY - fromY);
		final double directionX = (toX - fromX) / length;
		final double directionY = (toY - fromY) / length;
		node.layer((layered, mouseX, mouseY) -> {
			final Point end = Point.of(toX, toY);
			final Point left = Point.of(toX - directionX * 14D - directionY * 8D, toY - directionY * 14D + directionX * 8D);
			final Point right = Point.of(toX - directionX * 14D + directionY * 8D, toY - directionY * 14D - directionX * 8D);
			DrawUtils.SHAPE.drawLine(ThinkingMapUI.INK, 3F, Point.of(fromX, fromY), Point.of(toX - directionX * 10D, toY - directionY * 10D));
			DrawUtils.SHAPE.drawPolygon(ThinkingMapUI.INK, left, end, right);
		});
	}

}