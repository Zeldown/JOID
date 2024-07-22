package be.zeldown.joid.lib.font.dto.text;

import lombok.Getter;
import lombok.NonNull;

public enum TextOverflow {

	NONE(""),
	ELLIPSIS("..."),
	DOT("."),
	HYPHEN("-");

	@Getter private final String overflow;

	TextOverflow(final @NonNull String overflow) {
		this.overflow = overflow;
	}

}