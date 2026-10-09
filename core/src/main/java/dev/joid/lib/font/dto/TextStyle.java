package dev.joid.lib.font.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.IFont;
import dev.joid.lib.font.dto.effect.ITextEffect;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class TextStyle {

	private final TextStyle         base;
	private final List<ITextEffect> effects;

	private IFont      font;
	private Color      color;
	private boolean    italic;
	private FontWeight weight;

	private TextStyle(final TextStyle base, final IFont font, final FontWeight weight, final boolean italic, final Color color, final List<ITextEffect> effects) {
		this.base = base;
		this.font = font;
		this.weight = weight;
		this.italic = italic;
		this.color = color;
		this.effects = new ArrayList<>(effects);
	}

	public static @NonNull TextStyle create(final @NonNull FontWeight weight, final boolean italic, final @NonNull Color color, final @NonNull ITextEffect @NonNull... effects) {
		return new TextStyle(null, null, weight, italic, color, Arrays.asList(effects));
	}

	public @NonNull TextStyle copy() {
		return new TextStyle(this.base, this.font, this.weight, this.italic, this.color, this.effects);
	}

	public @NonNull TextStyle reset() {
		if (this.base != null) {
			this.font = this.base.font;
			this.weight = this.base.weight;
			this.italic = this.base.italic;
			this.color = this.base.color;
			this.effects.clear();
			this.effects.addAll(this.base.effects);
		}
		return this;
	}

	public @NonNull TextStyle derive() {
		return new TextStyle(this, this.font, this.weight, this.italic, this.color, this.effects);
	}

	public @NonNull TextStyle font(final IFont font) {
		this.font = font;
		return this;
	}

	public @NonNull TextStyle italic(final boolean italic) {
		this.italic = italic;
		return this;
	}

	public @NonNull TextStyle color(final @NonNull Color color) {
		this.color = color;
		return this;
	}

	public @NonNull TextStyle weight(final @NonNull FontWeight weight) {
		this.weight = weight;
		return this;
	}

	public @NonNull TextStyle effect(final @NonNull ITextEffect effect) {
		if (!this.effects.contains(effect)) {
			this.effects.add(effect);
		}
		return this;
	}

	public @NonNull TextStyle removeEffect(final @NonNull ITextEffect effect) {
		this.effects.remove(effect);
		return this;
	}

	public @NonNull TextStyle getBase() {
		return this.base == null ? this : this.base;
	}

	public @NonNull List<ITextEffect> getEffects() {
		return Collections.unmodifiableList(this.effects);
	}

}