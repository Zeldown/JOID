package com.example.further;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.draw.text.TextMode;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.geom.Point;

public class DrawingUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color MUTED   = new Color(139, 147, 163).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		final TextInfo title = TextInfo.create(ExampleFont.REGULAR, 40F).color(DrawingUI.TEXT);
		final TextInfo body = TextInfo.create(ExampleFont.REGULAR, 24F).color(DrawingUI.MUTED);
		// #region hint
		final Text hint = Text
		.create("The game waits for you. Press Escape to come back to it.", body)
		.alignX(Align.CENTER);
		// #endregion

		// #region layer
		ContainerNode
		.create(80, 80, 400, 200)
		.layer((node, mouseX, mouseY) -> {
			DrawUtils.SHAPE.drawRoundedRect(0D, 0D, node.getWidth(), node.getHeight(), DrawingUI.SURFACE, 24F);
			DrawUtils.SHAPE.drawCircle(100D, 100D, DrawingUI.ACCENT, 50D);
			DrawUtils.SHAPE.drawRect(180D, 85D, 180D, 30D, DrawingUI.ELEMENT);
		})
		.attach(this);
		// #endregion

		// #region shapes
		ContainerNode
		.create(80, 340, 900, 200)
		.layer((node, mouseX, mouseY) -> {
			DrawUtils.SHAPE.drawShadow(20D, 40D, 120D, 120D, DrawingUI.ACCENT, 16F, 20F);
			DrawUtils.SHAPE.drawRoundedRect(20D, 40D, 120D, 120D, DrawingUI.ELEMENT, 16F);
			DrawUtils.SHAPE.drawRoundedBorder(200D, 40D, 120D, 120D, DrawingUI.ACCENT, 16F, 6D);
			DrawUtils.SHAPE.drawBorder(380D, 40D, 120D, 120D, DrawingUI.ACCENT, 6D);
			DrawUtils.SHAPE.drawCircle(620D, 100D, DrawingUI.ACCENT, 60D);
			DrawUtils.SHAPE.drawPolygon(DrawingUI.MUTED, Point.of(740D, 160D), Point.of(880D, 160D), Point.of(810D, 40D));
		})
		.attach(this);
		// #endregion

		// #region lines
		ContainerNode
		.create(80, 600, 900, 200)
		.layer((node, mouseX, mouseY) -> {
			DrawUtils.SHAPE.drawLine(DrawingUI.ACCENT, 4F, Point.of(20D, 160D), Point.of(120D, 40D), Point.of(220D, 160D));
			DrawUtils.SHAPE.drawDashedLine(DrawingUI.ACCENT, 12, 4F, Point.of(300D, 160D), Point.of(520D, 40D));
			final Point start = Point.of(600D, 160D);
			final Point end = Point.of(880D, 40D);
			DrawUtils.SHAPE.drawCurvedLine(DrawingUI.MUTED, 4F, start, Point.of(700D, 0D), Point.of(780D, 200D), end);
		})
		.attach(this);
		// #endregion

		// #region text
		ContainerNode
		.create(1040, 80, 600, 300)
		.layer((node, mouseX, mouseY) -> {
			DrawUtils.TEXT.drawText(300D, 40D, "Paused", title, Align.CENTER, Align.START);
			DrawUtils.TEXT.drawText(100D, 120D, 400D, 160D, hint, TextMode.SPLIT);
		})
		.attach(this);
		// #endregion
	}

}