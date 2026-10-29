package com.example.text;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.effect.ITextEffect;
import dev.joid.lib.font.effect.ITextGlyph;

public final class RainbowTextEffect implements ITextEffect {

	@Override
	public void apply(final ITextGlyph glyph) {
		final long time = BridgeHandler.CLOCK.get().currentTimeMillis() + glyph.getIndex() * 120L;
		glyph.color(Color.RAINBOW(time).to(Color.WHITE, 0.5F).withAlpha(glyph.getColor().getAlphaF()));
	}

}