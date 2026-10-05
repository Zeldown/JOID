package dev.joid.lib.font.impl.glyph;

import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.IFont;
import dev.joid.lib.font.impl.glyph.dto.FontFamily;
import dev.joid.lib.font.impl.glyph.dto.IFontFace;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class GlyphFont<F extends IFontFace> implements IFont {

	private final FontFamily<F> family;

	protected GlyphFont(final @NonNull FontFamily<F> family) {
		this.family = family;
	}

	public final @NonNull F getFace(final @NonNull FontWeight weight, final boolean italic) {
		return this.family.resolve(weight, italic);
	}

}