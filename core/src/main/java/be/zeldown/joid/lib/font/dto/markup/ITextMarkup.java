package be.zeldown.joid.lib.font.dto.markup;

import be.zeldown.joid.lib.font.dto.TextStyle;
import lombok.NonNull;

public interface ITextMarkup {

	public int parse(final @NonNull String text, final int index, final @NonNull TextStyle style);

}