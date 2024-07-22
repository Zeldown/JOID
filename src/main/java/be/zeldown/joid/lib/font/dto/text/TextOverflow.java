package be.zeldown.joid.lib.font.dto.text;

import lombok.Getter;

public enum TextOverflow {

	NONE(""),
	ELLIPSIS("..."),
	DOT("."),
	HYPHEN("-");

	@Getter String overflow;

	TextOverflow(final String overflow) {
		this.overflow = overflow;
	}

}