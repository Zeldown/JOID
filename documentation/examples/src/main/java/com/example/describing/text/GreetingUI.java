package com.example.describing.text;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class GreetingUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color MUTED   = new Color(139, 147, 163).lock();

	@Override
	public void init() {
		// #region styles
		final TextInfo title = TextInfo.create(ExampleFont.REGULAR, 56F).weight(FontWeight.BOLD).color(GreetingUI.TEXT);
		final TextInfo body = TextInfo.create(ExampleFont.REGULAR, 28F).color(GreetingUI.MUTED);
		final TextInfo caption = body.withFontSize(20F).withLetterSpacing(0.15F);
		// #endregion

		RectNode
		.create(610, 390, 700, 300)
		.color(GreetingUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(GreetingUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region opener
			TextNode.create(50, 50).text(Text.create("Hello JOID", title)).attach(card);
			// #endregion
			TextNode.create(50, 140).text(Text.create("Every text pairs a string with a style.", body)).attach(card);
			TextNode.create(50, 220).text(Text.create("THREE STYLES, ONE FONT", caption)).attach(card);
		})
		.attach(this);
	}

}