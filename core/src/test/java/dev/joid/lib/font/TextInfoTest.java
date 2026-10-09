package dev.joid.lib.font;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.effect.ITextEffect;
import dev.joid.lib.font.markup.ITextMarkup;
import dev.joid.lib.font.markup.TextMarkup;

public class TextInfoTest {

	private static final IFont       FONT   = () -> TextInfoTest.RENDERER;
	private static final ITextEffect EFFECT = new ITextEffect() {};
	private static final ITextMarkup MARKUP = (text, index, style) -> 0;

	private static final ITextRenderer RENDERER = new ITextRenderer() {

		@Override
		public FontBounds drawText(final double x, final double y, final String text, final TextInfo info) {
			return new FontBounds(this.getWidth(text, info), this.getHeight(text, info));
		}

		@Override
		public double getWidth(final String text, final TextInfo info) {
			return text.length() * 10D;
		}

		@Override
		public double getHeight(final String text, final TextInfo info) {
			return 20D;
		}

		@Override
		public double getLineHeight(final TextInfo info) {
			return 25D;
		}

	};

	@Test
	public void describesItself() {
		Assert.assertEquals(TextInfoTest.FONT + "x20.0 [" + Color.WHITE + "]", TextInfo.create(TextInfoTest.FONT, 20F, Color.WHITE).toString());
	}

	@Test
	public void copiesEveryStyle() {
		final TextInfo copy = TextInfo.create(TextInfoTest.FONT, 20F).weight(FontWeight.LIGHT).markups(TextInfoTest.MARKUP).effects(TextInfoTest.EFFECT).copy();
		Assert.assertSame(FontWeight.LIGHT, copy.getWeight());
		Assert.assertEquals(Collections.singletonList(TextInfoTest.MARKUP), copy.getMarkups());
		Assert.assertEquals(Collections.singletonList(TextInfoTest.EFFECT), copy.getEffects());
	}

	@Test
	public void derivesItsShadow() {
		final TextInfo info = TextInfo.create(TextInfoTest.FONT, 27F, Color.WHITE);
		Assert.assertNull(info.getShadowColor());
		Assert.assertEquals(2F, info.getShadowX(), 0F);
		Assert.assertEquals(2F, info.getShadowY(), 0F);
		Assert.assertEquals(Color.WHITE.darker(0.3F).r, info.shadow().getShadowColor().r, 0F);
		info.shadow(Color.RED).shadow(4F, 5F);
		Assert.assertSame(Color.RED, info.getShadowColor());
		Assert.assertEquals(4F, info.getShadowX(), 0F);
		Assert.assertEquals(5F, info.getShadowY(), 0F);
	}

	@Test
	public void derivesItsShadowOffsetFromItsCurrentSize() {
		final TextInfo info = TextInfo.create(TextInfoTest.FONT, 27F).shadow(Color.BLACK);
		info.fontSize(54F);
		Assert.assertEquals(4F, info.getShadowX(), 0F);
		Assert.assertEquals(4F, info.getShadowY(), 0F);
		Assert.assertEquals(2F, info.copy().fontSize(27F).getShadowX(), 0F);
	}

	@Test
	public void keepsAnExplicitShadowOffsetWhateverItsSize() {
		final TextInfo info = TextInfo.create(TextInfoTest.FONT, 27F).shadow(3F, 4F).fontSize(54F);
		Assert.assertEquals(3F, info.getShadowX(), 0F);
		Assert.assertEquals(4F, info.getShadowY(), 0F);
	}

	@Test
	public void scalesItsMeasures() {
		final TextInfo info = TextInfo.create(TextInfoTest.FONT, 20F);
		Assert.assertEquals(20D, info.dw("abcd", 2D), 0D);
		Assert.assertEquals(10D, info.dh("abcd", 2D), 0D);
		Assert.assertEquals(12.5D, info.dh(2D), 0D);
		Assert.assertEquals(45D, info.aw("abcd", 5D), 0D);
		Assert.assertEquals(25D, info.ah("abcd", 5D), 0D);
		Assert.assertEquals(30D, info.ah(5D), 0D);
	}

	@Test
	public void buildsTheBaseStyle() {
		final TextStyle style = TextInfo.create(TextInfoTest.FONT, 20F, Color.RED).weight(FontWeight.BOLD).italic(true).effects(TextInfoTest.EFFECT).getStyle();
		Assert.assertSame(FontWeight.BOLD, style.getWeight());
		Assert.assertTrue(style.isItalic());
		Assert.assertSame(Color.RED, style.getColor());
		Assert.assertEquals(Collections.singletonList(TextInfoTest.EFFECT), style.getEffects());
		Assert.assertSame(TextInfoTest.FONT, style.getFont());
	}

	@Test
	public void changesEveryProperty() {
		final IFont other = () -> TextInfoTest.RENDERER;
		final TextInfo info = TextInfo.create(TextInfoTest.FONT, 20F).font(other).fontSize(32F).letterSpacing(0.02F).lineHeight(1.2F).color(Color.RED).colored(false).italic(true);
		Assert.assertSame(other, info.getFont());
		Assert.assertEquals(32F, info.getFontSize(), 0F);
		Assert.assertEquals(0.02F, info.getLetterSpacing(), 0F);
		Assert.assertEquals(1.2F, info.getLineHeight(), 0F);
		Assert.assertSame(Color.RED, info.getColor());
		Assert.assertFalse(info.isColored());
		Assert.assertTrue(info.isItalic());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void exposesReadOnlyEffects() {
		TextInfo.create(TextInfoTest.FONT, 20F).effects(TextInfoTest.EFFECT).getEffects().set(0, TextInfoTest.EFFECT);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void exposesReadOnlyMarkups() {
		TextInfo.create(TextInfoTest.FONT, 20F).markups(TextInfoTest.MARKUP).getMarkups().set(0, TextInfoTest.MARKUP);
	}

	@Test
	public void takesTheWeightAtCreation() {
		final TextInfo info = TextInfo.create(TextInfoTest.FONT, FontWeight.LIGHT, 20F, Color.RED);
		Assert.assertSame(FontWeight.LIGHT, info.getWeight());
		Assert.assertSame(Color.RED, info.getColor());
		Assert.assertEquals(20F, info.getFontSize(), 0F);
		Assert.assertSame(FontWeight.BOLD, TextInfo.create(TextInfoTest.FONT, FontWeight.BOLD, 20F).getWeight());
		Assert.assertSame(Color.BLACK, TextInfo.create(TextInfoTest.FONT, FontWeight.BOLD, 20F).getColor());
	}

	@Test
	public void measuresThroughItsRenderer() {
		final TextInfo info = TextInfo.create(TextInfoTest.FONT, 20F);
		Assert.assertEquals(40D, info.getWidth("abcd"), 0D);
		Assert.assertEquals(20D, info.getHeight("abcd"), 0D);
		Assert.assertEquals(25D, info.getHeight(), 0D);
		Assert.assertEquals(40D, info.getBounds("abcd").getWidth(), 0D);
		Assert.assertEquals(20D, info.getBounds("abcd").getHeight(), 0D);
	}

	@Test
	public void startsRegularWithoutEffect() {
		final TextInfo info = TextInfo.create(TextInfoTest.FONT, 20F);
		Assert.assertSame(FontWeight.REGULAR, info.getWeight());
		Assert.assertTrue(info.getStyle().getEffects().isEmpty());
	}

	@Test
	public void restrictsTheMarkupsOnDemand() {
		TextMarkup.register(TextInfoTest.MARKUP);
		try {
			Assert.assertTrue(TextInfo.create(TextInfoTest.FONT, 20F).markups().getMarkups().isEmpty());
		} finally {
			TextMarkup.unregister(TextInfoTest.MARKUP);
		}
	}

	@Test
	public void changesTheWeightAfterCreation() {
		Assert.assertSame(FontWeight.BLACK, TextInfo.create(TextInfoTest.FONT, FontWeight.LIGHT, 20F).weight(FontWeight.BLACK).getWeight());
	}

	@Test
	public void followsTheRegisteredMarkupsByDefault() {
		TextMarkup.register(TextInfoTest.MARKUP);
		try {
			Assert.assertTrue(TextInfo.create(TextInfoTest.FONT, 20F).getMarkups().contains(TextInfoTest.MARKUP));
		} finally {
			TextMarkup.unregister(TextInfoTest.MARKUP);
		}
	}

	@Test
	public void startsBlackColoredAndUprightWithoutSpacing() {
		final TextInfo info = TextInfo.create(TextInfoTest.FONT, 20F);
		Assert.assertSame(TextInfoTest.FONT, info.getFont());
		Assert.assertSame(Color.BLACK, info.getColor());
		Assert.assertTrue(info.isColored());
		Assert.assertFalse(info.isItalic());
		Assert.assertEquals(0F, info.getLetterSpacing(), 0F);
		Assert.assertEquals(0F, info.getLineHeight(), 0F);
		Assert.assertTrue(info.getEffects().isEmpty());
	}

	@Test
	public void takesTheColorAtCreation() {
		final TextInfo info = TextInfo.create(TextInfoTest.FONT, 20F, Color.RED);
		Assert.assertSame(Color.RED, info.getColor());
		Assert.assertSame(FontWeight.REGULAR, info.getWeight());
		Assert.assertEquals(20F, info.getFontSize(), 0F);
	}

	@Test
	public void copiesEveryProperty() {
		final TextInfo copy = TextInfo.create(TextInfoTest.FONT, FontWeight.BOLD, 20F, Color.RED).letterSpacing(0.1F).lineHeight(1.5F).colored(false).italic(true).shadow(Color.BLUE).shadow(3F, 4F).shadowTint(0.25F).copy();
		Assert.assertSame(TextInfoTest.FONT, copy.getFont());
		Assert.assertEquals(20F, copy.getFontSize(), 0F);
		Assert.assertSame(FontWeight.BOLD, copy.getWeight());
		Assert.assertEquals(0.1F, copy.getLetterSpacing(), 0F);
		Assert.assertEquals(1.5F, copy.getLineHeight(), 0F);
		Assert.assertSame(Color.RED, copy.getColor());
		Assert.assertFalse(copy.isColored());
		Assert.assertTrue(copy.isItalic());
		Assert.assertSame(Color.BLUE, copy.getShadowColor());
		Assert.assertEquals(3F, copy.getShadowX(), 0F);
		Assert.assertEquals(4F, copy.getShadowY(), 0F);
		Assert.assertEquals(0.25F, copy.getShadowTint(), 0F);
	}

	@Test
	public void leavesTheOriginalUntouchedByItsCopy() {
		final TextInfo info = TextInfo.create(TextInfoTest.FONT, 20F, Color.RED);
		info.copy().fontSize(40F).weight(FontWeight.BOLD).color(Color.WHITE).italic(true).shadow(Color.BLUE);
		Assert.assertEquals(20F, info.getFontSize(), 0F);
		Assert.assertSame(FontWeight.REGULAR, info.getWeight());
		Assert.assertSame(Color.RED, info.getColor());
		Assert.assertFalse(info.isItalic());
		Assert.assertNull(info.getShadowColor());
	}

	@Test
	public void keepsFollowingTheRegisteredMarkupsInACopy() {
		final TextInfo copy = TextInfo.create(TextInfoTest.FONT, 20F).copy();
		TextMarkup.register(TextInfoTest.MARKUP);
		try {
			Assert.assertTrue(copy.getMarkups().contains(TextInfoTest.MARKUP));
		} finally {
			TextMarkup.unregister(TextInfoTest.MARKUP);
		}
	}

	@Test
	public void removesItsShadow() {
		Assert.assertNull(TextInfo.create(TextInfoTest.FONT, 20F).shadow().shadow(null).getShadowColor());
	}

}