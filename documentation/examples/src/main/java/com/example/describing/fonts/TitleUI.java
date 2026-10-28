package com.example.describing.fonts;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class TitleUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();

	@Override
	public void init() {
		RectNode
		.create(660, 365, 600, 350)
		.color(TitleUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(TitleUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region weights
			final TextInfo regular = TextInfo.create(AppFonts.MONTSERRAT, 40F).color(TitleUI.TEXT);
			TextNode.create(50, 50).text(Text.create("Regular 400", regular)).attach(card);
			TextNode.create(50, 130).text(Text.create("Bold 700", regular.withWeight(FontWeight.BOLD))).attach(card);
			TextNode.create(50, 210).text(Text.create("Italic, slanted", regular.withItalic(true))).attach(card);
			// #endregion
		})
		.attach(this);
	}

}