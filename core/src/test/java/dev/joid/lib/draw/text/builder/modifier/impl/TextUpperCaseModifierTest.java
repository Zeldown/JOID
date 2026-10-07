package dev.joid.lib.draw.text.builder.modifier.impl;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.draw.text.builder.modifier.TextModifier;

public class TextUpperCaseModifierTest {

	@Test
	public void raisesEveryLetter() {
		Assert.assertEquals("HELLO WORLD 42", TextModifier.UPPER_CASE.modify("Hello world 42"));
	}

	@Test
	public void raisesEveryLetterWhateverTheDefaultLocale() {
		final Locale locale = Locale.getDefault();
		try {
			Locale.setDefault(new Locale("tr", "TR"));
			Assert.assertEquals("TITLE", TextModifier.UPPER_CASE.modify("title"));
		} finally {
			Locale.setDefault(locale);
		}
	}

}