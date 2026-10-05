package dev.joid.lib.draw.text.builder.modifier.impl;

import org.apache.commons.lang3.text.WordUtils;

import dev.joid.lib.draw.text.builder.modifier.ITextModifier;

public class TextWordCapitalizeModifier implements ITextModifier {

	@Override
	public String modify(final String text) {
		return WordUtils.capitalize(text);
	}

}