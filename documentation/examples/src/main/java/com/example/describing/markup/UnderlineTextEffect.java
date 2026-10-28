package com.example.describing.markup;

import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.font.effect.ITextEffect;
import dev.joid.lib.font.effect.ITextGlyph;

// #region class
public final class UnderlineTextEffect implements ITextEffect {

	private static final UnderlineTextEffect INSTANCE = new UnderlineTextEffect();

	private UnderlineTextEffect() {}

	public static UnderlineTextEffect inst() {
		return UnderlineTextEffect.INSTANCE;
	}

	@Override
	public void decorate(final ITextGlyph glyph) {
		final double thickness = glyph.getUnderlineThickness();
		DrawUtils.SHAPE.drawRect(glyph.getX(), glyph.getUnderlineY(), glyph.getAdvance(), thickness, glyph.getColor());
	}

}
// #endregion