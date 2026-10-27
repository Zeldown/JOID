package com.example.describing.fonts;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class TitleUI extends UI {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		RectNode
		.create(660, 365, 600, 350)
		.color(TitleUI.PLACEHOLDER)
		.body(card -> {
			// #region weights
			final TextInfo regular = TextInfo.create(AppFonts.MONTSERRAT, 40F).color(TitleUI.INK);
			TextNode.create(50, 50).text(Text.create("Regular 400", regular)).attach(card);
			TextNode.create(50, 130).text(Text.create("Bold 700", regular.withWeight(FontWeight.BOLD))).attach(card);
			TextNode.create(50, 210).text(Text.create("Italic, slanted", regular.withItalic(true))).attach(card);
			// #endregion
		})
		.attach(this);
	}

}