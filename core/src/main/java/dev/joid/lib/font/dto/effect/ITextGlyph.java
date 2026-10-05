package dev.joid.lib.font.dto.effect;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.dto.TextStyle;
import lombok.NonNull;

public interface ITextGlyph {

	public double getX();
	public int getIndex();
	public double getSize();
	public int getCodepoint();
	public double getAdvance();
	public double getAdvance(final int codepoint);
	public double getOffsetX();
	public double getOffsetY();
	public double getAscender();
	public double getBaseline();
	public double getDescender();
	public double getUnderlineY();
	public @NonNull Color getColor();
	public @NonNull TextStyle getStyle();
	public double getUnderlineThickness();

	public boolean isShadow();

	public boolean hasGlyph(final int codepoint);

	public @NonNull ITextGlyph codepoint(final int codepoint);

	public @NonNull ITextGlyph color(final @NonNull Color color);

	public @NonNull ITextGlyph offset(final double x, final double y);

}