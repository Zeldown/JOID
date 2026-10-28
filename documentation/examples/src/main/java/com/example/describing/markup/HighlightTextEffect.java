package com.example.describing.markup;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.font.effect.ITextEffect;
import dev.joid.lib.font.effect.ITextGlyph;

public final class HighlightTextEffect implements ITextEffect {

	private static final Color HOVER = new Color(54, 61, 77).lock();

	public static final HighlightTextEffect INSTANCE = new HighlightTextEffect();

	private HighlightTextEffect() {}

	// #region background
	@Override
	public void background(final ITextGlyph glyph) {
		final double top = glyph.getBaseline() - glyph.getAscender();
		final double bottom = glyph.getBaseline() - glyph.getDescender();
		DrawUtils.SHAPE.drawRect(glyph.getX(), top, glyph.getAdvance(), bottom - top, HighlightTextEffect.HOVER);
	}
	// #endregion

}