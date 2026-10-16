package dev.joid.lib.draw.text.builder.modifier;

import java.util.List;

import dev.joid.lib.font.TextInfo;
import dev.joid.lib.font.markup.ITextMarkup;
import dev.joid.lib.font.markup.TextMarkup;
import lombok.NonNull;

@FunctionalInterface
public interface ITextModifier {

	public @NonNull String modify(final @NonNull String text);

	public default @NonNull String modify(final @NonNull String text, final @NonNull TextInfo info) {
		final List<ITextMarkup> markups = info.getMarkups();
		if (markups.isEmpty()) {
			return this.modify(text);
		}

		final int[] tags = TextMarkup.tags(markups, text, info.getStyle().derive());
		final StringBuilder builder = new StringBuilder();
		int start = 0;
		for (int index = 0; index < text.length(); index++) {
			if (tags[index] != -1) {
				builder.append(start < index ? this.modify(text.substring(start, index)) : "").append(text.charAt(index));
				start = index + 1;
			}
		}

		return builder.append(start < text.length() ? this.modify(text.substring(start)) : "").toString();
	}

}