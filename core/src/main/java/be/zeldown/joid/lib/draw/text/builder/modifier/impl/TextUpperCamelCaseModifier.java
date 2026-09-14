package be.zeldown.joid.lib.draw.text.builder.modifier.impl;

import org.apache.commons.lang3.text.WordUtils;

import be.zeldown.joid.lib.draw.text.builder.modifier.ITextModifier;

public class TextUpperCamelCaseModifier implements ITextModifier {

	@Override
	public String modify(final String text) {
		return WordUtils.capitalizeFully(text);
	}

}