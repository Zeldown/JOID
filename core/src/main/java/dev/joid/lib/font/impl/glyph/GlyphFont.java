package dev.joid.lib.font.impl.glyph;

import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.IFont;
import dev.joid.lib.font.impl.glyph.dto.FontFamily;
import dev.joid.lib.font.impl.glyph.dto.IFontFace;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class GlyphFont<F extends IFontFace> implements IFont {

	private final int           bitmapSize;
	private final FontFamily<F> family;

	protected GlyphFont(final @NonNull FontFamily<F> family) {
		this(family, 0);
	}

	protected GlyphFont(final @NonNull FontFamily<F> family, final int bitmapSize) {
		if (bitmapSize < 0) {
			throw new IllegalArgumentException("The bitmap size of a font cannot be negative: " + bitmapSize);
		}

		this.family = family;
		this.bitmapSize = bitmapSize;
	}

	public final boolean isBitmap() {
		return this.bitmapSize > 0;
	}

	public final @NonNull F getFace(final @NonNull FontWeight weight, final boolean italic) {
		return this.family.resolve(weight, italic);
	}

	public final float snapSize(final float size, final double scale) {
		if (!this.isBitmap() || size <= 0F || scale <= 0D) {
			return size;
		}

		final double texel = Math.max(1D, Math.floor(size * scale / this.bitmapSize + 0.5D + 1E-6D));
		return (float) (texel * this.bitmapSize / scale);
	}

}