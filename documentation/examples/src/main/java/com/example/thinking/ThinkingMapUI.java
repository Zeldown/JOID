package com.example.thinking;

import com.example.ExampleFont;
import com.example.capture.CaptureStyle;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.Text;
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

public class ThinkingMapUI extends UI {

	@Override
	public void init() {
		RectNode
		.create(390, 250, 1140, 580)
		.color(Color.TRANSPARENT)
		.body(card -> {
			ThinkingMapUI.block(40, 40, 300, 100, "Your loop", "events, frame(), present", CaptureStyle.SURFACE, CaptureStyle.TEXT).attach(card);
			ThinkingMapUI.block(420, 40, 300, 100, "Backend", "LWJGL 3, Vulkan or LWJGL 2", CaptureStyle.SURFACE, CaptureStyle.TEXT).attach(card);
			ThinkingMapUI.block(800, 40, 300, 100, "UI bridge", "routes input, draws the UIs", CaptureStyle.SURFACE, CaptureStyle.TEXT).attach(card);
			RectNode
			.create(400, 200, 360, 340)
			.color(CaptureStyle.BACKGROUND)
			.effect(RoundedNodeEffect.create(12F))
			.effect(BorderNodeEffect.create(CaptureStyle.MUTED, 2F))
			.attach(card);
			ThinkingMapUI.block(490, 220, 180, 56, "UI", "", CaptureStyle.SURFACE, CaptureStyle.TEXT).attach(card);
			ThinkingMapUI.block(420, 320, 150, 56, "RectNode", "", CaptureStyle.SURFACE, CaptureStyle.TEXT).attach(card);
			ThinkingMapUI.block(590, 320, 150, 56, "TextNode", "", CaptureStyle.SURFACE, CaptureStyle.TEXT).attach(card);
			ThinkingMapUI.block(420, 420, 150, 56, "Clicks: 2", "", CaptureStyle.SURFACE, CaptureStyle.TEXT).attach(card);
			ThinkingMapUI.label(400, 486, 360, 40, "Design canvas, 1920 x 1080").attach(card);
			ThinkingMapUI.block(40, 420, 260, 56, "clicks = 2", "", CaptureStyle.ACCENT, CaptureStyle.ON_ACCENT).attach(card);
			ThinkingMapUI.label(40, 486, 260, 40, "Signal").attach(card);
			RectNode
			.create(800, 200, 260, 180)
			.color(CaptureStyle.SURFACE)
			.effect(RoundedNodeEffect.create(12F))
			.effect(BorderNodeEffect.create(CaptureStyle.STROKE, 1.5F))
			.body(screen -> ThinkingMapUI.label(0, 10, 260, 40, "Screen").attach(screen))
			.attach(card);
			ThinkingMapUI.block(880, 300, 220, 120, "Popup", "", CaptureStyle.ACCENT, CaptureStyle.ON_ACCENT).attach(card);
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
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 22F).color(CaptureStyle.TEXT);
		return TextNode.create(x, y, width, height).text(Text.create(text, info).align(Align.CENTER));
	}

	private static RectNode block(final double x, final double y, final double width, final double height, final String title, final String detail, final Color fill, final Color color) {
		final TextInfo bold = TextInfo.create(ExampleFont.REGULAR, 20F).weight(FontWeight.BOLD).color(color);
		final TextInfo regular = TextInfo.create(ExampleFont.REGULAR, 17F).color(fill == CaptureStyle.ACCENT ? color : CaptureStyle.MUTED);
		return RectNode
		.create(x, y, width, height)
		.color(fill)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(fill == CaptureStyle.ACCENT ? CaptureStyle.ACCENT : CaptureStyle.STROKE, 1.5F))
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
			DrawUtils.SHAPE.drawLine(CaptureStyle.LINE, 3F, Point.of(fromX, fromY), Point.of(toX - directionX * 10D, toY - directionY * 10D));
			DrawUtils.SHAPE.drawPolygon(CaptureStyle.LINE, left, end, right);
		});
	}

}