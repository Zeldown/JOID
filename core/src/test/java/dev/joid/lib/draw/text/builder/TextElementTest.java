package dev.joid.lib.draw.text.builder;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.internal.JOID;
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

}