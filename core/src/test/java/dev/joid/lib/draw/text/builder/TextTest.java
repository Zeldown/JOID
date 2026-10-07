package dev.joid.lib.draw.text.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import dev.joid.internal.JOID;
import dev.joid.lib.draw.text.builder.modifier.TextModifier;
import dev.joid.lib.draw.text.builder.utils.TextOverflow;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.IFont;
import dev.joid.lib.font.IFontProvider;
import dev.joid.lib.font.dto.FontBounds;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.font.impl.msdf.MsdfFontLoader;
import dev.joid.lib.utils.align.Align;

public class TextTest {

	private static final IFont FONT = () -> TextTest.PROVIDER;

	private static final IFontProvider PROVIDER = new IFontProvider() {

		@Override
		public FontBounds drawText(final double x, final double y, final String text, final TextInfo info) {
			return new FontBounds(this.getWidth(text, info), this.getHeight(text, info));
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
		Assert.assertSame(Align.START, text.getHorizontalAlignment());
		Assert.assertSame(Align.START, text.getVerticalAlignment());
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
		final List<TextElement> elementList = new ArrayList<>();
		elementList.add(TextElement.create("ab", TextTest.info()));
		final Text text = Text.create(elementList);
		elementList.add(TextElement.create("cd", TextTest.info()));
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
	public void takesItsHorizontalAlignmentAtCreation() {
		final Text object = Text.create("ab", TextTest.info(), Align.END);
		final Text supplier = Text.create(() -> "cd", TextTest.info(), Align.CENTER);
		Assert.assertEquals("ab", object.getText());
		Assert.assertSame(Align.END, object.getHorizontalAlignment());
		Assert.assertEquals("cd", supplier.getText());
		Assert.assertSame(Align.CENTER, supplier.getHorizontalAlignment());
	}

	@Test
	public void takesItsOverflowAtCreation() {
		final Text object = Text.create("ab", TextTest.info(), TextOverflow.ELLIPSIS);
		final Text supplier = Text.create(() -> "cd", TextTest.info(), TextOverflow.DOT);
		Assert.assertEquals("ab", object.getText());
		Assert.assertSame(TextOverflow.ELLIPSIS, object.getOverflow());
		Assert.assertEquals("cd", supplier.getText());
		Assert.assertSame(TextOverflow.DOT, supplier.getOverflow());
	}

	@Test
	public void takesItsAlignmentAndOverflowAtCreation() {
		final Text object = Text.create("ab", TextTest.info(), Align.END, TextOverflow.HYPHEN);
		final Text supplier = Text.create(() -> "cd", TextTest.info(), Align.CENTER, TextOverflow.DOT);
		Assert.assertEquals("ab", object.getText());
		Assert.assertSame(Align.END, object.getHorizontalAlignment());
		Assert.assertSame(TextOverflow.HYPHEN, object.getOverflow());
		Assert.assertEquals("cd", supplier.getText());
		Assert.assertSame(Align.CENTER, supplier.getHorizontalAlignment());
		Assert.assertSame(TextOverflow.DOT, supplier.getOverflow());
	}

	@Test
	public void takesBothAlignmentsAtCreation() {
		final Text object = Text.create("ab", TextTest.info(), Align.END, Align.CENTER);
		final Text supplier = Text.create(() -> "cd", TextTest.info(), Align.CENTER, Align.END);
		Assert.assertEquals("ab", object.getText());
		Assert.assertSame(Align.END, object.getHorizontalAlignment());
		Assert.assertSame(Align.CENTER, object.getVerticalAlignment());
		Assert.assertEquals("cd", supplier.getText());
		Assert.assertSame(Align.CENTER, supplier.getHorizontalAlignment());
		Assert.assertSame(Align.END, supplier.getVerticalAlignment());
	}

	@Test
	public void takesEverySettingAtCreation() {
		final Text object = Text.create("ab", TextTest.info(), Align.END, Align.CENTER, TextOverflow.ELLIPSIS);
		final Text supplier = Text.create(() -> "cd", TextTest.info(), Align.CENTER, Align.END, TextOverflow.HYPHEN);
		Assert.assertEquals("ab", object.getText());
		Assert.assertSame(Align.END, object.getHorizontalAlignment());
		Assert.assertSame(Align.CENTER, object.getVerticalAlignment());
		Assert.assertSame(TextOverflow.ELLIPSIS, object.getOverflow());
		Assert.assertEquals("cd", supplier.getText());
		Assert.assertSame(Align.CENTER, supplier.getHorizontalAlignment());
		Assert.assertSame(Align.END, supplier.getVerticalAlignment());
		Assert.assertSame(TextOverflow.HYPHEN, supplier.getOverflow());
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
	public void appendsAnotherTextOrAListOfElements() {
		final Text text = Text.create("ab", TextTest.info());
		Assert.assertEquals(20D, text.getWidth(), 0D);
		Assert.assertEquals(40D, text.add(Text.create("cd", TextTest.info())).getWidth(), 0D);
		Assert.assertEquals(50D, text.addAll(Collections.singletonList(TextElement.create("e", TextTest.info()))).getWidth(), 0D);
		Assert.assertEquals("abcde", text.getText());
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
	public void ignoresAnIndexOutsideItsElements() {
		final Text text = Text.create("ab", TextTest.info());
		Assert.assertEquals(20D, text.getWidth(), 0D);
		Assert.assertSame(text, text.text(-1, "x").text(1, "x").text(-1, () -> "x").text(1, () -> "x").info(-1, TextTest.info().fontSize(20F)).info(1, TextTest.info().fontSize(20F)));
		Assert.assertEquals("ab", text.getText());
		Assert.assertEquals(20D, text.getWidth(), 0D);
		Assert.assertTrue(Text.create().text("x").text(() -> "x").info(TextTest.info()).isEmpty());
	}

	@Test
	public void copiesItsElementsAndSettings() {
		final Text copy = Text.create("ab", TextTest.info(), Align.CENTER, Align.END, TextOverflow.ELLIPSIS).modifier(TextModifier.UPPER_CASE).copy();
		Assert.assertEquals("AB", copy.getText());
		Assert.assertSame(Align.CENTER, copy.getHorizontalAlignment());
		Assert.assertSame(Align.END, copy.getVerticalAlignment());
		Assert.assertSame(TextOverflow.ELLIPSIS, copy.getOverflow());
		Assert.assertSame(TextModifier.UPPER_CASE, copy.getModifier());
	}

	@Test
	public void keepsItsElementsWhenItsCopyGrows() {
		final Text text = Text.create("ab", TextTest.info());
		text.copy().add(TextElement.create("cd", TextTest.info()));
		Assert.assertEquals("ab", text.getText());
		Assert.assertEquals(20D, text.getWidth(), 0D);
	}

	@Test
	public void copiesItsSettingsWithoutItsElements() {
		final Text copy = Text.create("ab", TextTest.info(), Align.CENTER, Align.END, TextOverflow.ELLIPSIS).modifier(TextModifier.UPPER_CASE).copyProperties();
		Assert.assertTrue(copy.isEmpty());
		Assert.assertSame(Align.CENTER, copy.getHorizontalAlignment());
		Assert.assertSame(Align.END, copy.getVerticalAlignment());
		Assert.assertSame(TextOverflow.ELLIPSIS, copy.getOverflow());
		Assert.assertSame(TextModifier.UPPER_CASE, copy.getModifier());
	}

	@Test
	public void copiesWithAnotherModifier() {
		final Text copy = Text.create("Ab", TextTest.info(), Align.CENTER, Align.END, TextOverflow.ELLIPSIS).modifier(TextModifier.UPPER_CASE).copyWithModifier(TextModifier.LOWER_CASE);
		Assert.assertEquals("ab", copy.getText());
		Assert.assertSame(TextModifier.LOWER_CASE, copy.getModifier());
		Assert.assertSame(Align.CENTER, copy.getHorizontalAlignment());
		Assert.assertSame(Align.END, copy.getVerticalAlignment());
		Assert.assertSame(TextOverflow.ELLIPSIS, copy.getOverflow());
	}

	@Test
	public void copiesWithAnotherVerticalAlignment() {
		final Text copy = Text.create("ab", TextTest.info(), Align.CENTER, Align.END, TextOverflow.ELLIPSIS).modifier(TextModifier.UPPER_CASE).copyWithVerticalAlign(Align.START);
		Assert.assertEquals("AB", copy.getText());
		Assert.assertSame(Align.START, copy.getVerticalAlignment());
		Assert.assertSame(Align.CENTER, copy.getHorizontalAlignment());
		Assert.assertSame(TextOverflow.ELLIPSIS, copy.getOverflow());
		Assert.assertSame(TextModifier.UPPER_CASE, copy.getModifier());
	}

	@Test
	public void copiesWithAnotherHorizontalAlignment() {
		final Text copy = Text.create("ab", TextTest.info(), Align.CENTER, TextOverflow.ELLIPSIS).modifier(TextModifier.UPPER_CASE).copyWithHorizontalAlign(Align.END);
		Assert.assertEquals("AB", copy.getText());
		Assert.assertSame(Align.END, copy.getHorizontalAlignment());
		Assert.assertSame(TextOverflow.ELLIPSIS, copy.getOverflow());
		Assert.assertSame(TextModifier.UPPER_CASE, copy.getModifier());
	}

	@Test
	public void copiesWithAnotherOverflow() {
		final Text copy = Text.create("ab", TextTest.info(), Align.CENTER, Align.END, TextOverflow.ELLIPSIS).modifier(TextModifier.UPPER_CASE).copyWithOverflow(TextOverflow.DOT);
		Assert.assertEquals("AB", copy.getText());
		Assert.assertSame(TextOverflow.DOT, copy.getOverflow());
		Assert.assertSame(Align.CENTER, copy.getHorizontalAlignment());
		Assert.assertSame(Align.END, copy.getVerticalAlignment());
		Assert.assertSame(TextModifier.UPPER_CASE, copy.getModifier());
	}

	@Test
	public void changesItsAlignmentAndOverflow() {
		final Text text = Text.create("ab", TextTest.info()).horizontalAlign(Align.CENTER).verticalAlign(Align.END);
		Assert.assertSame(Align.CENTER, text.getHorizontalAlignment());
		Assert.assertSame(Align.END, text.getVerticalAlignment());
		text.horizontalAlign(Align.END).verticalAlign(Align.CENTER).overflow(TextOverflow.HYPHEN);
		Assert.assertSame(Align.END, text.getHorizontalAlignment());
		Assert.assertSame(Align.CENTER, text.getVerticalAlignment());
		Assert.assertSame(TextOverflow.HYPHEN, text.getOverflow());
	}

	@Test
	public void describesItself() {
		final Text text = Text.create(TextElement.create("ab", TextTest.info()), TextElement.create("cd", TextTest.info())).horizontalAlign(Align.CENTER).verticalAlign(Align.END).overflow(TextOverflow.ELLIPSIS).modifier(TextModifier.UPPER_CASE);
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
	public void keepsItsVerticalAlignmentInACopyWithAnotherHorizontalAlignment() {
		Assert.assertSame(Align.END, Text.create("ab", TextTest.info(), Align.CENTER, Align.END).copyWithHorizontalAlign(Align.START).getVerticalAlignment());
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