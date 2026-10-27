package com.example.text;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.font.effect.ITextEffect;
import dev.joid.lib.font.effect.ITextGlyph;

public final class HighlightTextEffect implements ITextEffect {

	private static final Color BAND = new Color(85, 85, 85).lock();

	@Override
	public void background(final ITextGlyph glyph) {
		final double top = glyph.getBaseline() - glyph.getAscender();
		DrawUtils.SHAPE.drawRect(glyph.getX(), top, glyph.getAdvance(), glyph.getBaseline() - glyph.getDescender() - top, HighlightTextEffect.BAND);
	}

}