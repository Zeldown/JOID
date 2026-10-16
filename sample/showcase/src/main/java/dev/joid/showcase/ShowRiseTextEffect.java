package dev.joid.showcase;

import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.font.effect.ITextEffect;
import dev.joid.lib.font.effect.ITextGlyph;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShowRiseTextEffect implements ITextEffect {

	private final long   start;
	private final double stagger;

	public static @NonNull ShowRiseTextEffect create(final long start, final double stagger) {
		return new ShowRiseTextEffect(start, stagger);
	}

	@Override
	public void apply(final @NonNull ITextGlyph glyph) {
		final double elapsed = BridgeHandler.CLOCK.get().currentTimeMillis() - this.start - glyph.getIndex() * this.stagger;
		final float progress = (float) Math.max(0D, Math.min(1D, elapsed / 700D));
		final float eased = TweenEquations.EXPO_OUT.compute(progress);
		glyph.offset(glyph.getOffsetX(), glyph.getOffsetY() + (1D - eased) * glyph.getSize() * 0.6D);
		glyph.color(glyph.getColor().withAlpha(glyph.getColor().getAlphaF() * Math.min(1F, progress * 1.6F)));
	}

}