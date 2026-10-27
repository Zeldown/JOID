package com.example.text;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.effect.ITextEffect;
import dev.joid.lib.font.effect.ITextGlyph;

public final class RainbowTextEffect implements ITextEffect {

	@Override
	public void apply(final ITextGlyph glyph) {
		glyph.color(Color.RAINBOW(BridgeHandler.CLOCK.get().currentTimeMillis() + glyph.getIndex() * 120L).withAlpha(glyph.getColor().getAlphaF()));
	}

}