package dev.joid.lib.font.impl.glyph;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.TextStyle;
import dev.joid.lib.font.effect.ITextGlyph;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextGlyph<F extends IFontFace> implements ITextGlyph {

	private final F            face;
	private final double       x;
	private final int          index;
	private final double       size;
	private final double       advance;
	private final boolean      shadow;
	private final double       baseline;
	private final TextStyle    style;
	private final GlyphFont<F> font;

	private Color  color;
	private int    codepoint;
	private double offsetX;
	private double offsetY;

	public static <F extends IFontFace> @NonNull TextGlyph<F> create(final @NonNull GlyphFont<F> font, final @NonNull F face, final int index, final int codepoint, final @NonNull TextStyle style, final double x, final double baseline, final double size, final double advance, final @NonNull Color color) {
		return new TextGlyph<>(face, x, index, size, advance, false, baseline, style, font, color, codepoint, 0D, 0D);
	}

	public @NonNull TextGlyph<F> shadow(final double x, final double y, final @NonNull Color color) {
		return new TextGlyph<>(this.face, this.x + x, this.index, this.size, this.advance, true, this.baseline + y, this.style, this.font, color, this.codepoint, this.offsetX, this.offsetY);
	}

	public boolean isSlanted() {
		return this.style.isItalic() && !this.face.isItalic();
	}

	@Override
	public double getAdvance(final int codepoint) {
		return this.face.getAdvance(codepoint) * this.size;
	}

	@Override
	public double getAscender() {
		return this.face.getAscender() * this.size;
	}

	@Override
	public double getDescender() {
		return this.face.getDescender() * this.size;
	}

	@Override
	public double getUnderlineY() {
		return this.baseline - this.face.getUnderlineY() * this.size;
	}

	@Override
	public double getUnderlineThickness() {
		return this.face.getUnderlineThickness() * this.size;
	}

	@Override
	public boolean hasGlyph(final int codepoint) {
		return this.face.hasGlyph(codepoint);
	}

	@Override
	public @NonNull TextGlyph<F> codepoint(final int codepoint) {
		this.codepoint = codepoint;
		return this;
	}

	@Override
	public @NonNull TextGlyph<F> color(final @NonNull Color color) {
		this.color = color;
		return this;
	}

	@Override
	public @NonNull TextGlyph<F> offset(final double x, final double y) {
		this.offsetX = x;
		this.offsetY = y;
		return this;
	}

}