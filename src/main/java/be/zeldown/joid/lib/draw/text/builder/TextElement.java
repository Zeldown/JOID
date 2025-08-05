package be.zeldown.joid.lib.draw.text.builder;

import java.util.function.Supplier;

import be.zeldown.joid.lib.draw.text.builder.modifier.ITextModifier;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class TextElement {

	private Supplier<Object> text;
	private TextInfo         info;
	private ITextModifier    modifier;

	protected TextElement(final @NonNull Object text, final @NonNull TextInfo info, final ITextModifier modifier) {
		this(() -> text.toString(), info, modifier);
	}

	protected TextElement(final @NonNull Supplier<@NonNull Object> text, final @NonNull TextInfo info, final ITextModifier modifier) {
		this.text = text;
		this.info = info;
		this.modifier = modifier;
	}

	public static final @NonNull TextElement create(final @NonNull Object text, final @NonNull TextInfo info) {
		return new TextElement(text, info, null);
	}

	public static final @NonNull TextElement create(final @NonNull Supplier<@NonNull Object> text, final @NonNull TextInfo info) {
		return new TextElement(() -> text.get(), info, null);
	}

	public static final @NonNull TextElement create(final int text, final @NonNull TextInfo info) {
		return new TextElement(String.valueOf(text), info, null);
	}

	public static final @NonNull TextElement create(final double text, final @NonNull TextInfo info) {
		return new TextElement(String.valueOf(text), info, null);
	}

	public static final @NonNull TextElement create(final float text, final @NonNull TextInfo info) {
		return new TextElement(String.valueOf(text), info, null);
	}

	public static final @NonNull TextElement create(final long text, final @NonNull TextInfo info) {
		return new TextElement(String.valueOf(text), info, null);
	}

	public static final @NonNull TextElement create(final char text, final @NonNull TextInfo info) {
		return new TextElement(String.valueOf(text), info, null);
	}

	public static final @NonNull TextElement create(final boolean text, final @NonNull TextInfo info) {
		return new TextElement(String.valueOf(text), info, null);
	}

	public final <T extends TextElement> @NonNull T text(final @NonNull Object text) {
		this.text = () -> text;
		return (T) this;
	}

	public final <T extends TextElement> @NonNull T text(final @NonNull Supplier<@NonNull Object> text) {
		this.text = text;
		return (T) this;
	}

	public final <T extends TextElement> @NonNull T text(final int text) {
		final String value = String.valueOf(text);
		this.text = () -> value;
		return (T) this;
	}

	public final <T extends TextElement> @NonNull T text(final double text) {
		final String value = String.valueOf(text);
		this.text = () -> value;
		return (T) this;
	}

	public final <T extends TextElement> @NonNull T text(final float text) {
		final String value = String.valueOf(text);
		this.text = () -> value;
		return (T) this;
	}

	public final <T extends TextElement> @NonNull T text(final long text) {
		final String value = String.valueOf(text);
		this.text = () -> value;
		return (T) this;
	}

	public final <T extends TextElement> @NonNull T text(final char text) {
		final String value = String.valueOf(text);
		this.text = () -> value;
		return (T) this;
	}

	public final <T extends TextElement> @NonNull T text(final boolean text) {
		final String value = String.valueOf(text);
		this.text = () -> value;
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
		return (T) new TextElement(text, this.info, this.modifier);
	}

	public final <T extends TextElement> @NonNull T copyWithText(final @NonNull Supplier<@NonNull Object> text) {
		return (T) new TextElement(text, this.info, this.modifier);
	}

	public final <T extends TextElement> @NonNull T copyWithInfo(final @NonNull TextInfo info) {
		return (T) new TextElement(this.text, info, this.modifier);
	}

	public final <T extends TextElement> @NonNull T copyWithModifier(final ITextModifier modifier) {
		return (T) new TextElement(this.text, this.info, modifier);
	}

	/* [ Getter Section ] */
	public final @NonNull String getRawText() {
		return this.text.get().toString();
	}

	public final @NonNull String getText() {
		final String rawText = this.getRawText();
		return this.modifier == null ? rawText : this.modifier.modify(rawText);
	}

}