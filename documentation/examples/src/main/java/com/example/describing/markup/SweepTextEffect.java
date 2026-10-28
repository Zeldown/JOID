package com.example.describing.markup;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.effect.ITextEffect;
import dev.joid.lib.font.effect.ITextGlyph;

// #region class
public final class SweepTextEffect implements ITextEffect {

	private static final Color ACCENT = new Color(91, 140, 255).lock();

	private static final SweepTextEffect INSTANCE = new SweepTextEffect();

	private SweepTextEffect() {}

	public static SweepTextEffect inst() {
		return SweepTextEffect.INSTANCE;
	}

	@Override
	public void apply(final ITextGlyph glyph) {
		final long step = BridgeHandler.CLOCK.get().currentTimeMillis() / 80L % 30L;
		if (Math.abs(step - glyph.getIndex()) < 2L) {
			glyph.color(SweepTextEffect.ACCENT);
		}
	}

}
// #endregion