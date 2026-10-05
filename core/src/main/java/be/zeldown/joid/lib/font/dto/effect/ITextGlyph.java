package be.zeldown.joid.lib.font.dto.effect;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.font.dto.TextStyle;
import lombok.NonNull;

public interface ITextGlyph {

	public double getX();

	public int getIndex();

	public double getSize();

	public int getCodepoint();

	public boolean isShadow();

	public double getAdvance();

	public double getOffsetX();

	public double getOffsetY();

	public double getBaseline();

	public double getAscender();

	public double getDescender();

	public double getUnderlineY();

	public @NonNull Color getColor();

	public @NonNull TextStyle getStyle();

	public double getUnderlineThickness();

	public boolean hasGlyph(final int codepoint);

	public double getAdvance(final int codepoint);

	public @NonNull ITextGlyph codepoint(final int codepoint);

	public @NonNull ITextGlyph color(final @NonNull Color color);

	public @NonNull ITextGlyph offset(final double x, final double y);

}