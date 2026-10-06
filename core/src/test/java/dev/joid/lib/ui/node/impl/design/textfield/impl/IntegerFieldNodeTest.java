package dev.joid.lib.ui.node.impl.design.textfield.impl;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.font.IFont;
import dev.joid.lib.font.IFontProvider;
import dev.joid.lib.font.dto.FontBounds;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.key.Key;

public class IntegerFieldNodeTest {

	private static final IFont FONT = () -> IntegerFieldNodeTest.PROVIDER;

	private static final IFontProvider PROVIDER = new IFontProvider() {

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

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void keepsOnlyTheDigitsOfAText() {
		final IntegerFieldNode field = IntegerFieldNode.create(0D, 0D, 100D).text("1a2 b3");
		Assert.assertEquals("123", field.getText());
		Assert.assertEquals(123, field.getValue());
	}

	@Test
	public void writesAValue() {
		final IntegerFieldNode field = IntegerFieldNode.create(0D, 0D, 100D).value(42);
		Assert.assertEquals("42", field.getText());
		Assert.assertEquals(42, field.getValue());
	}

	@Test
	public void lowersAValueAboveTheMaximum() {
		Assert.assertEquals(100, IntegerFieldNode.create(0D, 0D, 100D).max(100).value(250).getValue());
	}

	@Test
	public void raisesAValueBelowTheMinimum() {
		Assert.assertEquals(10, IntegerFieldNode.create(0D, 0D, 100D).min(10).value(5).getValue());
	}

	@Test
	public void keepsAValueInsideItsRange() {
		final IntegerFieldNode field = IntegerFieldNode.create(0D, 0D, 100D).range(1, 9);
		Assert.assertEquals(5, field.value(5).getValue());
		Assert.assertEquals(9, field.value(12).getValue());
		Assert.assertEquals(1, field.value(0).getValue());
	}

	@Test
	public void fallsBackToTheMinimumOnceEmptied() {
		final IntegerFieldNode field = IntegerFieldNode.create(0D, 0D, 100D).range(3, 9).value(5).text("");
		Assert.assertEquals("3", field.getText());
	}

	@Test
	public void fallsBackToTheMinimumWithoutDigits() {
		Assert.assertEquals("3", IntegerFieldNode.create(0D, 0D, 100D).range(3, 9).text("abc").getText());
	}

	@Test
	public void lowersANumberTooLongForAnInteger() {
		Assert.assertEquals(50, IntegerFieldNode.create(0D, 0D, 100D).range(0, 50).<IntegerFieldNode>text("99999999999").getValue());
		Assert.assertEquals(Integer.MAX_VALUE, IntegerFieldNode.create(0D, 0D, 100D).<IntegerFieldNode>text("99999999999").getValue());
	}

	@Test
	public void sizesItselfFromItsTextUnlessGivenAHeight() {
		Assert.assertEquals(0D, IntegerFieldNode.create(10D, 20D, 100D).getHeight(), 0D);
		Assert.assertEquals(30D, IntegerFieldNode.create(10D, 20D, 100D, 30D).getHeight(), 0D);
		Assert.assertEquals(100D, IntegerFieldNode.create(10D, 20D, 100D, 30D).getWidth(), 0D);
	}

	@Test
	public void filtersWhatTheUserTypes() {
		final IntegerFieldNode field = IntegerFieldNode.create(0D, 0D, 100D).range(0, 500).value(4).info(TextInfo.create(IntegerFieldNodeTest.FONT, 20F)).focused(true).cursorPosition(1);
		field.keyPressed('2', Key.DIGIT_2, InternalContext.create());
		field.keyPressed('x', Key.X, InternalContext.create());
		Assert.assertEquals("42", field.getText());
		field.keyPressed('9', Key.DIGIT_9, InternalContext.create());
		Assert.assertEquals(429, field.getValue());
		field.keyPressed('9', Key.DIGIT_9, InternalContext.create());
		Assert.assertEquals(500, field.getValue());
	}

	@Test
	public void keepsANegativeValueInsideItsRange() {
		Assert.assertEquals(-5, IntegerFieldNode.create(0D, 0D, 100D).range(-10, 10).value(-5).getValue());
	}

}