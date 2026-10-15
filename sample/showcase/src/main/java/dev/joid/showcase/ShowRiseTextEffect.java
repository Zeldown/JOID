package dev.joid.showcase;

import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.font.effect.ITextEffect;
import dev.joid.lib.font.effect.ITextGlyph;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ShowRiseTextEffect implements ITextEffect {

	private final long   start;
	private final double stagger;

	@Override
	public void apply(final ITextGlyph glyph) {
		final double elapsed = BridgeHandler.CLOCK.get().currentTimeMillis() - this.start - glyph.getIndex() * this.stagger;
		final float progress = (float) Math.max(0D, Math.min(1D, elapsed / 700D));
		final float eased = TweenEquations.EXPO_OUT.compute(progress);
		glyph.offset(glyph.getOffsetX(), glyph.getOffsetY() + (1D - eased) * glyph.getSize() * 0.6D);
		glyph.color(glyph.getColor().withAlpha(glyph.getColor().getAlphaF() * Math.min(1F, progress * 1.6F)));
	}

}