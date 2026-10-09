package dev.joid.lib.font.impl.bitmap;

import dev.joid.lib.font.impl.glyph.GlyphFont;
import dev.joid.lib.font.impl.glyph.dto.FontFamily;
import dev.joid.lib.font.impl.glyph.dto.IFontFace;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class BitmapFont<F extends IFontFace> extends GlyphFont<F> {

	private final int bitmapSize;

	protected BitmapFont(final @NonNull FontFamily<F> family, final int bitmapSize) {
		super(family);
		if (bitmapSize <= 0) {
			throw new IllegalArgumentException("The bitmap size of a font must be positive: " + bitmapSize);
		}

		this.bitmapSize = bitmapSize;
	}

}