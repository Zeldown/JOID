package com.example.describing.markup;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextStyle;
import dev.joid.lib.font.markup.ITextMarkup;

// #region class
public final class TagTextMarkup implements ITextMarkup {

	private static final Color ACCENT = new Color(91, 140, 255).lock();

	@Override
	public int parse(final String text, final int index, final TextStyle style) {
		if (text.startsWith("<b>", index)) {
			style.weight(FontWeight.BOLD);
			return 3;
		}
		if (text.startsWith("</b>", index)) {
			style.weight(style.getBase().getWeight());
			return 4;
		}
		if (text.startsWith("<em>", index)) {
			style.color(TagTextMarkup.ACCENT).effect(UnderlineTextEffect.INSTANCE);
			return 4;
		}
		if (text.startsWith("</em>", index)) {
			style.color(style.getBase().getColor()).removeEffect(UnderlineTextEffect.INSTANCE);
			return 5;
		}
		return 0;
	}

}
// #endregion