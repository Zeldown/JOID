package dev.joid.lib.draw.text;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TextOverflow {

	NONE(""),
	ELLIPSIS("..."),
	DOT("."),
	HYPHEN("-");

	@Getter
	private final String overflow;

}