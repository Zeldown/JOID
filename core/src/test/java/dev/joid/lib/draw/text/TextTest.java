package dev.joid.lib.draw.text;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import dev.joid.internal.JOID;
import dev.joid.lib.font.FontBounds;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.IFont;
import dev.joid.lib.font.ITextRenderer;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.font.impl.msdf.MsdfFontLoader;
import dev.joid.lib.font.markup.ITextMarkup;
import dev.joid.lib.font.markup.TextMarkup;
import dev.joid.lib.font.modifier.TextModifier;
import dev.joid.lib.utils.align.Align;

public class TextTest {

	private static final IFont       FONT = () -> TextTest.RENDERER;
	private static final ITextMarkup TAG  = (text, index, style) -> text.startsWith("<b>", index) ? 3 : text.startsWith("</b>", index) ? 4 : 0;

	private static final ITextRenderer RENDERER = new ITextRenderer() {

		@Override
		public FontBounds drawText(final double x, final double y, final String text, final TextInfo info) {
			return FontBounds.of(this.getWidth(text, info), this.getHeight(text, info));
		}

		@Override
		public double getWidth(final String text, final TextInfo info) {
			return text.length() * info.getFontSize();
		}

		@Override
		public double getHeight(final String text, final TextInfo info) {
			return info.getFontSize() * 2D;
		}

		@Override
		public double getLineHeight(final TextInfo info) {
			return info.getFontSize() * 2D;
		}

	};

	private static MsdfFont montserrat;

	@BeforeClass
	public static void load() {
		TextTest.montserrat = MsdfFontLoader.load(JOID.class.getResourceAsStream("/assets/dev/fonts/Montserrat/Montserrat-Regular.ttf"), JOID.class.getResourceAsStream("/assets/dev/fonts/Montserrat/Montserrat-Bold.ttf")).join();
	}

	@Test
	public void startsEmpty() {
		final Text text = Text.create();
		Assert.assertTrue(text.isEmpty());
		Assert.assertEquals("", text.getText());
		Assert.assertEquals("", text.getRawText());
		Assert.assertEquals(0D, text.getWidth(), 0D);
		Assert.assertEquals(0D, text.getHeight(), 0D);
	}

	@Test
	public void startsAtTheTopLeftWithoutOverflowNorModifier() {
		final Text text = Text.create("ab", TextTest.info());
		Assert.assertSame(Align.START, text.getAlignX());
		Assert.assertSame(Align.START, text.getAlignY());
		Assert.assertSame(TextOverflow.NONE, text.getOverflow());
		Assert.assertNull(text.getModifier());
	}

	@Test
	public void joinsTheTextOfItsElements() {
		final Text text = Text.create(TextElement.create("ab", TextTest.info()), TextElement.create(12, TextTest.info()));
		Assert.assertFalse(text.isEmpty());
		Assert.assertEquals(2, text.getElementList().size());
		Assert.assertEquals("ab12", text.getText());
		Assert.assertEquals("ab12", text.getRawText());
	}

	@Test
	public void keepsItsOwnListOfElements() {
		final TextElement[] elements = {TextElement.create("ab", TextTest.info())};
		final Text text = Text.create().add(elements);
		elements[0] = TextElement.create("cd", TextTest.info());
		Assert.assertEquals("ab", text.getText());
		Assert.assertEquals(1, text.getElementList().size());
	}

	@Test
	public void readsItsSourceOnEveryCall() {
		final StringBuilder builder = new StringBuilder("a");
		final Text object = Text.create(builder, TextTest.info());
		final Text supplier = Text.create(() -> builder.length(), TextTest.info());
		builder.append("b");
		Assert.assertEquals("ab", object.getText());
		Assert.assertEquals("2", supplier.getText());
	}

	@Test
	public void alignsBothAxesAtOnce() {
		final Text text = Text.create("ab", TextTest.info()).align(Align.CENTER);
		Assert.assertSame(Align.CENTER, text.getAlignX());
		Assert.assertSame(Align.CENTER, text.getAlignY());
		text.alignY(Align.END);
		Assert.assertSame(Align.CENTER, text.getAlignX());
		Assert.assertSame(Align.END, text.getAlignY());
	}

	@Test
	public void returnsItsElementsByIndex() {
		final TextElement first = TextElement.create("ab", TextTest.info());
		final TextElement second = TextElement.create("cd", TextTest.info());
		final Text text = Text.create(first, second);
		Assert.assertSame(first, text.get(0));
		Assert.assertSame(second, text.get(1));
	}

	@Test
	public void measuresTheWidthOfItsElementsAndTheHighestOne() {
		final Text text = Text.create(TextElement.create("abc", TextTest.info()), TextElement.create("d", TextTest.info().fontSize(20F)));
		Assert.assertEquals(50D, text.getWidth(), 0D);
		Assert.assertEquals(40D, text.getHeight(), 0D);
		Assert.assertEquals(50D, text.getBounds().getWidth(), 0D);
		Assert.assertEquals(40D, text.getBounds().getHeight(), 0D);
	}

	@Test
	public void scalesItsMeasures() {
		final Text text = Text.create("abcd", TextTest.info());
		Assert.assertEquals(20D, text.dw(2D), 0D);
		Assert.assertEquals(10D, text.dh(2D), 0D);
		Assert.assertEquals(45D, text.aw(5D), 0D);
		Assert.assertEquals(25D, text.ah(5D), 0D);
	}

	@Test
	public void modifiesItsTextButNotItsRawText() {
		final Text text = Text.create("Hello World", TextTest.info()).modifier(TextModifier.UPPER_CASE);
		Assert.assertSame(TextModifier.UPPER_CASE, text.getModifier());
		Assert.assertEquals("HELLO WORLD", text.getText());
		Assert.assertEquals("HELLO WORLD", text.getText(text.get(0)));
		Assert.assertEquals("Hello World", text.getRawText());
	}

	@Test
	public void readsAnElementAsIsWithoutModifier() {
		final Text text = Text.create("Hello", TextTest.info());
		Assert.assertEquals("Hello", text.getText(text.get(0)));
	}

	@Test
	public void measuresItsModifiedText() {
		final Text text = Text.create("ab", TextTest.info());
		Assert.assertEquals(20D, text.getWidth(), 0D);
		Assert.assertEquals(40D, text.modifier(value -> value + value).getWidth(), 0D);
		Assert.assertEquals(20D, text.modifier(null).getWidth(), 0D);
		Assert.assertEquals("ab", text.getText());
	}

	@Test
	public void remeasuresAfterAFailedMeasure() {
		final AtomicBoolean failing = new AtomicBoolean(true);
		final Text text = Text.create("ab", TextTest.info()).modifier(value -> {
			if (failing.get()) {
				throw new IllegalStateException("The modifier failed");
			}
			return value;
		});
		try {
			text.getWidth();
			Assert.fail("The modifier fails");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The modifier failed", expected.getMessage());
		}
		failing.set(false);
		Assert.assertEquals(20D, text.getWidth(), 0D);
		Assert.assertEquals(20D, text.getHeight(), 0D);
	}

	@Test
	public void remeasuresOnceAnElementIsAddedOrRemoved() {
		final TextElement element = TextElement.create("cd", TextTest.info());
		final Text text = Text.create("ab", TextTest.info());
		Assert.assertEquals(20D, text.getWidth(), 0D);
		Assert.assertEquals(40D, text.add(element).getWidth(), 0D);
		Assert.assertEquals("abcd", text.getText());
		Assert.assertEquals(20D, text.remove(element).getWidth(), 0D);
		Assert.assertEquals("ab", text.getText());
	}

	@Test
	public void forgetsEveryElementOnClear() {
		final Text text = Text.create("ab", TextTest.info());
		Assert.assertEquals(20D, text.getWidth(), 0D);
		Assert.assertEquals(0D, text.clear().getWidth(), 0D);
		Assert.assertEquals(0D, text.getHeight(), 0D);
		Assert.assertTrue(text.isEmpty());
	}

	@Test
	public void appendsAnotherTextOrElements() {
		final Text text = Text.create("ab", TextTest.info());
		Assert.assertEquals(20D, text.getWidth(), 0D);
		Assert.assertEquals(40D, text.add(Text.create("cd", TextTest.info())).getWidth(), 0D);
		Assert.assertEquals(60D, text.add(TextElement.create("e", TextTest.info()), TextElement.create("f", TextTest.info())).getWidth(), 0D);
		Assert.assertEquals("abcdef", text.getText());
	}

	@Test
	public void remeasuresOnceItsTextChanges() {
		final Text text = Text.create("ab", TextTest.info());
		Assert.assertEquals(20D, text.getWidth(), 0D);
		Assert.assertEquals(30D, text.text("abc").getWidth(), 0D);
		Assert.assertEquals("abc", text.getText());
	}

	@Test
	public void changesTheTextOfTheIndexedElement() {
		final Text text = Text.create(TextElement.create("ab", TextTest.info()), TextElement.create("cd", TextTest.info()));
		Assert.assertEquals(40D, text.getWidth(), 0D);
		Assert.assertEquals(50D, text.text(1, "xyz").getWidth(), 0D);
		Assert.assertEquals("abxyz", text.getText());
	}

	@Test
	public void convertsANewValueToText() {
		final Text text = Text.create(TextElement.create("ab", TextTest.info()), TextElement.create("cd", TextTest.info()));
		Assert.assertEquals("42cd", text.text(42).getText());
		Assert.assertEquals("421.5", text.text(1, 1.5D).getText());
	}

	@Test
	public void remeasuresOnceItsInfoChanges() {
		final Text text = Text.create(TextElement.create("ab", TextTest.info()), TextElement.create("cd", TextTest.info()));
		Assert.assertEquals(40D, text.getWidth(), 0D);
		Assert.assertEquals(20D, text.getHeight(), 0D);
		Assert.assertEquals(60D, text.info(TextTest.info().fontSize(20F)).getWidth(), 0D);
		Assert.assertEquals(40D, text.getHeight(), 0D);
		Assert.assertEquals(80D, text.info(1, TextTest.info().fontSize(20F)).getWidth(), 0D);
		Assert.assertEquals(20F, text.get(1).getInfo().getFontSize(), 0F);
	}

	@Test
	public void remeasuresOnceAnElementTakesAnotherInfo() {
		final TextInfo info = TextTest.info();
		final Text text = Text.create("ab", info);
		Assert.assertEquals(20D, text.getWidth(), 0D);
		text.get(0).info(info.withFontSize(20F));
		Assert.assertEquals(40D, text.getWidth(), 0D);
		Assert.assertEquals(40D, text.getHeight(), 0D);
	}

	@Test
	public void remeasuresOnceItsInfoChangesInPlace() {
		final TextInfo info = TextTest.info();
		final Text text = Text.create("ab", info);
		Assert.assertEquals(20D, text.getWidth(), 0D);
		info.fontSize(20F);
		Assert.assertEquals(40D, text.getWidth(), 0D);
		Assert.assertEquals(40D, text.getHeight(), 0D);
	}

	@Test
	public void remeasuresOnceItsMarkupChangesInPlace() {
		final TextInfo info = TextTest.montserrat();
		final Text text = Text.create("*ab", info);
		Assert.assertEquals(info.getWidth("*ab"), text.getWidth(), 0D);
		info.markups((value, index, style) -> value.charAt(index) == '*' ? 1 : 0);
		Assert.assertEquals(info.getWidth("ab"), text.getWidth(), 0D);
	}

	@Test
	public void remeasuresOnceTheModifierOfAnElementChanges() {
		final Text text = Text.create("ab", TextTest.info());
		Assert.assertEquals(20D, text.getWidth(), 0D);
		text.get(0).modifier(value -> value + value);
		Assert.assertEquals(40D, text.getWidth(), 0D);
	}

	@Test
	public void remeasuresOnceTheRegisteredMarkupsChange() {
		final ITextMarkup markup = (value, index, style) -> value.charAt(index) == '*' ? 1 : 0;
		final TextInfo info = TextTest.montserrat();
		final Text text = Text.create("*ab", info);
		Assert.assertEquals(info.getWidth("*ab"), text.getWidth(), 0D);
		TextMarkup.register(markup);
		try {
			Assert.assertEquals(info.getWidth("ab"), text.getWidth(), 0D);
		} finally {
			TextMarkup.unregister(markup);
		}
	}

	@Test
	public void readsItsRawTextWithoutTheModifiersOfItsElements() {
		final Text text = Text.create(TextElement.create("ab", TextTest.info()).modifier(TextModifier.UPPER_CASE), TextElement.create("cd", TextTest.info())).modifier(TextModifier.CAPITALIZE);
		Assert.assertEquals("abcd", text.getRawText());
		Assert.assertEquals("ABcd", text.getText());
	}

	@Test
	public void refusesAnIndexOutsideItsElements() {
		final Text text = Text.create("ab", TextTest.info());
		try {
			text.text(1, "x");
			Assert.fail("The text has a single element");
		} catch (final IndexOutOfBoundsException expected) {
			Assert.assertEquals("The text has no element at index 1, give an index from 0 and lower than its element count (1)", expected.getMessage());
		}
		try {
			text.info(-1, TextTest.info().fontSize(20F));
			Assert.fail("An index cannot be negative");
		} catch (final IndexOutOfBoundsException expected) {
			Assert.assertEquals("The text has no element at index -1, give an index from 0 and lower than its element count (1)", expected.getMessage());
		}
		try {
			Text.create().text("x");
			Assert.fail("An empty text has no element");
		} catch (final IndexOutOfBoundsException expected) {
			Assert.assertEquals("The text has no element at index 0, give an index from 0 and lower than its element count (0)", expected.getMessage());
		}
		Assert.assertEquals("ab", text.getText());
		Assert.assertEquals(20D, text.getWidth(), 0D);
	}

	@Test
	public void copiesItsElementsAndSettings() {
		final Text copy = Text.create("ab", TextTest.info()).alignX(Align.CENTER).alignY(Align.END).overflow(TextOverflow.ELLIPSIS).modifier(TextModifier.UPPER_CASE).copy();
		Assert.assertEquals("AB", copy.getText());
		Assert.assertSame(Align.CENTER, copy.getAlignX());
		Assert.assertSame(Align.END, copy.getAlignY());
		Assert.assertSame(TextOverflow.ELLIPSIS, copy.getOverflow());
		Assert.assertSame(TextModifier.UPPER_CASE, copy.getModifier());
	}

	@Test
	public void keepsTheElementsOfAnAddedText() {
		final Text added = Text.create("ab", TextTest.info());
		final Text text = Text.create().add(added);
		text.text(0, "cd");
		Assert.assertEquals("ab", added.getText());
		Assert.assertEquals("cd", text.getText());
	}

	@Test
	public void keepsItsElementsWhenItsCopyGrows() {
		final Text text = Text.create("ab", TextTest.info());
		text.copy().add(TextElement.create("cd", TextTest.info()));
		Assert.assertEquals("ab", text.getText());
		Assert.assertEquals(20D, text.getWidth(), 0D);
	}

	@Test
	public void keepsItsElementsWhenItsCopyChangesThem() {
		final Text text = Text.create("ab", TextTest.info());
		text.copy().text("x").info(TextInfo.create(TextTest.FONT, 20F));
		Assert.assertEquals("ab", text.getText());
		Assert.assertEquals(20D, text.getWidth(), 0D);
	}

	@Test
	public void copiesItsSettingsWithoutItsElements() {
		final Text copy = Text.create("ab", TextTest.info()).alignX(Align.CENTER).alignY(Align.END).overflow(TextOverflow.ELLIPSIS).modifier(TextModifier.UPPER_CASE).copyProperties();
		Assert.assertTrue(copy.isEmpty());
		Assert.assertSame(Align.CENTER, copy.getAlignX());
		Assert.assertSame(Align.END, copy.getAlignY());
		Assert.assertSame(TextOverflow.ELLIPSIS, copy.getOverflow());
		Assert.assertSame(TextModifier.UPPER_CASE, copy.getModifier());
	}

	@Test
	public void changesItsAlignmentsAndOverflow() {
		final Text text = Text.create("ab", TextTest.info()).alignX(Align.CENTER).alignY(Align.END);
		Assert.assertSame(Align.CENTER, text.getAlignX());
		Assert.assertSame(Align.END, text.getAlignY());
		text.alignX(Align.END).alignY(Align.CENTER).overflow(TextOverflow.HYPHEN);
		Assert.assertSame(Align.END, text.getAlignX());
		Assert.assertSame(Align.CENTER, text.getAlignY());
		Assert.assertSame(TextOverflow.HYPHEN, text.getOverflow());
	}

	@Test
	public void describesItself() {
		final Text text = Text.create(TextElement.create("ab", TextTest.info()), TextElement.create("cd", TextTest.info())).alignX(Align.CENTER).alignY(Align.END).overflow(TextOverflow.ELLIPSIS).modifier(TextModifier.UPPER_CASE);
		Assert.assertEquals("\"ABCD\" - CENTER / END - ELLIPSIS [2]", text.toString());
	}

	@Test
	public void measuresEachElementWithItsFont() {
		final TextInfo regular = TextTest.montserrat();
		final TextInfo bold = TextTest.montserrat().weight(FontWeight.BOLD);
		final Text text = Text.create(TextElement.create("Hello ", regular), TextElement.create("world", bold));
		Assert.assertEquals(regular.getWidth("Hello ") + bold.getWidth("world"), text.getWidth(), 0.0001D);
		Assert.assertEquals(Math.max(regular.getHeight(), bold.getHeight()), text.getHeight(), 0D);
	}

	@Test
	public void spacesItsLettersByAFractionOfTheFontSize() {
		final double plain = Text.create("abc", TextTest.montserrat()).getWidth();
		Assert.assertEquals(plain + 20D, Text.create("abc", TextTest.montserrat().letterSpacing(0.1F)).getWidth(), 0.0001D);
		Assert.assertEquals(plain - 4D, Text.create("abc", TextTest.montserrat().letterSpacing(-0.02F)).getWidth(), 0.0001D);
	}

	@Test
	public void spacesTheLettersOfTwoElementsAtTheirJunction() {
		final TextInfo info = TextTest.montserrat().letterSpacing(0.1F);
		final double plain = Text.create("ab", TextTest.montserrat()).getWidth() + Text.create("cd", TextTest.montserrat()).getWidth();
		Assert.assertEquals(plain + 30D, Text.create(TextElement.create("ab", info), TextElement.create("cd", info)).getWidth(), 0.0001D);
		Assert.assertEquals(plain + 30D, Text.create(TextElement.create("ab", info), TextElement.create("", info), TextElement.create("cd", info)).getWidth(), 0.0001D);
	}

	@Test
	public void takesItsLineHeightAsAFractionOfTheFontSize() {
		Assert.assertEquals(150D, Text.create("abc", TextTest.montserrat().lineHeight(1.5F)).getHeight(), 0D);
		Assert.assertEquals(80D, Text.create("abc", TextTest.montserrat().lineHeight(0.8F)).getBounds().getHeight(), 0.0001D);
	}

	@Test
	public void leavesItsMarkupOutOfItsWidth() {
		final TextInfo info = TextTest.montserrat().markups((text, index, style) -> text.charAt(index) == '*' ? 1 : 0);
		final Text text = Text.create("*a*b*", info);
		Assert.assertEquals(Text.create("ab", info).getWidth(), text.getWidth(), 0D);
		Assert.assertEquals("*a*b*", text.getText());
	}

	@Test
	public void modifiesTheTextAroundItsMarkup() {
		final TextInfo info = TextTest.info().markups(TextTest.TAG);
		final TextElement element = TextElement.create("<b>ab</b> cd", info).modifier(TextModifier.UPPER_CASE);
		Assert.assertEquals("<b>AB</b> CD", Text.create("<b>ab</b> cd", info).modifier(TextModifier.UPPER_CASE).getText());
		Assert.assertEquals("<b>ab</b>Cd", Text.create("<b>ab</b> cd", info).modifier(TextModifier.CAMEL_CASE).getText());
		Assert.assertEquals("Hello <b>world</b>", Text.create("hello <b>world</b>", info).modifier(TextModifier.CAPITALIZE).getText());
		Assert.assertEquals("<b>AB</b> CD", Text.create(element).getText());
	}

	@Test
	public void measuresTheModifiedTextWithoutItsMarkup() {
		final TextInfo info = TextTest.montserrat().markups(TextTest.TAG);
		Assert.assertEquals(Text.create("AB", info).getWidth(), Text.create("<b>ab</b>", info).modifier(TextModifier.UPPER_CASE).getWidth(), 0D);
	}

	@Test
	public void measuresTheWeightChosenByItsMarkup() {
		final TextInfo info = TextTest.montserrat().markups((text, index, style) -> {
			if (text.charAt(index) != '*') {
				return 0;
			}

			style.weight(FontWeight.BOLD);
			return 1;
		});
		Assert.assertEquals(TextTest.montserrat().weight(FontWeight.BOLD).getWidth("Hello"), Text.create("*Hello", info).getWidth(), 0.0001D);
	}

	@Test
	public void remeasuresItsSuppliedTextOnceItChanges() {
		final StringBuilder builder = new StringBuilder("ab");
		final Text text = Text.create(() -> builder, TextTest.info());
		Assert.assertEquals(20D, text.getWidth(), 0D);
		builder.append("cd");
		Assert.assertEquals(40D, text.getWidth(), 0D);
	}

	@Test
	public void replacesItsTextWithASupplier() {
		Assert.assertEquals("cd", Text.create("ab", TextTest.info()).text(() -> "cd").getText());
		Assert.assertEquals("cd", Text.create("ab", TextTest.info()).text(0, () -> "cd").getText());
	}

	@Test
	public void modifiesItsElementsAsItModifiesItsText() {
		final Text text = Text.create(TextElement.create("hello ", TextTest.info()), TextElement.create("world", TextTest.info())).modifier(TextModifier.CAPITALIZE);
		Assert.assertEquals(text.getText(), text.getText(text.get(0)) + text.getText(text.get(1)));
	}

	@Test
	public void readsATypedSupplier() {
		final Supplier<String> supplier = () -> "ab";
		Assert.assertEquals("ab", Text.create(supplier, TextTest.info()).getText());
		Assert.assertEquals("ab", Text.create("cd", TextTest.info()).text(supplier).getText());
	}

	@Test
	public void modifiesItsElementsAsOneText() {
		final Text text = Text.create(TextElement.create("hello ", TextTest.info()), TextElement.create("big world", TextTest.info())).modifier(TextModifier.CAMEL_CASE);
		Assert.assertEquals("hello", text.getText(text.get(0)));
		Assert.assertEquals("BigWorld", text.getText(text.get(1)));
		Assert.assertEquals(130D, text.getWidth(), 0D);
	}

	@Test
	public void modifiesAnElementOfAnotherTextOnItsOwn() {
		final Text text = Text.create(TextElement.create("hello ", TextTest.info()), TextElement.create("world", TextTest.info())).modifier(TextModifier.CAPITALIZE);
		Assert.assertEquals("world", text.getText(text.get(1)));
		Assert.assertEquals("World", text.getText(TextElement.create("world", TextTest.info())));
	}

	private static TextInfo info() {
		return TextInfo.create(TextTest.FONT, 10F);
	}

	private static TextInfo montserrat() {
		return TextInfo.create(TextTest.montserrat, 100F);
	}

}