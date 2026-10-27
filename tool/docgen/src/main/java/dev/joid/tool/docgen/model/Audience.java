package dev.joid.tool.docgen.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Audience {

	USER("use", "Use JOID"),
	INTERNAL(null, null),
	CONTRIBUTOR("build", "Build on JOID");

	private final String section;
	private final String label;

	public boolean isDocumented() {
		return this != Audience.INTERNAL;
	}

	public static Audience parse(final String value) {
		return Audience.valueOf(value.trim().toUpperCase());
	}

}