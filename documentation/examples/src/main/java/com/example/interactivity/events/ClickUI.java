package com.example.interactivity.events;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class ClickUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();

	@Override
	public void init() {
		// #region click
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(ClickUI.TEXT);
		final IntegerSignal clicks = new IntegerSignal(0);

		RectNode
		.create(760, 390, 400, 300)
		.color(ClickUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(ClickUI.ELEMENT, 1.5F))
		.body(card -> {
			RectNode
			.create(100, 80, 200, 60)
			.color(ClickUI.ELEMENT)
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