package dev.joid.tool.docgen.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApiMessage {

	private final boolean warning;
	private final String  type;
	private final String  text;
	private final String  origin;
	private final String  file;
	private final int     line;
	private final String  detail;

	public static ApiMessage create(final boolean warning, final String type, final String text, final String origin, final String file, final int line) {
		return new ApiMessage(warning, type, text, origin, file, line, null);
	}

	public static ApiMessage create(final String type, final String text, final String origin, final String file, final int line, final String detail) {
		return new ApiMessage(false, type, text, origin, file, line, detail);
	}

	public String getKey() {
		return (this.warning ? "warning " : this.type + " ") + this.text;
	}

}