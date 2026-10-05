package be.zeldown.joid.lib.font.impl.glyph.dto;

import be.zeldown.joid.lib.font.dto.TextStyle;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class GlyphPlacement<F extends IFontFace> {

	private final int       index;
	private final int       codepoint;
	private final F         face;
	private final double    x;
	private final TextStyle style;

}