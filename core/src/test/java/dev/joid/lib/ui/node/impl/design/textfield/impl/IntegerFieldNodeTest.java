package dev.joid.lib.ui.node.impl.design.textfield.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.font.FontBounds;
import dev.joid.lib.font.IFont;
import dev.joid.lib.font.ITextRenderer;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Phase;
import dev.joid.lib.ui.node.impl.design.textfield.TextFieldNode;
import dev.joid.lib.ui.node.impl.design.textfield.callback.NodeTextFieldChangeCallback;

import lombok.NonNull;

public class IntegerFieldNodeTest {

	private static final IFont FONT = () -> IntegerFieldNodeTest.RENDERER;

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

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void acceptsOnlyAnIntegerWhileTyping() {
		final IntegerFieldNode field = this.focused(IntegerFieldNode.create(0D, 0D, 100D).min(-10).max(10));
		this.type(field, "1a2 b");
		Assert.assertEquals("12", field.getText());
		field.cursorPosition(0);
		this.type(field, "-");
		Assert.assertEquals("-12", field.getText());
		this.type(field, "-");
		Assert.assertEquals("-12", field.getText());
	}

	@Test
	public void acceptsALoneMinusOnlyWhileNegativeValuesAreAllowed() {
		final IntegerFieldNode negative = this.focused(IntegerFieldNode.create(0D, 0D, 100D).min(-4).max(9));
		this.type(negative, "-");
		Assert.assertEquals("-", negative.getText());
		Assert.assertEquals(0, (int) negative.getValue());
		Assert.assertFalse(negative.isValid());
		final IntegerFieldNode positive = this.focused(IntegerFieldNode.create(0D, 200D, 100D).min(0).max(9));
		this.type(positive, "-");
		Assert.assertEquals("", positive.getText());
	}

	@Test
	public void neverMovesItsCursorOnARefusedKey() {
		final IntegerFieldNode field = this.focused(IntegerFieldNode.create(0D, 0D, 100D).value(42)).cursorPosition(1);
		this.type(field, "x");
		Assert.assertEquals("42", field.getText());
		Assert.assertEquals(1, field.getCursorPos());
	}

	@Test
	public void appliesNoBoundWhileTyping() {
		final IntegerFieldNode field = this.focused(IntegerFieldNode.create(0D, 0D, 100D).min(0).max(100).value(42)).cursorPosition(2);
		this.type(field, "5");
		Assert.assertEquals("425", field.getText());
		Assert.assertEquals(100, (int) field.getValue());
		Assert.assertFalse(field.isValid());
		this.press(field, Key.ENTER);
		Assert.assertEquals("100", field.getText());
		Assert.assertTrue(field.isValid());
	}

	@Test
	public void bringsAValueBelowItsMinimumBackToItOnCommit() {
		final IntegerFieldNode field = this.focused(IntegerFieldNode.create(0D, 0D, 100D).min(10).max(100).value(42));
		this.control(field, Key.A);
		this.type(field, "5");
		Assert.assertEquals("5", field.getText());
		Assert.assertEquals(10, (int) field.getValue());
		field.focused(false);
		Assert.assertEquals("10", field.getText());
	}

	@Test
	public void fallsBackToItsLastValueOnAnEmptyOrInvalidText() {
		final IntegerFieldNode field = this.focused(IntegerFieldNode.create(0D, 0D, 100D).min(-10).max(10).value(7));
		this.control(field, Key.A);
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("", field.getText());
		Assert.assertEquals(7, (int) field.getValue());
		Assert.assertFalse(field.isValid());
		this.type(field, "-");
		Assert.assertEquals(7, (int) field.getValue());
		field.focused(false);
		Assert.assertEquals("7", field.getText());
		Assert.assertEquals("7", field.<IntegerFieldNode>text("abc").getText());
	}

	@Test
	public void startsFromZeroBroughtInsideItsRangeNeverItsMiddle() {
		Assert.assertEquals(0, (int) IntegerFieldNode.create(0D, 0D, 100D).getValue());
		Assert.assertEquals(3, (int) IntegerFieldNode.create(0D, 0D, 100D).min(3).max(9).getValue());
		Assert.assertEquals(-2, (int) IntegerFieldNode.create(0D, 0D, 100D).min(-9).max(-2).getValue());
		Assert.assertEquals("", IntegerFieldNode.create(0D, 0D, 100D).min(3).max(9).getText());
	}

	@Test
	public void keepsAnEmptyTextWithoutValueWhenAllowed() {
		final IntegerSignal signal = new IntegerSignal(4);
		final IntegerFieldNode field = this.focused(IntegerFieldNode.create(0D, 0D, 100D).allowEmpty(true).signal(signal));
		this.control(field, Key.A);
		this.press(field, Key.BACKSPACE);
		Assert.assertNull(field.getValue());
		Assert.assertTrue(field.isValid());
		field.focused(false);
		Assert.assertEquals("", field.getText());
		Assert.assertNull(field.getValue());
		Assert.assertEquals(4, (int) signal.get());
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
		final IntegerFieldNode field = IntegerFieldNode.create(0D, 0D, 100D).min(1).max(9);
		Assert.assertEquals(5, (int) field.value(5).getValue());
		Assert.assertEquals(9, (int) field.value(12).getValue());
		Assert.assertEquals(1, (int) field.value(0).getValue());
		Assert.assertEquals("1", field.getText());
	}

	@Test
	public void keepsANegativeValueInsideItsRange() {
		Assert.assertEquals(-5, (int) IntegerFieldNode.create(0D, 0D, 100D).min(-10).max(10).value(-5).getValue());
	}

	@Test
	public void clampsItsValueOnceItsBoundsChange() {
		final IntegerFieldNode field = IntegerFieldNode.create(0D, 0D, 100D).value(50);
		Assert.assertEquals(20, (int) field.max(20).getValue());
		Assert.assertEquals("20", field.getText());
		Assert.assertEquals(25, (int) field.min(25).getValue());
		Assert.assertEquals(3, (int) field.min(0).max(3).getValue());
		Assert.assertEquals("3", field.<IntegerFieldNode>text("").getText());
	}

	@Test
	public void lowersANumberTooLongForAnInteger() {
		Assert.assertEquals(50, (int) IntegerFieldNode.create(0D, 0D, 100D).min(0).max(50).<IntegerFieldNode>text("99999999999").getValue());
		Assert.assertEquals(Integer.MAX_VALUE, (int) IntegerFieldNode.create(0D, 0D, 100D).<IntegerFieldNode>text("99999999999").getValue());
		Assert.assertEquals(Integer.MIN_VALUE, (int) IntegerFieldNode.create(0D, 0D, 100D).<IntegerFieldNode>text("-99999999999").getValue());
	}

	@Test
	public void reportsEveryKeystrokeWithTheValueItsCommitWouldApply() {
		final List<Object> changes = new ArrayList<>();
		final IntegerFieldNode field = this.focused(IntegerFieldNode.create(0D, 0D, 100D).min(0).max(50).value(4).onChange((node, text, value, valid) -> changes.addAll(Arrays.asList(text, value, valid)))).cursorPosition(1);
		this.type(field, "2");
		this.type(field, "9");
		this.press(field, Key.BACKSPACE);
		this.press(field, Key.BACKSPACE);
		this.press(field, Key.BACKSPACE);
		field.focused(false);
		Assert.assertEquals(Arrays.asList("42", 42, true, "429", 50, false, "42", 42, true, "4", 4, true, "", 4, false, "4", 4, true), changes);
	}

	@Test
	public void reportsNoFinalChangeWhenItsCommitKeepsItsText() {
		final List<String> changes = new ArrayList<>();
		final IntegerFieldNode field = this.focused(IntegerFieldNode.create(0D, 0D, 100D).value(4).onChange((node, text, value, valid) -> changes.add(text))).cursorPosition(1);
		this.type(field, "2");
		this.press(field, Key.ENTER);
		Assert.assertEquals(Collections.singletonList("42"), changes);
	}

	@Test
	public void refusesAKeystrokeThePrePhaseConsumes() {
		final IntegerFieldNode field = this.focused(IntegerFieldNode.create(0D, 0D, 100D).value(4).onChange(new NodeTextFieldChangeCallback<IntegerFieldNode, Integer>() {

			@Override
			public void apply(final @NonNull IntegerFieldNode node, final @NonNull String text, final Integer value, final boolean valid) {}

			@Override
			@NodeCallbackMethod(Phase.PRE)
			public void pre(final @NonNull IntegerFieldNode node, final @NonNull DispatchContext context, final @NonNull String text, final Integer value, final boolean valid) {
				if (value != null && value > 10) {
					context.cancel();
				}
			}

		})).cursorPosition(1);
		this.type(field, "2");
		Assert.assertEquals("4", field.getText());
		Assert.assertEquals(1, field.getCursorPos());
	}

	@Test
	public void bindsItsValueToASignalBothWays() {
		final List<String> changes = new ArrayList<>();
		final IntegerSignal signal = new IntegerSignal(7);
		final IntegerFieldNode field = IntegerFieldNode.create(0D, 0D, 100D).min(0).max(10).info(TextInfo.create(IntegerFieldNodeTest.FONT, 20F)).<IntegerFieldNode>onChange((node, text, value, valid) -> changes.add(text)).signal(signal);
		this.bridges.open(new NodeUI(field));
		Assert.assertEquals(7, (int) field.getValue());
		signal.set(42);
		Assert.assertEquals("10", field.getText());
		Assert.assertEquals(10, (int) signal.get());
		field.value(3);
		Assert.assertEquals(3, (int) signal.get());
		Assert.assertEquals(Arrays.asList("7", "10", "3"), changes);
	}

	@Test
	public void writesTheCorrectedValueToItsSignalOnEveryKeystrokeWithoutRewritingItsText() {
		final IntegerSignal signal = new IntegerSignal(4);
		final IntegerFieldNode field = this.focused(IntegerFieldNode.create(0D, 0D, 100D).min(0).max(100).signal(signal)).cursorPosition(1);
		this.type(field, "2");
		Assert.assertEquals(42, (int) signal.get());
		this.type(field, "5");
		Assert.assertEquals(100, (int) signal.get());
		Assert.assertEquals("425", field.getText());
		this.press(field, Key.BACKSPACE);
		this.press(field, Key.BACKSPACE);
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("", field.getText());
		Assert.assertEquals(4, (int) signal.get());
	}

	@Test
	public void showsAChangeOfItsSignalMadeElsewhereEvenWhileFocused() {
		final IntegerSignal signal = new IntegerSignal(4);
		final IntegerFieldNode field = this.focused(IntegerFieldNode.create(0D, 0D, 100D).signal(signal)).cursorPosition(1);
		this.type(field, "2");
		signal.set(9);
		Assert.assertEquals("9", field.getText());
		Assert.assertTrue(field.isFocused());
	}

	@Test
	public void bindsItsValueToAnyIntegerSignal() {
		final Signal<Integer> signal = new Signal<>(4);
		final IntegerFieldNode field = IntegerFieldNode.create(0D, 0D, 100D).min(0).max(10).info(TextInfo.create(IntegerFieldNodeTest.FONT, 20F)).signal(signal);
		this.bridges.open(new NodeUI(field));
		Assert.assertEquals("4", field.getText());
		Assert.assertSame(signal, field.getSignal());
		signal.set(12);
		Assert.assertEquals(10, (int) field.getValue());
		Assert.assertEquals(10, (int) signal.get());
		field.value(6);
		Assert.assertEquals(6, (int) signal.get());
	}

	@Test
	public void stepsItsValueWithTheArrowsInsideItsBounds() {
		final IntegerSignal signal = new IntegerSignal(5);
		final IntegerFieldNode field = this.focused(IntegerFieldNode.create(0D, 0D, 100D).min(0).max(8).step(2).signal(signal));
		this.press(field, Key.UP);
		Assert.assertEquals("7", field.getText());
		Assert.assertEquals(7, (int) signal.get());
		this.press(field, Key.UP);
		Assert.assertEquals("8", field.getText());
		this.press(field, Key.DOWN);
		this.press(field, Key.DOWN);
		Assert.assertEquals("4", field.getText());
		Assert.assertTrue(field.isFocused());
	}

	@Test
	public void stepsByOneByDefault() {
		final IntegerFieldNode field = this.focused(IntegerFieldNode.create(0D, 0D, 100D).value(5));
		this.press(field, Key.DOWN);
		Assert.assertEquals("4", field.getText());
	}

	@Test
	public void stepsFromItsCorrectedValueWhileATypedTextIsOutOfBounds() {
		final IntegerFieldNode field = this.focused(IntegerFieldNode.create(0D, 0D, 100D).min(0).max(100).value(42)).cursorPosition(2);
		this.type(field, "5");
		this.press(field, Key.DOWN);
		Assert.assertEquals("99", field.getText());
	}

	@Test
	public void stepsItsValueWithTheWheelOnlyWhenHovered() {
		final IntegerFieldNode field = IntegerFieldNode.create(100D, 100D, 100D, 30D).min(0).max(10).value(5).info(TextInfo.create(IntegerFieldNodeTest.FONT, 20F));
		this.bridges.open(new NodeUI(field));
		this.bridges.move(500D, 500D).frames(2);
		this.bridges.scroll(1D);
		Assert.assertEquals("5", field.getText());
		this.bridges.move(150D, 110D).frames(2);
		this.bridges.scroll(1D);
		Assert.assertEquals("6", field.getText());
		this.bridges.scroll(-1D);
		this.bridges.scroll(-1D);
		Assert.assertEquals("4", field.getText());
		Assert.assertFalse(field.isFocused());
	}

	@Test
	public void keepsItsCursorAtItsDistanceFromTheEndAfterAStepUp() {
		final IntegerFieldNode field = this.focused(IntegerFieldNode.create(0D, 0D, 100D).value(9)).cursorPosition(1);
		this.press(field, Key.UP);
		Assert.assertEquals("10", field.getText());
		Assert.assertEquals(2, field.getCursorPos());
		field.cursorPosition(0);
		this.press(field, Key.DOWN);
		Assert.assertEquals("9", field.getText());
		Assert.assertEquals(0, field.getCursorPos());
		this.press(field, Key.UP);
		Assert.assertEquals(1, field.getCursorPos());
	}

	@Test
	public void keepsItsCursorAtItsDistanceFromTheEndAfterAStepDown() {
		final IntegerFieldNode field = this.focused(IntegerFieldNode.create(0D, 0D, 100D).value(100)).cursorPosition(2);
		this.press(field, Key.DOWN);
		Assert.assertEquals("99", field.getText());
		Assert.assertEquals(1, field.getCursorPos());
		field.cursorPosition(2);
		this.press(field, Key.UP);
		Assert.assertEquals("100", field.getText());
		Assert.assertEquals(3, field.getCursorPos());
	}

	@Test
	public void keepsItsCursorAtItsDistanceFromTheEndAfterAWheelStep() {
		final IntegerFieldNode field = IntegerFieldNode.create(100D, 100D, 100D, 30D).value(99).info(TextInfo.create(IntegerFieldNodeTest.FONT, 20F));
		this.bridges.open(new NodeUI(field));
		field.focused(true).cursorPosition(1);
		this.bridges.move(150D, 110D).frames(2);
		this.bridges.scroll(1D);
		Assert.assertEquals("100", field.getText());
		Assert.assertEquals(2, field.getCursorPos());
	}

	@Test
	public void keepsItsCursorAtItsDistanceFromTheEndWhenItsSignalRewritesIt() {
		final IntegerSignal signal = new IntegerSignal(5);
		final IntegerFieldNode field = this.focused(IntegerFieldNode.create(0D, 0D, 100D).signal(signal)).cursorPosition(1);
		signal.set(1000);
		Assert.assertEquals("1000", field.getText());
		Assert.assertEquals(4, field.getCursorPos());
		field.cursorPosition(0);
		signal.set(7);
		Assert.assertEquals("7", field.getText());
		Assert.assertEquals(0, field.getCursorPos());
	}

	@Test
	public void keepsItsCursorAtItsDistanceFromTheEndWhenItsCommitReformatsIt() {
		final IntegerFieldNode field = this.focused(IntegerFieldNode.create(0D, 0D, 100D).value(5)).cursorPosition(0);
		this.type(field, "00");
		Assert.assertEquals("005", field.getText());
		Assert.assertEquals(2, field.getCursorPos());
		this.press(field, Key.UP);
		Assert.assertEquals("6", field.getText());
		Assert.assertEquals(0, field.getCursorPos());
	}

	@Test
	public void commitsAPasteAtOnce() {
		final List<String> changes = new ArrayList<>();
		final IntegerFieldNode field = this.focused(IntegerFieldNode.create(0D, 0D, 100D).min(0).max(100).value(4).onChange((node, text, value, valid) -> changes.add(text))).cursorPosition(1);
		this.bridges.getWindow().setClipboard("25");
		this.control(field, Key.V);
		Assert.assertEquals("100", field.getText());
		Assert.assertEquals(Arrays.asList("425", "100"), changes);
		this.bridges.getWindow().setClipboard("x1");
		this.control(field, Key.V);
		Assert.assertEquals("100", field.getText());
	}

	@Test
	public void keepsItsFocusAndItsCursorOnTab() {
		final IntegerFieldNode first = IntegerFieldNode.create(0D, 0D, 100D, 30D).max(10).value(3).info(TextInfo.create(IntegerFieldNodeTest.FONT, 20F));
		final TextFieldNode second = TextFieldNode.create(0D, 100D, 100D, 30D).text("hello").info(TextInfo.create(IntegerFieldNodeTest.FONT, 20F));
		this.bridges.open(new NodeUI(first, second));
		first.focused(true).cursorPosition(1);
		first.keyPressed('\t', Key.TAB, DispatchContext.create());
		Assert.assertTrue(first.isFocused());
		Assert.assertFalse(second.isFocused());
		Assert.assertEquals("3", first.getText());
		Assert.assertEquals(-1, first.getSelectionStart());
		Assert.assertEquals(1, first.getCursorPos());
	}

	@Test
	public void placesItsCursorWhereClicked() {
		final IntegerFieldNode field = IntegerFieldNode.create(100D, 100D, 200D, 30D).value(12345).info(TextInfo.create(IntegerFieldNodeTest.FONT, 20F));
		this.bridges.open(new NodeUI(field));
		this.bridges.move(124D, 110D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertTrue(field.isFocused());
		Assert.assertEquals(2, field.getCursorPos());
		Assert.assertEquals(-1, field.getSelectionStart());
	}

	@Test
	public void restoresItsValueFromBeforeItsFocusOnEscape() {
		final IntegerSignal signal = new IntegerSignal(4);
		final IntegerFieldNode field = this.focused(IntegerFieldNode.create(0D, 0D, 100D).signal(signal)).cursorPosition(1);
		this.type(field, "21");
		Assert.assertEquals(421, (int) signal.get());
		this.press(field, Key.ESCAPE);
		Assert.assertEquals("4", field.getText());
		Assert.assertEquals(4, (int) signal.get());
		Assert.assertFalse(field.isFocused());
	}

	@Test
	public void sizesItselfFromItsTextUnlessGivenAHeight() {
		Assert.assertEquals(0D, IntegerFieldNode.create(10D, 20D, 100D).getHeight(), 0D);
		Assert.assertEquals(30D, IntegerFieldNode.create(10D, 20D, 100D, 30D).getHeight(), 0D);
		Assert.assertEquals(100D, IntegerFieldNode.create(10D, 20D, 100D, 30D).getWidth(), 0D);
	}

	private IntegerFieldNode focused(final IntegerFieldNode field) {
		this.bridges.open(new NodeUI(field.info(TextInfo.create(IntegerFieldNodeTest.FONT, 20F))));
		return field.focused(true);
	}

	private void press(final IntegerFieldNode field, final Key key) {
		field.keyPressed(' ', key, DispatchContext.create());
	}

	private void control(final IntegerFieldNode field, final Key key) {
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.press(field, key);
		this.bridges.getWindow().getKeys().remove(Key.LEFT_CONTROL);
	}

	private void type(final IntegerFieldNode field, final String text) {
		for (final char c : text.toCharArray()) {
			field.keyPressed(c, Key.UNKNOWN, DispatchContext.create());
		}
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