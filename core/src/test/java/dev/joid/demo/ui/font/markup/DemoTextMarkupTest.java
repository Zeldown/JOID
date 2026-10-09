package dev.joid.demo.ui.font.markup;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.demo.ui.font.effect.DemoHighlightTextEffect;
import dev.joid.demo.ui.font.effect.DemoUnderlineTextEffect;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextStyle;

public class DemoTextMarkupTest {

	@Test
	public void ignoresPlainText() {
		final TextStyle style = DemoTextMarkupTest.style();
		for (final String text : new String[] {"hello", "<x>", "<w=12>", "<c=12>", "<b", "</x>"}) {
			Assert.assertEquals(text, 0, DemoTextMarkup.inst().parse(text, 0, style));
		}
		Assert.assertSame(FontWeight.REGULAR, style.getWeight());
	}

	@Test
	public void switchesTheStyle() {
		final TextStyle style = DemoTextMarkupTest.style();
		DemoTextMarkup.inst().parse("<i>", 0, style);
		Assert.assertTrue(style.isItalic());
		DemoTextMarkup.inst().parse("</i>", 0, style);
		Assert.assertFalse(style.isItalic());
	}

	@Test
	public void switchesTheColor() {
		final TextStyle style = DemoTextMarkupTest.style();
		Assert.assertEquals(10, DemoTextMarkup.inst().parse("<c=ff0000>", 0, style));
		Assert.assertEquals(1F, style.getColor().r, 0F);
		Assert.assertEquals(0F, style.getColor().g, 0F);
		Assert.assertEquals(4, DemoTextMarkup.inst().parse("</c>", 0, style));
		Assert.assertSame(Color.WHITE, style.getColor());
	}

	@Test
	public void switchesTheWeight() {
		final TextStyle style = DemoTextMarkupTest.style();
		Assert.assertEquals(3, DemoTextMarkup.inst().parse("<b>", 0, style));
		Assert.assertSame(FontWeight.BOLD, style.getWeight());
		Assert.assertEquals(4, DemoTextMarkup.inst().parse("</b>", 0, style));
		Assert.assertSame(FontWeight.REGULAR, style.getWeight());
		Assert.assertEquals(7, DemoTextMarkup.inst().parse("<w=300>", 0, style));
		Assert.assertSame(FontWeight.LIGHT, style.getWeight());
		Assert.assertEquals(4, DemoTextMarkup.inst().parse("</w>", 0, style));
		Assert.assertSame(FontWeight.REGULAR, style.getWeight());
	}

	@Test
	public void addsAndRemovesEffects() {
		final TextStyle style = DemoTextMarkupTest.style();
		DemoTextMarkup.inst().parse("<u>", 0, style);
		DemoTextMarkup.inst().parse("<h>", 0, style);
		Assert.assertTrue(style.getEffects().contains(DemoUnderlineTextEffect.inst()));
		Assert.assertTrue(style.getEffects().contains(DemoHighlightTextEffect.inst()));
		DemoTextMarkup.inst().parse("</u>", 0, style);
		DemoTextMarkup.inst().parse("</h>", 0, style);
		Assert.assertTrue(style.getEffects().isEmpty());
	}

	@Test
	public void readsATagInsideTheText() {
		final TextStyle style = DemoTextMarkupTest.style();
		Assert.assertEquals(3, DemoTextMarkup.inst().parse("an <b>bold", 3, style));
		Assert.assertSame(FontWeight.BOLD, style.getWeight());
	}

	private static TextStyle style() {
		return TextStyle.create(FontWeight.REGULAR, false, Color.WHITE).derive();
	}

}