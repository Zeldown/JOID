package dev.joid.lib.font.impl.glyph;

import dev.joid.lib.font.IFontProvider;
import dev.joid.lib.font.impl.glyph.dto.FontFamily;
import dev.joid.lib.font.impl.glyph.dto.IFontFace;
import lombok.NonNull;

public final class SingleFaceFont<F extends IFontFace> extends GlyphFont<F> {

	private SingleFaceFont(final FontFamily<F> family) {
		super(family);
	}

	public static <F extends IFontFace> @NonNull SingleFaceFont<F> of(final @NonNull F face) {
		return new SingleFaceFont<>(FontFamily.of(face));
	}

	@Override
	public @NonNull IFontProvider getFontProvider() {
		throw new UnsupportedOperationException("A single face font of a test has no provider");
	}

}