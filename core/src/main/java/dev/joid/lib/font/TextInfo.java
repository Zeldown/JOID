package dev.joid.lib.font;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.effect.ITextEffect;
import dev.joid.lib.font.markup.ITextMarkup;
import dev.joid.lib.font.markup.TextMarkup;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.With;

@Getter
@With(AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextInfo {

	private final IFont      font;
	private final float      fontSize;
	private final FontWeight weight;
	private final float      letterSpacing;
	private final float      lineHeight;
	private final Color      color;
	private final boolean    colored;
	private final boolean    italic;

	private final Color shadowColor;
	private final Float shadowX;
	private final Float shadowY;
	private final Float shadowTint;

	private final ITextMarkup[] markups;
	private final ITextEffect[] effects;

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
		this(font, fontSize, weight, 0, 0, color, true, false, null, null, null, null, null, new ITextEffect[0]);
	}

	public final double getHeight() {
		return this.font.getTextRenderer().getLineHeight(this);
	}

	public final double getHeight(final @NonNull String text) {
		return this.font.getTextRenderer().getHeight(text, this);
	}

	public final @NonNull TextStyle getStyle() {
		return TextStyle.create(this.weight, this.italic, this.color, this.effects).font(this.font);
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
		return this.font.getTextRenderer().getWidth(text, this);
	}

	public final @NonNull FontBounds getBounds(final @NonNull String text) {
		return FontBounds.create(this.getWidth(text), this.getHeight(text));
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
		return this.withFont(font);
	}

	public final @NonNull TextInfo fontSize(final float fontSize) {
		return this.withFontSize(fontSize);
	}

	public final @NonNull TextInfo weight(final @NonNull FontWeight weight) {
		return this.withWeight(weight);
	}

	public final @NonNull TextInfo letterSpacing(final float letterSpacing) {
		return this.withLetterSpacing(letterSpacing);
	}

	public final @NonNull TextInfo lineHeight(final float lineHeight) {
		return this.withLineHeight(lineHeight);
	}

	public final @NonNull TextInfo color(final @NonNull Color color) {
		return this.withColor(color);
	}

	public final @NonNull TextInfo colored(final boolean colored) {
		return this.withColored(colored);
	}

	public final @NonNull TextInfo italic(final boolean italic) {
		return this.withItalic(italic);
	}

	public final @NonNull TextInfo markups(final @NonNull ITextMarkup @NonNull... markups) {
		return this.withMarkups(markups.clone());
	}

	public final @NonNull TextInfo effects(final @NonNull ITextEffect @NonNull... effects) {
		return this.withEffects(effects.clone());
	}

	public final @NonNull TextInfo shadow() {
		return this.withShadowColor(this.color.darker(0.3F));
	}

	public final @NonNull TextInfo shadow(final Color color) {
		return this.withShadowColor(color);
	}

	public final @NonNull TextInfo shadow(final float x, final float y) {
		return this.withShadowX(x).withShadowY(y);
	}

	public final @NonNull TextInfo shadowTint(final Float shadowTint) {
		return this.withShadowTint(shadowTint);
	}

	@Override
	public String toString() {
		return this.font.toString() + "x" + this.fontSize + " [" + this.color + "]";
	}

}