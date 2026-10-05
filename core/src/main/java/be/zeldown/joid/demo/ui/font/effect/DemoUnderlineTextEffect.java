package be.zeldown.joid.demo.ui.font.effect;

import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.font.dto.effect.ITextEffect;
import be.zeldown.joid.lib.font.dto.effect.ITextGlyph;
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
		final double left = Math.round(glyph.getX());
		final double right = Math.round(glyph.getX() + glyph.getAdvance());
		DrawUtils.SHAPE.drawRect(left, Math.round(glyph.getUnderlineY()), right - left, Math.max(1D, Math.round(glyph.getUnderlineThickness())), glyph.getColor());
	}

}