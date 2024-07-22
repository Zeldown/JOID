package be.zeldown.joid.lib.font.dto.text.modifier.impl;

import org.apache.commons.lang3.text.WordUtils;

import be.zeldown.joid.lib.font.dto.text.modifier.ITextModifier;
import lombok.NonNull;

public class TextWordCapitalizeModifier implements ITextModifier {

	@Override
	public String modify(final @NonNull String text) {
		return WordUtils.capitalize(text);
	}

}