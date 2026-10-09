package dev.joid.lib.font.impl.glyph;

import dev.joid.lib.font.TextStyle;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class GlyphPlacement<F extends IFontFace> {

	private final int          index;
	private final int          codepoint;
	private final GlyphFont<F> font;
	private final F            face;
	private final double       x;
	private final TextStyle    style;

}