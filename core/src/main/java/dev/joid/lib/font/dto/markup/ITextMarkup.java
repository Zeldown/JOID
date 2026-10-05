package dev.joid.lib.font.dto.markup;

import dev.joid.lib.font.dto.TextStyle;
import lombok.NonNull;

public interface ITextMarkup {

	public int parse(final @NonNull String text, final int index, final @NonNull TextStyle style);

}