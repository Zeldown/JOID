package com.example.interactivity.hover;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class EnterUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24).color(EnterUI.INK);

		RectNode
		.create(660, 340, 600, 400)
		.color(EnterUI.PLACEHOLDER)
		.body(card -> {
			// #region events
			final IntegerSignal entries = new IntegerSignal(0);
			final DoubleSignal dotX = new DoubleSignal(150D);
			final DoubleSignal dotY = new DoubleSignal(100D);

			RectNode
			.create(60, 40, 300, 200)
			.color(Color.WHITE)
			.onHoverStart(event -> entries.increment())
			.onHover(event -> {
				dotX.set(event.getMouseX() - event.getNode().getAbsoluteX());
				dotY.set(event.getMouseY() - event.getNode().getAbsoluteY());
			})
			.onHoverEnd(event -> {
				dotX.set(150D);
				dotY.set(100D);
			})
			.body(area -> {
				CircleNode
				.create(0, 0, 20)
				.color(EnterUI.INK)
				.x(dotX.get() - 10D)
				.y(dotY.get() - 10D)
				.interactive(false)
				.attach(area);
			})
			.attach(card);
			// #endregion
			TextNode.create(60, 290).text(Text.create("Entries: " + entries.get(), info)).attach(card);
		})
		.attach(this);
	}

}