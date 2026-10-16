package dev.joid.lib.font.modifier;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextModifier {

	public static final ITextModifier LOWER_CASE       = text -> text.toLowerCase(Locale.ROOT);
	public static final ITextModifier UPPER_CASE       = text -> text.toUpperCase(Locale.ROOT);
	public static final ITextModifier CAMEL_CASE       = text -> TextModifier.camelCase(text, false);
	public static final ITextModifier UPPER_CAMEL_CASE = text -> TextModifier.camelCase(text, true);
	public static final ITextModifier SNAKE_CASE       = text -> text.toLowerCase(Locale.ROOT).replace(" ", "_");

	public static final ITextModifier CAPITALIZE       = StringUtils::capitalize;
	public static final ITextModifier WORD_CAPITALIZE  = TextModifier::capitalizeWords;

	private static String camelCase(final String text, final boolean upperFirst) {
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

	private static String capitalizeWords(final String text) {
		final StringBuilder builder = new StringBuilder(text.length());
		boolean capitalize = true;
		for (final char character : text.toCharArray()) {
			builder.append(capitalize ? Character.toTitleCase(character) : character);
			capitalize = Character.isWhitespace(character);
		}
		return builder.toString();
	}

}