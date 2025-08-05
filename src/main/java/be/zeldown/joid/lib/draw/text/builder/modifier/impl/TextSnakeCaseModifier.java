package be.zeldown.joid.lib.draw.text.builder.modifier.impl;

import be.zeldown.joid.lib.draw.text.builder.modifier.ITextModifier;

public class TextSnakeCaseModifier implements ITextModifier {

	@Override
	public String modify(final String text) {
		return text.toLowerCase().replace(" ", "_");
	}

}