package dev.joid.lib.font.modifier;

import java.util.Locale;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.StringUtils;

import dev.joid.lib.font.TextInfo;
import dev.joid.lib.font.markup.TextMarkup;
import lombok.NonNull;

@FunctionalInterface
public interface TextModifier {

	public static final TextModifier LOWER_CASE       = text -> text.toLowerCase(Locale.ROOT);
	public static final TextModifier UPPER_CASE       = text -> text.toUpperCase(Locale.ROOT);

	public static final TextModifier CAMEL_CASE       = text -> TextModifier.camelCase(text, false);
	public static final TextModifier UPPER_CAMEL_CASE = text -> TextModifier.camelCase(text, true);

	public static final TextModifier SNAKE_CASE       = text -> text.toLowerCase(Locale.ROOT).replace(" ", "_");

	public static final TextModifier CAPITALIZE       = StringUtils::capitalize;
	public static final TextModifier WORD_CAPITALIZE  = TextModifier::capitalizeWords;

	public @NonNull String modify(final @NonNull String text);

	public default @NonNull String modify(final @NonNull String text, final @NonNull TextInfo info) {
		if (info.getMarkups().isEmpty()) {
			return this.modify(text);
		}

		final int[] tags = TextMarkup.tags(text, info);
		final StringBuilder builder = new StringBuilder();
		final StringBuilder plain = new StringBuilder();
		int start = 0;
		for (int index = 0; index < text.length(); index++) {
			if (tags[index] != -1) {
				builder.append(TextModifier.modifyAfter(this::modify, plain, text.substring(start, index))).append(text.charAt(index));
				start = index + 1;
			}
		}

		return builder.append(TextModifier.modifyAfter(this::modify, plain, text.substring(start))).toString();
	}

	public static @NonNull String modifyAfter(final @NonNull UnaryOperator<String> modifier, final @NonNull StringBuilder before, final @NonNull String text) {
		final String start = modifier.apply(before.toString());
		final String modified = modifier.apply(before.append(text).toString());
		return modified.startsWith(start) ? modified.substring(start.length()) : modifier.apply(text);
	}

	public static @NonNull String camelCase(final @NonNull String text, final boolean upperFirst) {
		final StringBuilder builder = new StringBuilder();
		for (final String word : text.split("(?U)[\\W_]+")) {
			if (builder.length() == 0 && !upperFirst) {
				builder.append(word.toLowerCase(Locale.ROOT));
			} else if (!word.isEmpty()) {
				builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase(Locale.ROOT));
			}
		}

		return builder.toString();
	}

	public static @NonNull String capitalizeWords(final @NonNull String text) {
		final StringBuilder builder = new StringBuilder(text.length());
		boolean capitalize = true;
		for (final char character : text.toCharArray()) {
			builder.append(capitalize ? Character.toTitleCase(character) : character);
			capitalize = Character.isWhitespace(character);
		}
		return builder.toString();
	}

}