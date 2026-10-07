package dev.joid.lib.draw.text.builder.modifier.impl;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.draw.text.builder.modifier.TextModifier;

public class TextSnakeCaseModifierTest {

	@Test
	public void joinsTheWordsWithUnderscores() {
		Assert.assertEquals("hello_big_world", TextModifier.SNAKE_CASE.modify("Hello Big World"));
	}

	@Test
	public void lowersEveryLetterWhateverTheDefaultLocale() {
		final Locale locale = Locale.getDefault();
		try {
			Locale.setDefault(new Locale("tr", "TR"));
			Assert.assertEquals("big_title", TextModifier.SNAKE_CASE.modify("BIG TITLE"));
		} finally {
			Locale.setDefault(locale);
		}
	}

}