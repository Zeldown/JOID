package be.zeldown.joid.lib.font.dto.effect;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.font.dto.TextStyle;
import lombok.NonNull;

public interface ITextGlyph {

	public int getIndex();

	public int getCodepoint();

	public double getX();

	public double getBaseline();

	public double getSize();

	public double getAdvance();

	public double getOffsetX();

	public double getOffsetY();

	public double getAscender();

	public double getDescender();

	public double getUnderlineY();

	public double getUnderlineThickness();

	public boolean isShadow();

	public @NonNull Color getColor();

	public @NonNull TextStyle getStyle();

	public boolean hasGlyph(final int codepoint);

	public double getAdvance(final int codepoint);

	public @NonNull ITextGlyph codepoint(final int codepoint);

	public @NonNull ITextGlyph color(final @NonNull Color color);

	public @NonNull ITextGlyph offset(final double x, final double y);

}