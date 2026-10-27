package com.example.describing.markup;

import dev.joid.lib.font.effect.ITextEffect;
import dev.joid.lib.font.effect.ITextGlyph;

// #region class
public final class WaveTextEffect implements ITextEffect {

	@Override
	public void apply(final ITextGlyph glyph) {
		glyph.offset(0D, Math.sin(glyph.getIndex() * 0.6D) * glyph.getSize() / 8D);
	}

}
// #endregion