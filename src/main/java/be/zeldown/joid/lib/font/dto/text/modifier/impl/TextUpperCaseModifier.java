package be.zeldown.joid.lib.font.dto.text.modifier.impl;

import be.zeldown.joid.lib.font.dto.text.modifier.ITextModifier;

public class TextUpperCaseModifier implements ITextModifier {

	@Override
	public String modify(final String text) {
		return text.toUpperCase();
	}

}