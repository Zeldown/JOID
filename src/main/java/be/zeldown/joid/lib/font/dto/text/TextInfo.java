package be.zeldown.joid.lib.font.dto.text;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.font.dto.font.FontBounds;
import be.zeldown.joid.lib.font.dto.font.IFont;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public final class TextInfo {

	private IFont   font;
	private float   fontSize;
	private float   letterSpacing;
	private float   lineHeight;
	private Color   color;
	private boolean italic;

	private Color shadowColor;
	private float shadowX;
	private float shadowY;

	private TextInfo(final IFont font, final float fontSize) {
		this(font, fontSize, Color.BLACK);
	}

	private TextInfo(final IFont font, final float fontSize, final Color color) {
		this(font, fontSize, 0F, 0F, color, false, null, fontSize / 13.5F, fontSize / 13.5F);
	}

	public static final @NonNull TextInfo create(final @NonNull IFont font, final float fontSize) {
		return new TextInfo(font, fontSize);
	}

	public static final @NonNull TextInfo create(final @NonNull IFont font, final float fontSize, final @NonNull Color color) {
		return new TextInfo(font, fontSize, color);
	}

	/* [ Getter Section ] */
	public final @NonNull FontBounds getBounds(final @NonNull String text) {
		return new FontBounds(this.getWidth(text), this.getHeight(text));
	}

	public final double getWidth(final @NonNull String text) {
		return this.font.getFontProvider().getWidth(text, this);
	}

	public final double getHeight(final @NonNull String text) {
		return this.font.getFontProvider().getHeight(text, this);
	}

	public final double getHeight() {
		return this.font.getFontProvider().getLineHeight(this);
	}

	public final double dw(final @NonNull String text, final double value) {
		return this.getWidth(text) / value;
	}

	public final double dh(final @NonNull String text, final double value) {
		return this.getHeight(text) / value;
	}

	public final double dh(final double value) {
		return this.getHeight() / value;
	}

	public final double aw(final @NonNull String text, final double value) {
		return this.getWidth(text) + value;
	}

	public final double ah(final @NonNull String text, final double value) {
		return this.getHeight(text) + value;
	}

	public final double ah(final double value) {
		return this.getHeight() + value;
	}

	/* [ Builder Section ] */
	public final @NonNull TextInfo font(final IFont font) {
		this.font = font;
		return this;
	}

	public final @NonNull TextInfo fontSize(final float fontSize) {
		this.fontSize = fontSize;
		return this;
	}

	public final @NonNull TextInfo letterSpacing(final float letterSpacing) {
		this.letterSpacing = letterSpacing;
		return this;
	}

	public final @NonNull TextInfo lineHeight(final float lineHeight) {
		this.lineHeight = lineHeight;
		return this;
	}

	public final @NonNull TextInfo color(final @NonNull Color color) {
		this.color = color;
		return this;
	}

	public final @NonNull TextInfo italic(final boolean italic) {
		this.italic = italic;
		return this;
	}

	public final @NonNull TextInfo shadow(final Color color) {
		this.shadowColor = color;
		return this;
	}

	public final @NonNull TextInfo shadow() {
		this.shadowColor = this.color.darker(0.3F);
		return this;
	}

	public final @NonNull TextInfo shadow(final float x, final float y) {
		this.shadowX = x;
		this.shadowY = y;
		return this;
	}

	public final @NonNull TextInfo copy() {
		return new TextInfo(this.font, this.fontSize, this.letterSpacing, this.lineHeight, this.color, this.italic, this.shadowColor, this.shadowX, this.shadowY);
	}

}