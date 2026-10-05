package dev.joid.lib.font.dto.markup;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.dto.TextStyle;

public class TextMarkupTest {

	private static final ITextMarkup BOLD = (text, index, style) -> {
		if (text.charAt(index) != '*') {
			return 0;
		}

		style.weight(FontWeight.BOLD);
		return 1;
	};

	private static final ITextMarkup STARS = (text, index, style) -> {
		if (!text.startsWith("**", index)) {
			return 0;
		}

		style.weight(FontWeight.BLACK);
		return 2;
	};

	@Test(expected = UnsupportedOperationException.class)
	public void exposesAReadOnlyRegistry() {
		TextMarkup.getRegistered().add(TextMarkupTest.BOLD);
	}

	@Test
	public void skipsMarkupsThatDoNotMatch() {
		Assert.assertEquals(0, TextMarkup.parse(Arrays.asList(TextMarkupTest.BOLD, TextMarkupTest.STARS), "text", 0, TextMarkupTest.style()));
	}

	@Test
	public void consumesNothingWithoutMarkup() {
		final TextStyle style = TextMarkupTest.style();
		Assert.assertEquals(0, TextMarkup.parse(Collections.emptyList(), "*text", 0, style));
		Assert.assertSame(FontWeight.REGULAR, style.getWeight());
	}

	@Test
	public void letsTheFirstMatchingMarkupWin() {
		final TextStyle style = TextMarkupTest.style();
		Assert.assertEquals(2, TextMarkup.parse(Arrays.asList(TextMarkupTest.STARS, TextMarkupTest.BOLD), "**text", 0, style));
		Assert.assertSame(FontWeight.BLACK, style.getWeight());
		Assert.assertEquals(1, TextMarkup.parse(Arrays.asList(TextMarkupTest.BOLD, TextMarkupTest.STARS), "**text", 0, style));
		Assert.assertSame(FontWeight.BOLD, style.getWeight());
	}

	@Test
	public void givesPriorityToTheLatestRegistration() {
		TextMarkup.register(TextMarkupTest.BOLD);
		TextMarkup.register(TextMarkupTest.STARS);
		try {
			Assert.assertEquals(Arrays.asList(TextMarkupTest.STARS, TextMarkupTest.BOLD), TextMarkup.getRegistered().subList(0, 2));
		} finally {
			TextMarkup.unregister(TextMarkupTest.BOLD);
			TextMarkup.unregister(TextMarkupTest.STARS);
		}
		Assert.assertFalse(TextMarkup.getRegistered().contains(TextMarkupTest.BOLD));
		Assert.assertFalse(TextMarkup.getRegistered().contains(TextMarkupTest.STARS));
	}

	private static TextStyle style() {
		return TextStyle.create(FontWeight.REGULAR, false, Color.WHITE).derive();
	}

}