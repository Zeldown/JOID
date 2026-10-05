package be.zeldown.joid.demo.ui.font.effect;

import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.font.dto.effect.ITextEffect;
import be.zeldown.joid.lib.font.dto.effect.ITextGlyph;
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
		final int start = (int) Math.floorMod(BridgeHandler.CLOCK.get().currentTimeMillis() / 80L * 31L + glyph.getIndex() * 17L, DemoScrambleTextEffect.POOL.length());
		for (int i = 0; i < DemoScrambleTextEffect.POOL.length(); i++) {
			final char candidate = DemoScrambleTextEffect.POOL.charAt((start + i) % DemoScrambleTextEffect.POOL.length());
			if (glyph.hasGlyph(candidate) && Math.abs(glyph.getAdvance(candidate) - advance) < glyph.getSize() / 20D) {
				glyph.codepoint(candidate);
				return;
			}
		}
	}

}