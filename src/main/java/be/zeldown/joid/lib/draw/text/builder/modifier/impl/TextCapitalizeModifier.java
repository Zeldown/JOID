package be.zeldown.joid.lib.draw.text.builder.modifier.impl;

import org.apache.commons.lang3.StringUtils;

import be.zeldown.joid.lib.draw.text.builder.modifier.ITextModifier;
import lombok.NonNull;

public class TextCapitalizeModifier implements ITextModifier {

	@Override
	public @NonNull String modify(final @NonNull String text) {
		return StringUtils.capitalize(text);
	}

}