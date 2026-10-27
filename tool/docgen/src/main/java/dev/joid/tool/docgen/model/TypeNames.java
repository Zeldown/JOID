package dev.joid.tool.docgen.model;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TypeNames {

	public static String clean(final String type) {
		return type.replaceAll("@[\\w.]+(\\([^)]*\\))?\\s*", "").replaceAll("\\s+", " ").trim();
	}

	public static String erase(final String type) {
		final StringBuilder builder = new StringBuilder();
		int depth = 0;
		for (final char character : TypeNames.clean(type).toCharArray()) {
			if (character == '<') {
				depth++;
			} else if (character == '>') {
				depth--;
			} else if (depth == 0 && character != ' ') {
				builder.append(character);
			}
		}

		final String erased = builder.toString().replace("...", "[]");
		final int arrays = erased.indexOf('[');
		final String base = arrays < 0 ? erased : erased.substring(0, arrays);
		final String suffix = arrays < 0 ? "" : erased.substring(arrays);
		return base.substring(base.lastIndexOf('.') + 1) + suffix;
	}

	public static String raw(final String type) {
		final String cleaned = TypeNames.clean(type);
		final int generic = cleaned.indexOf('<');
		final String base = generic < 0 ? cleaned : cleaned.substring(0, generic);
		return base.replace("[]", "").replace("...", "").trim();
	}

	public static String normalizeKey(final String key) {
		final String trimmed = key.replace("`", "").trim();
		final int open = trimmed.indexOf('(');
		if (open < 0 || !trimmed.endsWith(")")) {
			return trimmed.substring(trimmed.lastIndexOf('#') + 1).replaceAll("\\s+", "");
		}

		final String name = trimmed.substring(trimmed.lastIndexOf('#', open) + 1, open).trim();
		final List<String> parameters = new ArrayList<>();
		for (final String parameter : TypeNames.split(trimmed.substring(open + 1, trimmed.length() - 1))) {
			final String type = TypeNames.stripName(parameter);
			if (!type.isEmpty()) {
				parameters.add(TypeNames.erase(type));
			}
		}
		return name + "(" + String.join(",", parameters) + ")";
	}

	public static List<String> split(final String parameters) {
		final List<String> parts = new ArrayList<>();
		final StringBuilder current = new StringBuilder();
		int depth = 0;
		for (final char character : parameters.toCharArray()) {
			if (character == '<') {
				depth++;
			} else if (character == '>') {
				depth--;
			}

			if (character == ',' && depth == 0) {
				parts.add(current.toString().trim());
				current.setLength(0);
				continue;
			}
			current.append(character);
		}

		if (current.toString().trim().length() > 0) {
			parts.add(current.toString().trim());
		}
		return parts;
	}

	private static String stripName(final String parameter) {
		final String cleaned = TypeNames.clean(parameter).replaceAll("^final ", "");
		final int depthEnd = cleaned.lastIndexOf('>');
		final int space = cleaned.lastIndexOf(' ');
		if (space > depthEnd && space > 0 && cleaned.substring(space + 1).matches("[a-z_$][\\w$]*")) {
			return cleaned.substring(0, space);
		}
		return cleaned;
	}

}