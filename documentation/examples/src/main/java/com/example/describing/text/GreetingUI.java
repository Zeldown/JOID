package com.example.describing.text;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class GreetingUI extends UI {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color DARK        = new Color(85, 85, 85);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		// #region styles
		final TextInfo title = TextInfo.create(ExampleFont.REGULAR, 56F).weight(FontWeight.BOLD).color(GreetingUI.DARK);
		final TextInfo body = TextInfo.create(ExampleFont.REGULAR, 28F).color(GreetingUI.INK);
		final TextInfo caption = body.withFontSize(20F).withLetterSpacing(0.15F);
		// #endregion

		RectNode
		.create(610, 390, 700, 300)
		.color(GreetingUI.PLACEHOLDER)
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