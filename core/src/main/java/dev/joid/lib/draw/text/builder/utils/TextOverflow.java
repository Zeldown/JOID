package dev.joid.lib.draw.text.builder.utils;

import lombok.Getter;

public enum TextOverflow {

	NONE(""),
	ELLIPSIS("..."),
	DOT("."),
	HYPHEN("-");

	@Getter
	private final String overflow;

	private TextOverflow(final String overflow) {
		this.overflow = overflow;
	}

}