package dev.joid.lib.ui.node.impl.design.textfield.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.font.IFont;
import dev.joid.lib.font.IFontProvider;
import dev.joid.lib.font.dto.FontBounds;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.key.Key;
import dev.joid.lib.utils.signal.Signal;
import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;

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
		Assert.assertEquals(123, (int) field.getValue());
	}

	@Test
	public void writesAValue() {
		final IntegerFieldNode field = IntegerFieldNode.create(0D, 0D, 100D).value(42);
		Assert.assertEquals("42", field.getText());
		Assert.assertEquals(42, (int) field.getValue());
	}

	@Test
	public void lowersAValueAboveTheMaximum() {
		Assert.assertEquals(100, (int) IntegerFieldNode.create(0D, 0D, 100D).max(100).value(250).getValue());
	}

	@Test
	public void raisesAValueBelowTheMinimum() {
		Assert.assertEquals(10, (int) IntegerFieldNode.create(0D, 0D, 100D).min(10).value(5).getValue());
	}

	@Test
	public void keepsAValueInsideItsRange() {
		final IntegerFieldNode field = IntegerFieldNode.create(0D, 0D, 100D).range(1, 9);
		Assert.assertEquals(5, (int) field.value(5).getValue());
		Assert.assertEquals(9, (int) field.value(12).getValue());
		Assert.assertEquals(1, (int) field.value(0).getValue());
	}

	@Test
	public void readsTheMiddleOfItsRangeOnceEmptied() {
		final IntegerFieldNode field = IntegerFieldNode.create(0D, 0D, 100D).range(3, 9).value(5).text("");
		Assert.assertEquals("", field.getText());
		Assert.assertEquals(6, (int) field.getValue());
	}

	@Test
	public void readsTheMiddleOfItsRangeWithoutDigits() {
		final IntegerFieldNode field = IntegerFieldNode.create(0D, 0D, 100D).range(3, 9).text("abc");
		Assert.assertEquals("", field.getText());
		Assert.assertEquals(6, (int) field.getValue());
		Assert.assertEquals(0, (int) IntegerFieldNode.create(0D, 0D, 100D).getValue());
		Assert.assertEquals(-3, (int) IntegerFieldNode.create(0D, 0D, 100D).range(Integer.MIN_VALUE + 2, Integer.MAX_VALUE - 7).getValue());
	}

	@Test
	public void keepsALoneMinusOnlyWhileNegativeValuesAreAllowed() {
		final IntegerFieldNode field = IntegerFieldNode.create(0D, 0D, 100D).range(-4, 9).text("-");
		Assert.assertEquals("-", field.getText());
		Assert.assertEquals(2, (int) field.getValue());
		Assert.assertEquals("", IntegerFieldNode.create(0D, 0D, 100D).range(0, 9).text("-").getText());
	}

	@Test
	public void putsAnEmptiedFieldBackToTheMiddleOnceUnfocused() {
		final IntegerFieldNode field = IntegerFieldNode.create(0D, 0D, 100D).range(0, 10).value(4).focused(true).text("");
		Assert.assertEquals("", field.getText());
		field.focused(false);
		Assert.assertEquals("5", field.getText());
		Assert.assertEquals("7", field.value(7).focused(true).focused(false).getText());
	}

	@Test
	public void clampsItsValueOnceItsBoundsChange() {
		final IntegerFieldNode field = IntegerFieldNode.create(0D, 0D, 100D).value(50);
		Assert.assertEquals(20, (int) field.max(20).getValue());
		Assert.assertEquals(25, (int) field.min(25).getValue());
		Assert.assertEquals(3, (int) field.range(0, 3).getValue());
		Assert.assertEquals("", field.<IntegerFieldNode>text("").min(1).getText());
	}

	@Test
	public void bindsItsValueToASignalBothWays() {
		final List<String> changes = new ArrayList<>();
		final IntegerSignal signal = new IntegerSignal(7);
		final IntegerFieldNode field = IntegerFieldNode.create(0D, 0D, 100D).range(0, 10).info(TextInfo.create(IntegerFieldNodeTest.FONT, 20F)).<IntegerFieldNode>onChange((node, oldText, newText) -> changes.add(newText)).signal(signal);
		this.bridges.open(new NodeUI(field));
		Assert.assertEquals(7, (int) field.getValue());
		signal.set(42);
		Assert.assertEquals("10", field.getText());
		Assert.assertEquals(10, (int) signal.getOrDefault());
		field.value(3);
		Assert.assertEquals(3, (int) signal.getOrDefault());
		field.focused(true).text("");
		Assert.assertEquals(5, (int) signal.getOrDefault());
		Assert.assertEquals("", field.getText());
		field.keyPressed('8', Key.DIGIT_8, InternalContext.create());
		Assert.assertEquals(8, (int) signal.getOrDefault());
		Assert.assertEquals(Arrays.asList("7", "10", "3", "", "8"), changes);
	}

	@Test
	public void bindsItsValueToAnyIntegerSignal() {
		final Signal<Integer> signal = new Signal<>(4);
		final IntegerFieldNode field = IntegerFieldNode.create(0D, 0D, 100D).range(0, 10).info(TextInfo.create(IntegerFieldNodeTest.FONT, 20F)).signal(signal);
		this.bridges.open(new NodeUI(field));
		Assert.assertEquals("4", field.getText());
		Assert.assertSame(signal, field.getSignal());
		signal.set(12);
		Assert.assertEquals(10, (int) field.getValue());
		Assert.assertEquals(10, (int) signal.getOrDefault());
		field.value(6);
		Assert.assertEquals(6, (int) signal.getOrDefault());
	}

	@Test
	public void lowersANumberTooLongForAnInteger() {
		Assert.assertEquals(50, (int) IntegerFieldNode.create(0D, 0D, 100D).range(0, 50).<IntegerFieldNode>text("99999999999").getValue());
		Assert.assertEquals(Integer.MAX_VALUE, (int) IntegerFieldNode.create(0D, 0D, 100D).<IntegerFieldNode>text("99999999999").getValue());
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
		Assert.assertEquals(429, (int) field.getValue());
		field.keyPressed('9', Key.DIGIT_9, InternalContext.create());
		Assert.assertEquals(500, (int) field.getValue());
	}

	@Test
	public void keepsANegativeValueInsideItsRange() {
		Assert.assertEquals(-5, (int) IntegerFieldNode.create(0D, 0D, 100D).range(-10, 10).value(-5).getValue());
	}

	public static final class NodeUI extends UI {

		private final Node[] nodes;

		private NodeUI(final Node... nodes) {
			this.nodes = nodes;
		}

		@Override
		public void init() {
			super.add(this.nodes);
		}

	}

}