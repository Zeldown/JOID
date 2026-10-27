package com.example.further;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.draw.text.TextMode;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.geom.Point;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class DrawingUI extends UI {

	private static final Color INK   = new Color(153, 153, 153).lock();
	private static final Color DARK  = new Color(85, 85, 85).lock();
	private static final Color LIGHT = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo title = TextInfo.create(ExampleFont.REGULAR, 40F).color(DrawingUI.DARK);
		final TextInfo body = TextInfo.create(ExampleFont.REGULAR, 24F).color(DrawingUI.INK);
		// #region hint
		final Text hint = Text
		.create("The game waits for you. Press Escape to come back to it.", body)
		.alignX(Align.CENTER);
		// #endregion

		// #region layer
		ContainerNode
		.create(80, 80, 400, 200)
		.layer((node, mouseX, mouseY) -> {
			DrawUtils.SHAPE.drawRoundedRect(0D, 0D, node.getWidth(), node.getHeight(), Color.WHITE, 24F);
			DrawUtils.SHAPE.drawCircle(100D, 100D, DrawingUI.INK, 50D);
			DrawUtils.SHAPE.drawRect(180D, 85D, 180D, 30D, DrawingUI.LIGHT);
		})
		.attach(this);
		// #endregion

		// #region shapes
		ContainerNode
		.create(80, 340, 900, 200)
		.layer((node, mouseX, mouseY) -> {
			DrawUtils.SHAPE.drawShadow(20D, 40D, 120D, 120D, DrawingUI.DARK, 16F, 20F);
			DrawUtils.SHAPE.drawRoundedRect(20D, 40D, 120D, 120D, Color.WHITE, 16F);
			DrawUtils.SHAPE.drawRoundedBorder(200D, 40D, 120D, 120D, DrawingUI.INK, 16F, 6D);
			DrawUtils.SHAPE.drawBorder(380D, 40D, 120D, 120D, DrawingUI.INK, 6D);
			DrawUtils.SHAPE.drawCircle(620D, 100D, DrawingUI.INK, 60D);
			DrawUtils.SHAPE.drawPolygon(Color.WHITE, Point.of(740D, 160D), Point.of(880D, 160D), Point.of(810D, 40D));
		})
		.attach(this);
		// #endregion

		// #region lines
		ContainerNode
		.create(80, 600, 900, 200)
		.layer((node, mouseX, mouseY) -> {
			DrawUtils.SHAPE.drawLine(DrawingUI.INK, 4F, Point.of(20D, 160D), Point.of(120D, 40D), Point.of(220D, 160D));
			DrawUtils.SHAPE.drawDashedLine(DrawingUI.INK, 12, 4F, Point.of(300D, 160D), Point.of(520D, 40D));
			final Point start = Point.of(600D, 160D);
			final Point end = Point.of(880D, 40D);
			DrawUtils.SHAPE.drawCurvedLine(Color.WHITE, 4F, start, Point.of(700D, 0D), Point.of(780D, 200D), end);
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