package dev.joid.lib.draw.text.builder;

import java.util.function.Supplier;

import dev.joid.lib.draw.text.builder.modifier.ITextModifier;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.font.converter.TextConverter;
import dev.joid.lib.signal.Signal;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class TextElement {

	private TextInfo      info;
	private Supplier<?>   text;
	private ITextModifier modifier;

	protected TextElement(final @NonNull Supplier<?> text, final @NonNull TextInfo info, final ITextModifier modifier) {
		this.text = text;
		this.info = info;
		this.modifier = modifier;
	}

	public static final @NonNull TextElement create(final @NonNull Object text, final @NonNull TextInfo info) {
		return new TextElement(Signal.from(text), info, null);
	}

	public static final @NonNull TextElement create(final @NonNull Supplier<?> text, final @NonNull TextInfo info) {
		return new TextElement(() -> text.get(), info, null);
	}

	public final <T extends TextElement> @NonNull T text(final @NonNull Object text) {
		return this.text(Signal.from(text));
	}

	public final <T extends TextElement> @NonNull T text(final @NonNull Supplier<?> text) {
		this.text = text;
		return (T) this;
	}

	public final <T extends TextElement> @NonNull T info(final @NonNull TextInfo info) {
		this.info = info;
		return (T) this;
	}

	public final <T extends TextElement> @NonNull T modifier(final ITextModifier modifier) {
		this.modifier = modifier;
		return (T) this;
	}

	public final <T extends TextElement> @NonNull T copy() {
		return (T) new TextElement(this.text, this.info, this.modifier);
	}

	public final <T extends TextElement> @NonNull T copyWithText(final @NonNull Object text) {
		return (T) new TextElement(() -> text, this.info, this.modifier);
	}

	public final <T extends TextElement> @NonNull T copyWithText(final @NonNull Supplier<?> text) {
		return (T) new TextElement(text, this.info, this.modifier);
	}

	public final @NonNull String getText() {
		final String rawText = this.getRawText();
		return this.modifier == null ? rawText : this.modifier.modify(rawText, this.info);
	}

	public final @NonNull String getRawText() {
		return TextConverter.convert(this.text.get());
	}

}