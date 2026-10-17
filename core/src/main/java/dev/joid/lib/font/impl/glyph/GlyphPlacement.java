package dev.joid.lib.font.impl.glyph;

import dev.joid.lib.font.TextStyle;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlyphPlacement<F extends IFontFace> {

	private final int          index;
	private final int          codepoint;
	private final GlyphFont<F> font;
	private final F            face;
	private final double       x;
	private final TextStyle    style;

	public static <F extends IFontFace> @NonNull GlyphPlacement<F> of(final int index, final int codepoint, final @NonNull GlyphFont<F> font, final @NonNull F face, final double x, final @NonNull TextStyle style) {
		return new GlyphPlacement<>(index, codepoint, font, face, x, style);
	}

}