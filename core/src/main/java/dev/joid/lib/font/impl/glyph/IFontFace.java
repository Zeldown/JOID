package dev.joid.lib.font.impl.glyph;

import dev.joid.lib.font.FontWeight;
import lombok.NonNull;

public interface IFontFace {

	public boolean isItalic();

	public float getAscender();
	public float getDescender();
	public float getLineHeight();
	public float getUnderlineY();
	public float getUnderlineThickness();
	public @NonNull String getName();
	public @NonNull FontWeight getWeight();
	public float getAdvance(final int codepoint);
	public float getKerning(final int previous, final int current);

	public boolean hasGlyph(final int codepoint);

}