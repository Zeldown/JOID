package com.example.describing.text;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.draw.text.TextMode;
import dev.joid.lib.draw.text.TextOverflow;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class ModesUI extends UI {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color DARK        = new Color(85, 85, 85);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(ModesUI.INK);
		final TextInfo caption = TextInfo.create(ExampleFont.REGULAR, 20F).color(ModesUI.DARK);
		final String sentence = "A sentence too long for the width of its box, in four modes.";

		RectNode
		.create(460, 290, 1000, 500)
		.color(ModesUI.PLACEHOLDER)
		.body(card -> {
			RectNode.create(40, 60, 440, 40).color(Color.WHITE).attach(card);
			RectNode.create(520, 60, 440, 40).color(Color.WHITE).attach(card);
			RectNode.create(40, 220, 440, 40).color(Color.WHITE).attach(card);
			RectNode.create(40, 380, 440, 40).color(Color.WHITE).attach(card);
			// #region modes
			TextNode
			.create(40, 60, 440, 40)
			.text(Text.create(sentence, info).overflow(TextOverflow.ELLIPSIS))
			.mode(TextMode.OVERFLOW)
			.attach(card);
			TextNode.create(520, 60, 440, 40).text(Text.create(sentence, info)).mode(TextMode.SPLIT).attach(card);
			TextNode.create(40, 220, 440, 40).text(Text.create(sentence, info)).mode(TextMode.BOX).attach(card);
			TextNode.create(40, 380, 440, 40).text(Text.create(sentence, info)).attach(card);
			// #endregion
			TextNode.create(40, 20).text(Text.create("OVERFLOW", caption)).attach(card);
			TextNode.create(520, 20).text(Text.create("SPLIT", caption)).attach(card);
			TextNode.create(40, 180).text(Text.create("BOX", caption)).attach(card);
			TextNode.create(40, 340).text(Text.create("NORMAL", caption)).attach(card);
		})
		.attach(this);
	}

}