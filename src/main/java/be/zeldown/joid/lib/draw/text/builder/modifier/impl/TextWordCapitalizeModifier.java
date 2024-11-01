package be.zeldown.joid.lib.draw.text.builder.modifier.impl;

import org.apache.commons.lang3.text.WordUtils;

import be.zeldown.joid.lib.draw.text.builder.modifier.ITextModifier;
import lombok.NonNull;

public class TextWordCapitalizeModifier implements ITextModifier {

	@Override
	public @NonNull String modify(final @NonNull String text) {
		return WordUtils.capitalize(text);
	}

}