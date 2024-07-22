package be.zeldown.joid.lib.font.dto.text.modifier.impl;

import be.zeldown.joid.lib.font.dto.text.modifier.ITextModifier;

public class TextCamelCaseModifier implements ITextModifier {

	@Override
	public String modify(final String text) {
		final String[] words = text.split("[\\W_]+");
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < words.length; i++) {
		    String word = words[i];
		    if (i == 0) {
		        word = word.isEmpty() ? word : word.toLowerCase();
		    } else {
		        word = word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
		    }
		    builder.append(word);
		}

		return builder.toString();
	}

}