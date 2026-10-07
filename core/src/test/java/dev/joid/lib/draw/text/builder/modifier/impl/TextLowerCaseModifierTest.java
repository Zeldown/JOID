package dev.joid.lib.draw.text.builder.modifier.impl;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.draw.text.builder.modifier.TextModifier;

public class TextLowerCaseModifierTest {

	@Test
	public void lowersEveryLetter() {
		Assert.assertEquals("hello world 42", TextModifier.LOWER_CASE.modify("Hello WORLD 42"));
	}

	@Test
	public void lowersEveryLetterWhateverTheDefaultLocale() {
		final Locale locale = Locale.getDefault();
		try {
			Locale.setDefault(new Locale("tr", "TR"));
			Assert.assertEquals("title", TextModifier.LOWER_CASE.modify("TITLE"));
		} finally {
			Locale.setDefault(locale);
		}
	}

}