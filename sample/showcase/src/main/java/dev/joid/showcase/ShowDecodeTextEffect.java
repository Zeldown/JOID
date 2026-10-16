package dev.joid.showcase;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.effect.ITextEffect;
import dev.joid.lib.font.effect.ITextGlyph;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShowDecodeTextEffect implements ITextEffect {

	private static final String POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789#%&$@";

	private final long   start;
	private final Color  accent;
	private final double stagger;

	public static @NonNull ShowDecodeTextEffect create(final long start, final @NonNull Color accent, final double stagger) {
		return new ShowDecodeTextEffect(start, accent, stagger);
	}

	@Override
	public void apply(final ITextGlyph glyph) {
		if (Character.isWhitespace(glyph.getCodepoint())) {
			return;
		}
		final long now = BridgeHandler.CLOCK.get().currentTimeMillis();
		final double reveal = this.start + glyph.getIndex() * this.stagger;
		if (now >= reveal) {
			final float flash = (float) Math.max(0D, 1D - (now - reveal) / 400D);
			glyph.color(glyph.getColor().to(Color.WHITE.withAlpha(glyph.getColor().getAlphaF()), flash));
			return;
		}
		final long seed = (now / 60L) * 31L + glyph.getIndex() * 17L;
		final double advance = glyph.getAdvance();
		for (int attempt = 0; attempt < ShowDecodeTextEffect.POOL.length(); attempt++) {
			final char candidate = ShowDecodeTextEffect.POOL.charAt((int) ((seed + attempt * 7L) % ShowDecodeTextEffect.POOL.length()));
			if (glyph.hasGlyph(candidate)) {
				glyph.codepoint(candidate).offset(glyph.getOffsetX() + (advance - glyph.getAdvance(candidate)) / 2D, glyph.getOffsetY());
				break;
			}
		}
		glyph.color(this.accent.withAlpha(glyph.getColor().getAlphaF() * (now < this.start ? 0F : 0.85F)));
	}

}