package com.example.interactivity.events;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class ClickUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		// #region click
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(ClickUI.INK);
		final IntegerSignal clicks = new IntegerSignal(0);

		RectNode
		.create(760, 390, 400, 300)
		.color(ClickUI.PLACEHOLDER)
		.body(card -> {
			RectNode
			.create(100, 80, 200, 60)
			.color(Color.WHITE)
			.onClick(event -> clicks.increment())
			.label(Text.create("Click me", info))
			.attach(card);
			TextNode
			.create(200, 200)
			.text(Text.create("Clicks: " + clicks.get(), info))
			.anchorX(Align.CENTER)
			.attach(card);
		})
		.attach(this);
		// #endregion
	}

}