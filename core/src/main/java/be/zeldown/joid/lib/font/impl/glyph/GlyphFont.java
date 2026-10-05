package be.zeldown.joid.lib.font.impl.glyph;

import be.zeldown.joid.lib.font.FontWeight;
import be.zeldown.joid.lib.font.IFont;
import be.zeldown.joid.lib.font.dto.TextInfo;
import be.zeldown.joid.lib.font.impl.glyph.dto.FontFamily;
import be.zeldown.joid.lib.font.impl.glyph.dto.IGlyphFace;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class GlyphFont<F extends IGlyphFace> implements IFont {

	private final FontFamily<F> family;

	protected GlyphFont(final @NonNull FontFamily<F> family) {
		this.family = family;
	}

	public final @NonNull TextInfo info(final float fontSize) {
		return TextInfo.create(this, fontSize);
	}

	public final @NonNull F getFace(final @NonNull FontWeight weight, final boolean italic) {
		return this.family.resolve(weight, italic);
	}

}