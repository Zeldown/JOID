package dev.joid.lib.font.dto;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.IFont;
import dev.joid.lib.font.dto.effect.ITextEffect;
import dev.joid.lib.font.dto.markup.ITextMarkup;
import dev.joid.lib.font.dto.markup.TextMarkup;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public final class TextInfo {

	private IFont      font;
	private float      fontSize;
	private FontWeight weight;
	private float      letterSpacing;
	private float      lineHeight;
	private Color      color;
	private boolean    colored;
	private boolean    italic;

	private Color shadowColor;
	private Float shadowX;
	private Float shadowY;

	private ITextMarkup[] markups;
	private ITextEffect[] effects;

	public static final @NonNull TextInfo create(final @NonNull IFont font, final float fontSize) {
		return new TextInfo(font, FontWeight.REGULAR, fontSize, Color.BLACK);
	}

	public static final @NonNull TextInfo create(final @NonNull IFont font, final float fontSize, final @NonNull Color color) {
		return new TextInfo(font, FontWeight.REGULAR, fontSize, color);
	}

	public static final @NonNull TextInfo create(final @NonNull IFont font, final @NonNull FontWeight weight, final float fontSize) {
		return new TextInfo(font, weight, fontSize, Color.BLACK);
	}

	public static final @NonNull TextInfo create(final @NonNull IFont font, final @NonNull FontWeight weight, final float fontSize, final @NonNull Color color) {
		return new TextInfo(font, weight, fontSize, color);
	}

	private TextInfo(final IFont font, final FontWeight weight, final float fontSize, final Color color) {
		this(font, fontSize, weight, 0, 0, color, true, false, null, null, null, null, new ITextEffect[0]);
	}

	public final double getHeight() {
		return this.font.getFontProvider().getLineHeight(this);
	}

	public final double getHeight(final @NonNull String text) {
		return this.font.getFontProvider().getHeight(text, this);
	}

	public final @NonNull TextStyle getStyle() {
		return TextStyle.create(this.weight, this.italic, this.color, this.effects);
	}

	public final float getShadowX() {
		return this.shadowX != null ? this.shadowX : this.fontSize / 13.5F;
	}

	public final float getShadowY() {
		return this.shadowY != null ? this.shadowY : this.fontSize / 13.5F;
	}

	public final @NonNull List<ITextEffect> getEffects() {
		return Collections.unmodifiableList(Arrays.asList(this.effects));
	}

	public final @NonNull List<ITextMarkup> getMarkups() {
		return this.markups == null ? TextMarkup.getRegistered() : Collections.unmodifiableList(Arrays.asList(this.markups));
	}

	public final double getWidth(final @NonNull String text) {
		return this.font.getFontProvider().getWidth(text, this);
	}

	public final @NonNull FontBounds getBounds(final @NonNull String text) {
		return new FontBounds(this.getWidth(text), this.getHeight(text));
	}

	public final double dw(final @NonNull String text, final double value) {
		return this.getWidth(text) / value;
	}

	public final double dh(final double value) {
		return this.getHeight() / value;
	}

	public final double dh(final @NonNull String text, final double value) {
		return this.getHeight(text) / value;
	}

	public final double aw(final @NonNull String text, final double value) {
		return this.getWidth(text) + value;
	}

	public final double ah(final double value) {
		return this.getHeight() + value;
	}

	public final double ah(final @NonNull String text, final double value) {
		return this.getHeight(text) + value;
	}

	public final @NonNull TextInfo font(final IFont font) {
		this.font = font;
		return this;
	}

	public final @NonNull TextInfo fontSize(final float fontSize) {
		this.fontSize = fontSize;
		return this;
	}

	public final @NonNull TextInfo weight(final @NonNull FontWeight weight) {
		this.weight = weight;
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

	public final @NonNull TextInfo italic(final boolean italic) {
		this.italic = italic;
		return this;
	}

	public final @NonNull TextInfo markups(final @NonNull ITextMarkup @NonNull... markups) {
		this.markups = markups;
		return this;
	}

	public final @NonNull TextInfo effects(final @NonNull ITextEffect @NonNull... effects) {
		this.effects = effects;
		return this;
	}

	public final @NonNull TextInfo shadow() {
		this.shadowColor = this.color.darker(0.3F);
		return this;
	}

	public final @NonNull TextInfo shadow(final Color color) {
		this.shadowColor = color;
		return this;
	}

	public final @NonNull TextInfo shadow(final float x, final float y) {
		this.shadowX = x;
		this.shadowY = y;
		return this;
	}

	public final @NonNull TextInfo copy() {
		return new TextInfo(this.font, this.fontSize, this.weight, this.letterSpacing, this.lineHeight, this.color, this.colored, this.italic, this.shadowColor, this.shadowX, this.shadowY, this.markups, this.effects);
	}

	@Override
	public String toString() {
		return this.font.toString() + "x" + this.fontSize + " [" + this.color + "]";
	}

}