package dev.joid.lib.draw.text.builder.modifier.impl;

import dev.joid.lib.draw.text.builder.modifier.ITextModifier;

public class TextCamelCaseModifier implements ITextModifier {

	@Override
	public String modify(final String text) {
		final StringBuilder builder = new StringBuilder();
		for (final String word : text.split("(?U)[\\W_]+")) {
			if (builder.length() == 0) {
				builder.append(word.toLowerCase());
			} else if (!word.isEmpty()) {
				builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase());
			}
		}

		return builder.toString();
	}

}