package dev.joid.lib.draw.text.builder;

import java.util.function.Supplier;

import dev.joid.lib.draw.text.builder.modifier.ITextModifier;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.font.converter.TextConverter;
import dev.joid.lib.signal.Signal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextElement {

	private TextInfo      info;
	private Supplier<?>   text;
	private ITextModifier modifier;

	public static @NonNull TextElement create(final @NonNull Object text, final @NonNull TextInfo info) {
		return new TextElement(info, Signal.from(text), null);
	}

	public static @NonNull TextElement create(final @NonNull Supplier<?> text, final @NonNull TextInfo info) {
		return new TextElement(info, text, null);
	}

	public @NonNull TextElement text(final @NonNull Object text) {
		return this.text(Signal.from(text));
	}

	public @NonNull TextElement text(final @NonNull Supplier<?> text) {
		this.text = text;
		return this;
	}

	public @NonNull TextElement info(final @NonNull TextInfo info) {
		this.info = info;
		return this;
	}

	public @NonNull TextElement modifier(final ITextModifier modifier) {
		this.modifier = modifier;
		return this;
	}

	public @NonNull TextElement copy() {
		return new TextElement(this.info, this.text, this.modifier);
	}

	public @NonNull TextElement copyWithText(final @NonNull Object text) {
		return new TextElement(this.info, () -> text, this.modifier);
	}

	public @NonNull TextElement copyWithText(final @NonNull Supplier<?> text) {
		return new TextElement(this.info, text, this.modifier);
	}

	public @NonNull String getText() {
		final String rawText = this.getRawText();
		return this.modifier == null ? rawText : this.modifier.modify(rawText, this.info);
	}

	public @NonNull String getRawText() {
		return TextConverter.convert(this.text.get());
	}

}