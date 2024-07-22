package be.zeldown.joid.lib.font.dto.text.modifier.impl;

import be.zeldown.joid.lib.font.dto.text.modifier.ITextModifier;
import lombok.NonNull;

public class TextLowerCaseModifier implements ITextModifier {

	@Override
	public String modify(final @NonNull String text) {
		return text.toLowerCase();
	}

}