package dev.joid.lib.draw.text.builder.modifier.impl;

import java.util.Locale;

import dev.joid.lib.draw.text.builder.modifier.ITextModifier;

public class TextUpperCaseModifier implements ITextModifier {

	@Override
	public String modify(final String text) {
		return text.toUpperCase(Locale.ROOT);
	}

}