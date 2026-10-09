package dev.joid.lib.font.impl.glyph;

import dev.joid.lib.font.ITextRenderer;
import lombok.NonNull;

public final class SingleFaceFont<F extends IFontFace> extends GlyphFont<F> {

	private SingleFaceFont(final FontFamily<F> family) {
		super(family);
	}

	public static <F extends IFontFace> @NonNull SingleFaceFont<F> of(final @NonNull F face) {
		return new SingleFaceFont<>(FontFamily.of(face));
	}

	@Override
	public @NonNull ITextRenderer getTextRenderer() {
		throw new UnsupportedOperationException("A single face font of a test has no renderer");
	}

}