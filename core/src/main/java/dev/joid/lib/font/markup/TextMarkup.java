package dev.joid.lib.font.markup;

import java.util.Arrays;
import java.util.List;

import dev.joid.lib.font.TextStyle;
import dev.joid.lib.utils.registry.Registry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextMarkup {

	private static final Registry<ITextMarkup> MARKUPS = new Registry<>();

	public static void register(final @NonNull ITextMarkup markup) {
		TextMarkup.MARKUPS.register(markup);
	}

	public static void unregister(final @NonNull ITextMarkup markup) {
		TextMarkup.MARKUPS.unregister(markup);
	}

	public static @NonNull List<ITextMarkup> getRegistered() {
		return TextMarkup.MARKUPS.getAll();
	}

	public static int parse(final @NonNull List<ITextMarkup> markups, final @NonNull String text, final int index, final @NonNull TextStyle style) {
		for (final ITextMarkup markup : markups) {
			final int consumed = markup.parse(text, index, style);
			if (consumed > 0) {
				return consumed;
			}
		}

		return 0;
	}

	public static @NonNull int[] tags(final @NonNull List<ITextMarkup> markups, final @NonNull String text, final @NonNull TextStyle style) {
		final int[] tags = new int[text.length()];
		Arrays.fill(tags, -1);
		if (markups.isEmpty()) {
			return tags;
		}

		for (int index = 0; index < text.length();) {
			final int consumed = TextMarkup.parse(markups, text, index, style);
			if (consumed > 0) {
				Arrays.fill(tags, index, index + consumed, index);
				index += consumed;
				continue;
			}

			index += Character.charCount(text.codePointAt(index));
		}
		return tags;
	}

}