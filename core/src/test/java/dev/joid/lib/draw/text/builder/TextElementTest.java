package dev.joid.lib.draw.text.builder;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.internal.JOID;
import dev.joid.lib.draw.text.builder.modifier.TextModifier;
import dev.joid.lib.font.FontUsage;
import dev.joid.lib.font.dto.TextInfo;

public class TextElementTest {

	private static final TextInfo INFO = TextInfo.create(() -> null, 10F);

	@Test
	public void remembersWhereItWasCreatedInDevMode() {
		final boolean previous = JOID.inst().isDevMode();
		JOID.inst().setDevMode(true);
		try {
			final TextElement element = Text.create("text", TextElementTest.INFO).getElementList().get(0);
			Assert.assertEquals(TextElementTest.class.getName(), element.getOrigin()[0].getClassName());
			Assert.assertEquals("remembersWhereItWasCreatedInDevMode", element.getOrigin()[0].getMethodName());
			Assert.assertSame(element.getOrigin(), element.copyWithText("other").getOrigin());
			Assert.assertSame(element.getOrigin(), element.copy().getOrigin());
		} finally {
			JOID.inst().setDevMode(previous);
		}
	}

	@Test
	public void remembersNothingOutsideDevMode() {
		Assert.assertNull(TextElement.create("text", TextElementTest.INFO).getOrigin());
	}

	@Test
	public void exposesTheOriginOnlyDuringItsUsage() {
		final StackTraceElement[] origin = {new StackTraceElement("com.example.Screen", "init", "Screen.java", 42)};
		Assert.assertEquals(1D, FontUsage.trace(origin, () -> FontUsage.getOrigin().get() == origin ? 1D : 0D), 0D);
		Assert.assertFalse(FontUsage.getOrigin().isPresent());
		Assert.assertEquals(2D, FontUsage.trace(null, () -> FontUsage.getOrigin().isPresent() ? 0D : 2D), 0D);
	}

	@Test
	public void keepsItsOriginInEveryCopy() {
		final boolean previous = JOID.inst().isDevMode();
		JOID.inst().setDevMode(true);
		try {
			final TextElement element = TextElement.create("text", TextElementTest.INFO);
			Assert.assertNotNull(element.getOrigin());
			Assert.assertSame(element.getOrigin(), element.copyWithText(() -> "other").getOrigin());
			Assert.assertSame(element.getOrigin(), element.copyWithInfo(TextElementTest.INFO).getOrigin());
			Assert.assertSame(element.getOrigin(), element.copyWithModifier(TextModifier.UPPER_CASE).getOrigin());
		} finally {
			JOID.inst().setDevMode(previous);
		}
	}

	@Test
	public void writesEveryPrimitiveAsText() {
		Assert.assertEquals("12", TextElement.create(12, TextElementTest.INFO).getText());
		Assert.assertEquals("34", TextElement.create(34L, TextElementTest.INFO).getText());
		Assert.assertEquals("c", TextElement.create('c', TextElementTest.INFO).getText());
		Assert.assertEquals("1.5", TextElement.create(1.5F, TextElementTest.INFO).getText());
		Assert.assertEquals("2.25", TextElement.create(2.25D, TextElementTest.INFO).getText());
		Assert.assertEquals("true", TextElement.create(true, TextElementTest.INFO).getText());
	}

	@Test
	public void replacesItsTextWithEveryPrimitive() {
		final TextElement element = TextElement.create("text", TextElementTest.INFO);
		Assert.assertEquals("12", element.text(12).getText());
		Assert.assertEquals("34", element.text(34L).getText());
		Assert.assertEquals("c", element.text('c').getText());
		Assert.assertEquals("1.5", element.text(1.5F).getText());
		Assert.assertEquals("2.25", element.text(2.25D).getText());
		Assert.assertEquals("false", element.text(false).getText());
	}

	@Test
	public void readsItsObjectOnEveryCall() {
		final StringBuilder builder = new StringBuilder("a");
		final TextElement created = TextElement.create(builder, TextElementTest.INFO);
		final TextElement replaced = TextElement.create("text", TextElementTest.INFO).text(builder);
		builder.append("b");
		Assert.assertEquals("ab", created.getText());
		Assert.assertEquals("ab", replaced.getText());
	}

	@Test
	public void readsItsSupplierOnEveryCall() {
		final AtomicInteger counter = new AtomicInteger();
		final TextElement created = TextElement.create(() -> counter.incrementAndGet(), TextElementTest.INFO);
		Assert.assertEquals("1", created.getText());
		Assert.assertEquals("2", created.getText());
		final TextElement replaced = TextElement.create("text", TextElementTest.INFO).text(() -> counter.incrementAndGet());
		Assert.assertEquals("3", replaced.getText());
	}

	@Test
	public void modifiesItsTextButNotItsRawText() {
		final TextElement element = TextElement.create("Hello", TextElementTest.INFO);
		Assert.assertNull(element.getModifier());
		Assert.assertEquals("Hello", element.getText());
		element.modifier(TextModifier.UPPER_CASE);
		Assert.assertSame(TextModifier.UPPER_CASE, element.getModifier());
		Assert.assertEquals("HELLO", element.getText());
		Assert.assertEquals("Hello", element.getRawText());
	}

	@Test
	public void changesItsInfo() {
		final TextInfo info = TextInfo.create(() -> null, 20F);
		final TextElement element = TextElement.create("text", TextElementTest.INFO);
		Assert.assertSame(TextElementTest.INFO, element.getInfo());
		Assert.assertSame(info, element.info(info).getInfo());
	}

	@Test
	public void copiesItsTextInfoAndModifier() {
		final TextElement element = TextElement.create("text", TextElementTest.INFO).modifier(TextModifier.UPPER_CASE);
		final TextElement copy = element.copy();
		Assert.assertEquals("TEXT", copy.getText());
		Assert.assertSame(TextElementTest.INFO, copy.getInfo());
		Assert.assertSame(TextModifier.UPPER_CASE, copy.getModifier());
	}

	@Test
	public void leavesTheOriginalUntouchedByItsCopy() {
		final TextElement element = TextElement.create("text", TextElementTest.INFO);
		element.copy().text("other").info(TextInfo.create(() -> null, 20F)).modifier(TextModifier.UPPER_CASE);
		Assert.assertEquals("text", element.getText());
		Assert.assertSame(TextElementTest.INFO, element.getInfo());
		Assert.assertNull(element.getModifier());
	}

	@Test
	public void copiesWithAnotherText() {
		final TextElement element = TextElement.create("text", TextElementTest.INFO).modifier(TextModifier.UPPER_CASE);
		final TextElement object = element.copyWithText("other");
		final TextElement supplier = element.copyWithText(() -> "supplied");
		Assert.assertEquals("OTHER", object.getText());
		Assert.assertSame(TextElementTest.INFO, object.getInfo());
		Assert.assertEquals("SUPPLIED", supplier.getText());
		Assert.assertSame(TextElementTest.INFO, supplier.getInfo());
		Assert.assertEquals("TEXT", element.getText());
	}

	@Test
	public void copiesWithAnotherInfo() {
		final TextInfo info = TextInfo.create(() -> null, 20F);
		final TextElement element = TextElement.create("text", TextElementTest.INFO).modifier(TextModifier.UPPER_CASE);
		final TextElement copy = element.copyWithInfo(info);
		Assert.assertSame(info, copy.getInfo());
		Assert.assertEquals("TEXT", copy.getText());
		Assert.assertSame(TextElementTest.INFO, element.getInfo());
	}

	@Test
	public void copiesWithAnotherModifier() {
		final TextElement element = TextElement.create("Text", TextElementTest.INFO).modifier(TextModifier.UPPER_CASE);
		final TextElement copy = element.copyWithModifier(TextModifier.LOWER_CASE);
		Assert.assertEquals("text", copy.getText());
		Assert.assertSame(TextElementTest.INFO, copy.getInfo());
		Assert.assertEquals("TEXT", element.getText());
		Assert.assertNull(element.copyWithModifier(null).getModifier());
	}

	@Test
	public void readsATypedSupplier() {
		final Supplier<String> supplier = () -> "ab";
		Assert.assertEquals("ab", TextElement.create(supplier, TextElementTest.INFO).getText());
		Assert.assertEquals("ab", TextElement.create("cd", TextElementTest.INFO).text(supplier).getText());
		Assert.assertEquals("ab", TextElement.create("cd", TextElementTest.INFO).copyWithText(supplier).getText());
	}

}