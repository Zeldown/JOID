package be.zeldown.joid.demo.ui.font.effect;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.font.dto.effect.ITextEffect;
import be.zeldown.joid.lib.font.dto.effect.ITextGlyph;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DemoHighlightTextEffect implements ITextEffect {

	private static final Color                   COLOR    = new Color(255, 214, 0, 110);
	private static final DemoHighlightTextEffect INSTANCE = new DemoHighlightTextEffect();

	public static @NonNull DemoHighlightTextEffect inst() {
		return DemoHighlightTextEffect.INSTANCE;
	}

	@Override
	public void background(final @NonNull ITextGlyph glyph) {
		final double left = Math.round(glyph.getX());
		final double right = Math.round(glyph.getX() + glyph.getAdvance());
		final double top = Math.round(glyph.getBaseline() - glyph.getAscender());
		DrawUtils.SHAPE.drawRect(left, top, right - left, Math.round(glyph.getBaseline() - glyph.getDescender()) - top, DemoHighlightTextEffect.COLOR);
	}

}