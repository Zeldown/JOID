package dev.joid.lib.font.modifier;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

public class TextModifierTest {

	@Test
	public void joinsTheWordsInCamelCase() {
		Assert.assertEquals("helloBigWorld", TextModifier.CAMEL_CASE.modify("hello big world"));
	}

	@Test
	public void lowersTheOtherLetters() {
		Assert.assertEquals("helloWorld", TextModifier.CAMEL_CASE.modify("HELLO WORLD"));
	}

	@Test
	public void splitsOnEverySeparator() {
		Assert.assertEquals("helloBigWorldAgain", TextModifier.CAMEL_CASE.modify("hello_big-world.again"));
	}

	@Test
	public void keepsAnEmptyText() {
		Assert.assertEquals("", TextModifier.CAMEL_CASE.modify(""));
	}

	@Test
	public void startsInLowerCaseAfterALeadingSeparator() {
		Assert.assertEquals("helloWorld", TextModifier.CAMEL_CASE.modify(" hello world"));
	}

	@Test
	public void keepsTheAccentedLetters() {
		Assert.assertEquals("déjàVu", TextModifier.CAMEL_CASE.modify("déjà vu"));
	}

	@Test
	public void raisesOnlyTheFirstLetter() {
		Assert.assertEquals("Hello world", TextModifier.CAPITALIZE.modify("hello world"));
	}

	@Test
	public void keepsTheOtherLettersAsTheyAre() {
		Assert.assertEquals("HELLO", TextModifier.CAPITALIZE.modify("hELLO"));
	}

	@Test
	public void keepsAnEmptyTextInCapitalize() {
		Assert.assertEquals("", TextModifier.CAPITALIZE.modify(""));
	}

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

	@Test
	public void joinsTheWordsWithUnderscores() {
		Assert.assertEquals("hello_big_world", TextModifier.SNAKE_CASE.modify("Hello Big World"));
	}

	@Test
	public void lowersEveryLetterWhateverTheDefaultLocaleInSnakeCase() {
		final Locale locale = Locale.getDefault();
		try {
			Locale.setDefault(new Locale("tr", "TR"));
			Assert.assertEquals("big_title", TextModifier.SNAKE_CASE.modify("BIG TITLE"));
		} finally {
			Locale.setDefault(locale);
		}
	}

	@Test
	public void raisesTheFirstLetterAndLowersTheRest() {
		Assert.assertEquals("Hello", TextModifier.UPPER_CAMEL_CASE.modify("hELLO"));
	}

	@Test
	public void joinsTheWordsInUpperCamelCase() {
		Assert.assertEquals("HelloBigWorld", TextModifier.UPPER_CAMEL_CASE.modify("hello big world"));
	}

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

	@Test
	public void raisesTheFirstLetterOfEachWord() {
		Assert.assertEquals("Hello Big World", TextModifier.WORD_CAPITALIZE.modify("hello big world"));
	}

	@Test
	public void keepsTheOtherLettersAsTheyAreInWordCapitalize() {
		Assert.assertEquals("Hello WORLD", TextModifier.WORD_CAPITALIZE.modify("hello wORLD"));
	}

}