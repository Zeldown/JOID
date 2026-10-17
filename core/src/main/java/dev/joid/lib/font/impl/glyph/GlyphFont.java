package dev.joid.lib.font.impl.glyph;

import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.IFont;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class GlyphFont<F extends IFontFace> implements IFont {

	@NonNull
	private final FontFamily<F> family;

	public final @NonNull F getFace(final @NonNull FontWeight weight, final boolean italic) {
		return this.family.resolve(weight, italic);
	}

}