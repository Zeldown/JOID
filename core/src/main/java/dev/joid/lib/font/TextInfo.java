package dev.joid.lib.font;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.effect.ITextEffect;
import dev.joid.lib.font.markup.ITextMarkup;
import dev.joid.lib.font.markup.TextMarkup;
import dev.joid.lib.utils.lock.LockableValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextInfo extends LockableValue<TextInfo> {

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
	private Float shadowTint;

	private ITextMarkup[] markups;
	private ITextEffect[] effects;

	public static @NonNull TextInfo create(final @NonNull IFont font, final float fontSize) {
		return new TextInfo(font, FontWeight.REGULAR, fontSize, Color.BLACK);
	}

	public static @NonNull TextInfo create(final @NonNull IFont font, final float fontSize, final @NonNull Color color) {
		return new TextInfo(font, FontWeight.REGULAR, fontSize, color);
	}

	public static @NonNull TextInfo create(final @NonNull IFont font, final @NonNull FontWeight weight, final float fontSize) {
		return new TextInfo(font, weight, fontSize, Color.BLACK);
	}

	public static @NonNull TextInfo create(final @NonNull IFont font, final @NonNull FontWeight weight, final float fontSize, final @NonNull Color color) {
		return new TextInfo(font, weight, fontSize, color);
	}

	private TextInfo(final IFont font, final FontWeight weight, final float fontSize, final Color color) {
		this(font, fontSize, weight, 0F, 0F, color, true, false, null, null, null, null, null, new ITextEffect[0]);
	}

	public double getHeight() {
		return this.font.getTextRenderer().getLineHeight(this);
	}

	public double getHeight(final @NonNull String text) {
		return this.font.getTextRenderer().getHeight(text, this);
	}

	public double getBaseline() {
		return this.font.getTextRenderer().getBaseline(this);
	}

	public @NonNull TextStyle getStyle() {
		return TextStyle.create(this.weight, this.italic, this.color, this.effects).font(this.font);
	}

	public float getShadowX() {
		return this.shadowX != null ? this.shadowX : this.fontSize / 13.5F;
	}

	public float getShadowY() {
		return this.shadowY != null ? this.shadowY : this.fontSize / 13.5F;
	}

	public @NonNull List<ITextEffect> getEffects() {
		return Collections.unmodifiableList(Arrays.asList(this.effects));
	}

	public @NonNull List<ITextMarkup> getMarkups() {
		return this.markups == null ? TextMarkup.getRegistered() : Collections.unmodifiableList(Arrays.asList(this.markups));
	}

	public double getWidth(final @NonNull String text) {
		return this.font.getTextRenderer().getWidth(text, this);
	}

	public @NonNull FontBounds getBounds(final @NonNull String text) {
		return FontBounds.create(this.getWidth(text), this.getHeight(text));
	}

	public double dw(final @NonNull String text, final double value) {
		return this.getWidth(text) / value;
	}

	public double dh(final double value) {
		return this.getHeight() / value;
	}

	public double dh(final @NonNull String text, final double value) {
		return this.getHeight(text) / value;
	}

	public double aw(final @NonNull String text, final double value) {
		return this.getWidth(text) + value;
	}

	public double ah(final double value) {
		return this.getHeight() + value;
	}

	public double ah(final @NonNull String text, final double value) {
		return this.getHeight(text) + value;
	}

	public @NonNull TextInfo font(final @NonNull IFont font) {
		return super.mutate("withFont", () -> this.font = font);
	}

	public @NonNull TextInfo fontSize(final float fontSize) {
		return super.mutate("withFontSize", () -> this.fontSize = fontSize);
	}

	public @NonNull TextInfo weight(final @NonNull FontWeight weight) {
		return super.mutate("withWeight", () -> this.weight = weight);
	}

	public @NonNull TextInfo letterSpacing(final float letterSpacing) {
		return super.mutate("withLetterSpacing", () -> this.letterSpacing = letterSpacing);
	}

	public @NonNull TextInfo lineHeight(final float lineHeight) {
		return super.mutate("withLineHeight", () -> this.lineHeight = lineHeight);
	}

	public @NonNull TextInfo color(final @NonNull Color color) {
		return super.mutate("withColor", () -> this.color = color);
	}

	public @NonNull TextInfo colored(final boolean colored) {
		return super.mutate("withColored", () -> this.colored = colored);
	}

	public @NonNull TextInfo italic(final boolean italic) {
		return super.mutate("withItalic", () -> this.italic = italic);
	}

	public @NonNull TextInfo markups(final @NonNull ITextMarkup @NonNull... markups) {
		return super.mutate("withMarkups", () -> this.markups = markups.clone());
	}

	public @NonNull TextInfo effects(final @NonNull ITextEffect @NonNull... effects) {
		return super.mutate("withEffects", () -> this.effects = effects.clone());
	}

	public @NonNull TextInfo shadow() {
		return super.mutate("withShadow", () -> this.shadowColor = this.color.darker(0.3F));
	}

	public @NonNull TextInfo shadow(final Color color) {
		return super.mutate("withShadow", () -> this.shadowColor = color);
	}

	public @NonNull TextInfo shadow(final float x, final float y) {
		return super.mutate("withShadow", () -> {
			this.shadowX = x;
			this.shadowY = y;
		});
	}

	public @NonNull TextInfo shadowTint(final Float shadowTint) {
		return super.mutate("withShadowTint", () -> this.shadowTint = shadowTint);
	}

	public @NonNull TextInfo withFont(final @NonNull IFont font) {
		return super.derive(info -> info.font(font));
	}

	public @NonNull TextInfo withFontSize(final float fontSize) {
		return super.derive(info -> info.fontSize(fontSize));
	}

	public @NonNull TextInfo withWeight(final @NonNull FontWeight weight) {
		return super.derive(info -> info.weight(weight));
	}

	public @NonNull TextInfo withLetterSpacing(final float letterSpacing) {
		return super.derive(info -> info.letterSpacing(letterSpacing));
	}

	public @NonNull TextInfo withLineHeight(final float lineHeight) {
		return super.derive(info -> info.lineHeight(lineHeight));
	}

	public @NonNull TextInfo withColor(final @NonNull Color color) {
		return super.derive(info -> info.color(color));
	}

	public @NonNull TextInfo withColored(final boolean colored) {
		return super.derive(info -> info.colored(colored));
	}

	public @NonNull TextInfo withItalic(final boolean italic) {
		return super.derive(info -> info.italic(italic));
	}

	public @NonNull TextInfo withMarkups(final @NonNull ITextMarkup @NonNull... markups) {
		return super.derive(info -> info.markups(markups));
	}

	public @NonNull TextInfo withEffects(final @NonNull ITextEffect @NonNull... effects) {
		return super.derive(info -> info.effects(effects));
	}

	public @NonNull TextInfo withShadow() {
		return super.derive(info -> info.shadow());
	}

	public @NonNull TextInfo withShadow(final Color color) {
		return super.derive(info -> info.shadow(color));
	}

	public @NonNull TextInfo withShadow(final float x, final float y) {
		return super.derive(info -> info.shadow(x, y));
	}

	public @NonNull TextInfo withShadowTint(final Float shadowTint) {
		return super.derive(info -> info.shadowTint(shadowTint));
	}

	@Override
	public @NonNull TextInfo copy() {
		return new TextInfo(this.font, this.fontSize, this.weight, this.letterSpacing, this.lineHeight, this.color, this.colored, this.italic, this.shadowColor, this.shadowX, this.shadowY, this.shadowTint, this.markups, this.effects);
	}

	@Override
	public String toString() {
		return this.font.toString() + "x" + this.fontSize + " [" + this.color + "]";
	}

}