package dev.joid.demo.ui.font.effect;

import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.font.effect.ITextEffect;
import dev.joid.lib.font.effect.ITextGlyph;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DemoUnderlineTextEffect implements ITextEffect {

	private static final DemoUnderlineTextEffect INSTANCE = new DemoUnderlineTextEffect();

	public static @NonNull DemoUnderlineTextEffect inst() {
		return DemoUnderlineTextEffect.INSTANCE;
	}

	@Override
	public void decorate(final @NonNull ITextGlyph glyph) {
		DrawUtils.SHAPE.drawRect(glyph.getX(), glyph.getUnderlineY(), glyph.getAdvance(), glyph.getUnderlineThickness(), glyph.getColor());
	}

}