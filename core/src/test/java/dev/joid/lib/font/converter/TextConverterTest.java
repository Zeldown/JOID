package dev.joid.lib.font.converter;

import java.util.Arrays;
import java.util.Collections;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public class TextConverterTest {

	private final LanguageTextConverter converter = new LanguageTextConverter("en");

	@After
	public void unregister() {
		TextConverter.unregister(this.converter);
	}

	@Test
	public void keepsAString() {
		TextConverter.register(this.converter);
		Assert.assertEquals("Play", TextConverter.convert("Play"));
	}

	@Test
	public void convertsAnObjectWithItsToStringWithoutConverter() {
		Assert.assertEquals("menu.play", TextConverter.convert(new TranslatableText("menu.play")));
		Assert.assertEquals("12", TextConverter.convert(12));
	}

	@Test
	public void convertsAnObjectWithTheConverterThatSupportsIt() {
		TextConverter.register(this.converter);
		Assert.assertEquals("[en] menu.play", TextConverter.convert(new TranslatableText("menu.play")));
		Assert.assertEquals("12", TextConverter.convert(12));
	}

	@Test
	public void givesPriorityToTheLatestRegistration() {
		final LanguageTextConverter french = new LanguageTextConverter("fr");
		TextConverter.register(this.converter);
		TextConverter.register(french);
		try {
			Assert.assertEquals("[fr] menu.play", TextConverter.convert(new TranslatableText("menu.play")));
			TextConverter.register(this.converter);
			Assert.assertEquals("[en] menu.play", TextConverter.convert(new TranslatableText("menu.play")));
		} finally {
			TextConverter.unregister(french);
		}
	}

	@Test
	public void stopsConvertingOnceUnregistered() {
		TextConverter.register(this.converter);
		TextConverter.unregister(this.converter);
		Assert.assertEquals("menu.play", TextConverter.convert(new TranslatableText("menu.play")));
	}

	@Test
	public void convertsEveryLineOfAList() {
		TextConverter.register(this.converter);
		Assert.assertEquals(Arrays.asList("First", "[en] menu.play"), TextConverter.convertLines(Arrays.asList("First", new TranslatableText("menu.play"))));
	}

	@Test
	public void convertsASingleObjectToOneLine() {
		TextConverter.register(this.converter);
		Assert.assertEquals(Collections.singletonList("[en] menu.play"), TextConverter.convertLines(new TranslatableText("menu.play")));
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullText() {
		TextConverter.convert(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullLine() {
		TextConverter.convertLines(Arrays.asList("First", null));
	}

	@RequiredArgsConstructor
	public static final class TranslatableText {

		private final String key;

		@Override
		public String toString() {
			return this.key;
		}

	}

	@RequiredArgsConstructor
	public static final class LanguageTextConverter implements ITextConverter {

		private final String language;

		@Override
		public boolean supports(final @NonNull Object text) {
			return text instanceof TranslatableText;
		}

		@Override
		public @NonNull String convert(final @NonNull Object text) {
			return "[" + this.language + "] " + text;
		}

	}

}