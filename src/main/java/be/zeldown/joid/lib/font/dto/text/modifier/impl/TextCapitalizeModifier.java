package be.zeldown.joid.lib.font.dto.text.modifier.impl;

import org.apache.commons.lang3.StringUtils;

import be.zeldown.joid.lib.font.dto.text.modifier.ITextModifier;
import lombok.NonNull;

public class TextCapitalizeModifier implements ITextModifier {

	@Override
	public String modify(final @NonNull String text) {
		return StringUtils.capitalize(text);
	}

}