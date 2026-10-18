package dev.joid.demo.font.effect;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.font.effect.ITextEffect;
import dev.joid.lib.font.effect.ITextGlyph;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DemoHighlightTextEffect implements ITextEffect {

	private static final Color                   COLOR    = new Color(153, 153, 153, 90).lock();
	private static final DemoHighlightTextEffect INSTANCE = new DemoHighlightTextEffect();

	public static @NonNull DemoHighlightTextEffect inst() {
		return DemoHighlightTextEffect.INSTANCE;
	}

	@Override
	public void background(final @NonNull ITextGlyph glyph) {
		final double top = glyph.getBaseline() - glyph.getAscender();
		DrawUtils.SHAPE.drawRect(glyph.getX(), top, glyph.getAdvance(), glyph.getBaseline() - glyph.getDescender() - top, DemoHighlightTextEffect.COLOR);
	}

}