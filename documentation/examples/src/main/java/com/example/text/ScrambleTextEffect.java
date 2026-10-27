package com.example.text;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.font.effect.ITextEffect;
import dev.joid.lib.font.effect.ITextGlyph;

public final class ScrambleTextEffect implements ITextEffect {

	private static final String POOL = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	@Override
	public void apply(final ITextGlyph glyph) {
		final long time = BridgeHandler.CLOCK.get().currentTimeMillis();
		if (Character.isWhitespace(glyph.getCodepoint()) || time % 2000L >= 300L + glyph.getIndex() * 45L) {
			return;
		}

		final double advance = glyph.getAdvance(glyph.getCodepoint());
		int candidates = 0;
		for (int i = 0; i < ScrambleTextEffect.POOL.length(); i++) {
			if (this.fits(glyph, ScrambleTextEffect.POOL.charAt(i), advance)) {
				candidates++;
			}
		}
		if (candidates == 0) {
			return;
		}

		final long step = time / 80L;
		final long seed = (step * 0x9E3779B97F4A7C15L + glyph.getIndex()) * 0xBF58476D1CE4E5B9L;
		int remaining = (int) ((seed >>> 33) % candidates);
		for (int i = 0; i < ScrambleTextEffect.POOL.length(); i++) {
			final char candidate = ScrambleTextEffect.POOL.charAt(i);
			if (this.fits(glyph, candidate, advance) && remaining-- == 0) {
				glyph.codepoint(candidate).offset(glyph.getOffsetX() + (advance - glyph.getAdvance(candidate)) / 2D, glyph.getOffsetY());
				return;
			}
		}
	}

	private boolean fits(final ITextGlyph glyph, final char candidate, final double advance) {
		return glyph.hasGlyph(candidate) && Math.abs(glyph.getAdvance(candidate) - advance) <= advance * 0.25D;
	}

}