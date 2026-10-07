package dev.joid.showcase;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.dto.effect.ITextEffect;
import dev.joid.lib.font.dto.effect.ITextGlyph;

public class ShowDecodeTextEffect implements ITextEffect {

	private static final String POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789#%&$@";

	private final long   start;
	private final double stagger;
	private final Color  accent;

	public ShowDecodeTextEffect(final long start, final double stagger, final Color accent) {
		this.start = start;
		this.stagger = stagger;
		this.accent = accent;
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
			glyph.color(glyph.getColor().to(Color.WHITE.copyAlpha(glyph.getColor().a), flash));
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
		glyph.color(this.accent.copyAlpha(glyph.getColor().a * (now < this.start ? 0F : 0.85F)));
	}

}