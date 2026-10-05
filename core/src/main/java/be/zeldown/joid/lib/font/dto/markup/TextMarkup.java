package be.zeldown.joid.lib.font.dto.markup;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import be.zeldown.joid.lib.font.dto.TextStyle;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TextMarkup {

	private static final List<ITextMarkup> MARKUPS = new CopyOnWriteArrayList<>();

	public static void register(final @NonNull ITextMarkup markup) {
		TextMarkup.MARKUPS.add(0, markup);
	}

	public static void unregister(final @NonNull ITextMarkup markup) {
		TextMarkup.MARKUPS.remove(markup);
	}

	public static @NonNull List<ITextMarkup> getRegistered() {
		return Collections.unmodifiableList(TextMarkup.MARKUPS);
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

}