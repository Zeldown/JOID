package be.zeldown.joid.lib.font.impl.glyph.dto;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.font.dto.TextStyle;
import be.zeldown.joid.lib.font.dto.effect.ITextGlyph;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class TextGlyph<F extends IFontFace> implements ITextGlyph {

	private final F         face;
	private final double    x;
	private final int       index;
	private final double    size;
	private final double    advance;
	private final boolean   shadow;
	private final double    baseline;
	private final TextStyle style;

	private Color  color;
	private int    codepoint;
	private double offsetX;
	private double offsetY;

	private TextGlyph(final F face, final int index, final int codepoint, final TextStyle style, final double x, final double baseline, final double size, final double advance, final Color color, final boolean shadow) {
		this.face = face;
		this.index = index;
		this.codepoint = codepoint;
		this.style = style;
		this.x = x;
		this.baseline = baseline;
		this.size = size;
		this.advance = advance;
		this.color = color;
		this.shadow = shadow;
	}

	public static <F extends IFontFace> @NonNull TextGlyph<F> create(final @NonNull F face, final int index, final int codepoint, final @NonNull TextStyle style, final double x, final double baseline, final double size, final double advance, final @NonNull Color color) {
		return new TextGlyph<>(face, index, codepoint, style, x, baseline, size, advance, color, false);
	}

	public boolean isSlanted() {
		return this.style.isItalic() && !this.face.isItalic();
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
	public double getAdvance(final int codepoint) {
		return this.face.getAdvance(codepoint) * this.size;
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

	public @NonNull TextGlyph<F> shadow(final double x, final double y, final @NonNull Color color) {
		return new TextGlyph<>(this.face, this.index, this.codepoint, this.style, this.x + x, this.baseline + y, this.size, this.advance, color, true).offset(this.offsetX, this.offsetY);
	}

}