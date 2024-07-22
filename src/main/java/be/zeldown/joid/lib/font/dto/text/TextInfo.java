package be.zeldown.joid.lib.font.dto.text;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.font.dto.font.IFont;
import be.zeldown.joid.lib.font.dto.text.modifier.ITextModifier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public final class TextInfo {

	private IFont   font;
	private int     fontSize;
	private float   letterSpacing;
	private float   lineHeight;
	private Color   color;
	private boolean colored;

	private Color shadowColor;
	private float shadowX;
	private float shadowY;

	private TextAlign     align;
	private ITextModifier modifier;

	private TextInfo(final @NonNull IFont font, final int fontSize) {
		this(font, fontSize, Color.BLACK);
	}

	private TextInfo(final @NonNull IFont font, final int fontSize, final @NonNull Color color) {
		this(font, fontSize, 0, 0, color, true, null, fontSize/13.5F, fontSize/13.5F, TextAlign.LEFT, null);
	}

	public static final @NonNull TextInfo create(final @NonNull IFont font, final int fontSize) {
		return new TextInfo(font, fontSize);
	}

	public static final @NonNull TextInfo create(final @NonNull IFont font, final int fontSize, final @NonNull Color color) {
		return new TextInfo(font, fontSize, color);
	}

	/* [ Getter Section ] */
	public final double getWidth(final @NonNull String text) {
		return DrawUtils.TEXT.getWidth(text, this);
	}

	public final double getHeight(final @NonNull String text) {
		return DrawUtils.TEXT.getHeight(text, this);
	}

	public final double getHeight() {
		return DrawUtils.TEXT.getLineHeight(this);
	}

	/* [ Builder Section ] */
	public final @NonNull String modify(final @NonNull String text) {
		if (this.modifier != null) {
			return this.modifier.modify(text);
		}

		return text;
	}

	public final @NonNull TextInfo font(final @NonNull IFont font) {
		this.font = font;
		return this;
	}

	public final @NonNull TextInfo fontSize(final int fontSize) {
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

	public final @NonNull TextInfo colored(final boolean colored) {
		this.colored = colored;
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

	public final @NonNull TextInfo align(final @NonNull TextAlign align) {
		this.align = align;
		return this;
	}

	public final @NonNull TextInfo modifier(final @NonNull ITextModifier modifier) {
		this.modifier = modifier;
		return this;
	}

	public final @NonNull TextInfo copy() {
		return new TextInfo(this.font, this.fontSize, this.letterSpacing, this.lineHeight, this.color, this.colored, this.shadowColor, this.shadowX, this.shadowY, this.align, this.modifier);
	}

}