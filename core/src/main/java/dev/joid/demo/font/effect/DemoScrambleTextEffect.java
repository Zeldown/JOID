package dev.joid.demo.font.effect;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.font.effect.ITextEffect;
import dev.joid.lib.font.effect.ITextGlyph;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DemoScrambleTextEffect implements ITextEffect {

	private static final String                 POOL     = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static final DemoScrambleTextEffect INSTANCE = new DemoScrambleTextEffect();

	public static @NonNull DemoScrambleTextEffect inst() {
		return DemoScrambleTextEffect.INSTANCE;
	}

	@Override
	public void apply(final @NonNull ITextGlyph glyph) {
		if (Character.isWhitespace(glyph.getCodepoint())) {
			return;
		}

		final double advance = glyph.getAdvance(glyph.getCodepoint());
		int candidates = 0;
		for (int i = 0; i < DemoScrambleTextEffect.POOL.length(); i++) {
			final char candidate = DemoScrambleTextEffect.POOL.charAt(i);
			if (DemoScrambleTextEffect.isCandidate(glyph, candidate, advance)) {
				candidates++;
			}
		}

		if (candidates == 0) {
			return;
		}

		final long step = BridgeHandler.CLOCK.get().currentTimeMillis() / 80L;
		final long seed = (step * 0x9E3779B97F4A7C15L + glyph.getIndex()) * 0xBF58476D1CE4E5B9L;
		int remaining = (int) ((seed >>> 33) % candidates);
		for (int i = 0; i < DemoScrambleTextEffect.POOL.length(); i++) {
			final char candidate = DemoScrambleTextEffect.POOL.charAt(i);
			if (DemoScrambleTextEffect.isCandidate(glyph, candidate, advance) && remaining-- == 0) {
				glyph.codepoint(candidate).offset(glyph.getOffsetX() + (advance - glyph.getAdvance(candidate)) / 2D, glyph.getOffsetY());
				return;
			}
		}
	}

	private static boolean isCandidate(final ITextGlyph glyph, final char candidate, final double advance) {
		return glyph.hasGlyph(candidate) && Math.abs(glyph.getAdvance(candidate) - advance) <= advance * 0.25D;
	}

}