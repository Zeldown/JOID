package be.zeldown.joid.lib.draw.text.builder.modifier.impl;

import be.zeldown.joid.lib.draw.text.builder.modifier.ITextModifier;
import lombok.NonNull;

public class TextLowerCaseModifier implements ITextModifier {

	@Override
	public @NonNull String modify(final @NonNull String text) {
		return text.toLowerCase();
	}

}