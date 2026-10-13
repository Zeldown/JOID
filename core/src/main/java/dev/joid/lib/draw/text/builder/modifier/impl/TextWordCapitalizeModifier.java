package dev.joid.lib.draw.text.builder.modifier.impl;

import dev.joid.lib.draw.text.builder.modifier.ITextModifier;

public class TextWordCapitalizeModifier implements ITextModifier {

	@Override
	public String modify(final String text) {
		final StringBuilder builder = new StringBuilder(text.length());
		boolean capitalize = true;
		for (final char character : text.toCharArray()) {
			builder.append(capitalize ? Character.toTitleCase(character) : character);
			capitalize = Character.isWhitespace(character);
		}
		return builder.toString();
	}

}