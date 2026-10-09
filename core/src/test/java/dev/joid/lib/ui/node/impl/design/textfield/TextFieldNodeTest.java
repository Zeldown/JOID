package dev.joid.lib.ui.node.impl.design.textfield;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.IFont;
import dev.joid.lib.font.IFontProvider;
import dev.joid.lib.font.dto.FontBounds;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.font.dto.markup.ITextMarkup;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.key.Key;
import dev.joid.lib.utils.signal.Signal;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class TextFieldNodeTest {

	private static final Color INK = new Color(0.2F, 0.4F, 0.6F, 1F);

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private final FieldFont font = new FieldFont();

	@Test
	public void startsEmptyAndUnfocused() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D);
		Assert.assertEquals("", field.getText());
		Assert.assertEquals("", field.getPlaceholder());
		Assert.assertFalse(field.isFocused());
		Assert.assertEquals(-1, field.getMaxTextLength());
		Assert.assertEquals(0, field.getCursorPos());
		Assert.assertEquals(-1, field.getSelectionStart());
		Assert.assertTrue(field.getAccept().test("abc"));
		Assert.assertFalse(field.isAllowEmpty());
		Assert.assertSame(Align.START, field.getHorizontalAlignment());
		Assert.assertSame(Align.CENTER, field.getVerticalAlignment());
	}

	@Test
	public void startsWithTheDocumentedMargins() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D);
		Assert.assertEquals(2D, field.getMarginLeft(), 0D);
		Assert.assertEquals(2D, field.getMarginRight(), 0D);
		Assert.assertEquals(10D, field.getMarginTop(), 0D);
		Assert.assertEquals(10D, field.getMarginBottom(), 0D);
		Assert.assertEquals(15D, field.getCursorMargin(), 0D);
	}

	@Test
	public void sizesItsHeightFromItsTextOnTheFirstDraw() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info());
		final TextFieldNode thin = TextFieldNode.create(100D, 300D, 200D).info(this.info()).marginVertical(4D);
		Assert.assertEquals(0D, field.getHeight(), 0D);
		this.bridges.open(new NodeUI(field, thin));
		Assert.assertEquals(40D, field.getHeight(), 0D);
		Assert.assertEquals(28D, thin.getHeight(), 0D);
		Assert.assertEquals(200D, field.getWidth(), 0D);
	}

	@Test
	public void keepsTheHeightItWasGiven() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D, 30D).info(this.info());
		this.bridges.open(new NodeUI(field));
		Assert.assertEquals(30D, field.getHeight(), 0D);
	}

	@Test
	public void setsEveryMarginAtOnce() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D);
		Assert.assertSame(field, field.margin(3D));
		Assert.assertEquals(3D, field.getMarginLeft(), 0D);
		Assert.assertEquals(3D, field.getMarginRight(), 0D);
		Assert.assertEquals(3D, field.getMarginTop(), 0D);
		Assert.assertEquals(3D, field.getMarginBottom(), 0D);
		Assert.assertEquals(15D, field.getCursorMargin(), 0D);
		Assert.assertSame(field, field.margin(4D).cursorMargin(6D));
		Assert.assertEquals(4D, field.getMarginLeft(), 0D);
		Assert.assertEquals(4D, field.getMarginRight(), 0D);
		Assert.assertEquals(4D, field.getMarginTop(), 0D);
		Assert.assertEquals(4D, field.getMarginBottom(), 0D);
		Assert.assertEquals(6D, field.getCursorMargin(), 0D);
	}

	@Test
	public void setsEachMarginOnItsOwn() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).marginTop(1D).marginLeft(2D).marginRight(3D).marginBottom(4D).cursorMargin(5D);
		Assert.assertEquals(1D, field.getMarginTop(), 0D);
		Assert.assertEquals(2D, field.getMarginLeft(), 0D);
		Assert.assertEquals(3D, field.getMarginRight(), 0D);
		Assert.assertEquals(4D, field.getMarginBottom(), 0D);
		Assert.assertEquals(5D, field.getCursorMargin(), 0D);
		Assert.assertSame(field, field.marginVertical(7D));
		Assert.assertEquals(7D, field.getMarginTop(), 0D);
		Assert.assertEquals(7D, field.getMarginBottom(), 0D);
		Assert.assertSame(field, field.marginHorizontal(8D));
		Assert.assertEquals(8D, field.getMarginLeft(), 0D);
		Assert.assertEquals(8D, field.getMarginRight(), 0D);
	}

	@Test
	public void takesItsAlignment() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D);
		Assert.assertSame(field, field.horizontalAlign(Align.END).verticalAlign(Align.START));
		Assert.assertSame(Align.END, field.getHorizontalAlignment());
		Assert.assertSame(Align.START, field.getVerticalAlignment());
		Assert.assertSame(field, field.horizontalAlign(Align.CENTER));
		Assert.assertSame(field, field.verticalAlign(Align.END));
		Assert.assertSame(Align.CENTER, field.getHorizontalAlignment());
		Assert.assertSame(Align.END, field.getVerticalAlignment());
	}

	@Test
	public void takesItsPlaceholderAndItsInfo() {
		final TextInfo info = this.info();
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D);
		Assert.assertSame(field, field.placeholder("Search"));
		Assert.assertSame(field, field.info(info));
		Assert.assertEquals("Search", field.getPlaceholder());
		Assert.assertSame(info, field.getInfo());
	}

	@Test
	public void keepsTheCursorPositionInsideTheText() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).text("abc");
		Assert.assertEquals(2, field.cursorPosition(2).getCursorPos());
		Assert.assertEquals(0, field.cursorPosition(-3).getCursorPos());
		Assert.assertEquals(3, field.cursorPosition(9).getCursorPos());
	}

	@Test
	public void reportsEachChangeOfItsText() {
		final List<Object> changes = new ArrayList<>();
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).onChange((node, text, value, valid) -> changes.addAll(Arrays.asList(node, text, value, valid)));
		field.text("ab").text("ab");
		Assert.assertEquals(Arrays.asList(field, "ab", "ab", true), changes);
		Assert.assertEquals("ab", field.getText());
	}

	@Test
	public void typesItsCallbacksByTheTypeInferredFromTheContext() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).onChange((node, text, value, valid) -> node.horizontalAlign(Align.END));
		final MultilineTextFieldNode notes = MultilineTextFieldNode.create(100D, 100D, 200D, 100D).placeholder("Notes").onFocus(node -> node.text("focused"));
		field.text("ab");
		notes.focused(true);
		Assert.assertSame(Align.END, field.getHorizontalAlignment());
		Assert.assertEquals("focused", notes.getText());
	}

	@Test
	public void formatsAGivenTextAtOnce() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).format(String::toUpperCase);
		Assert.assertEquals("AB", field.text("ab").getText());
		Assert.assertEquals("CD", field.text("cd").getText());
	}

	@Test
	public void formatsWhatIsTypedOnlyOnCommit() {
		final List<Object> changes = new ArrayList<>();
		final TextFieldNode field = this.field("ab").<TextFieldNode>format(String::toUpperCase).<TextFieldNode>onChange((node, text, value, valid) -> changes.addAll(Arrays.asList(text, value, valid))).cursorPosition(2);
		this.type(field, "c");
		Assert.assertEquals("abc", field.getText());
		Assert.assertEquals("ABC", field.getValue());
		this.press(field, Key.ENTER);
		Assert.assertEquals("ABC", field.getText());
		Assert.assertEquals(Arrays.asList("abc", "ABC", true, "ABC", "ABC", true), changes);
	}

	@Test
	public void refusesWhatItsAcceptRefusesWithoutMovingItsCursor() {
		final TextFieldNode field = this.field("ab").accept(text -> !text.contains("x")).cursorPosition(1);
		this.type(field, "x");
		Assert.assertEquals("ab", field.getText());
		Assert.assertEquals(1, field.getCursorPos());
		this.type(field, "c");
		Assert.assertEquals("acb", field.getText());
		Assert.assertEquals(2, field.getCursorPos());
	}

	@Test
	public void reportsATextItsAcceptRefusesAsInvalid() {
		final List<Boolean> valid = new ArrayList<>();
		final TextFieldNode field = this.field("ab").<TextFieldNode>accept(text -> text.length() <= 3).onChange((node, text, value, accepted) -> valid.add(accepted));
		field.text("abcd");
		Assert.assertEquals("abcd", field.getText());
		Assert.assertFalse(field.isValid());
		Assert.assertEquals(Collections.singletonList(false), valid);
	}

	@Test
	public void cutsATextLongerThanItsMaximumLength() {
		Assert.assertEquals("abc", TextFieldNode.create(100D, 100D, 200D).maxTextLength(3).text("abcdef").getText());
		Assert.assertEquals("", TextFieldNode.create(100D, 100D, 200D).maxTextLength(0).text("abcdef").getText());
		Assert.assertEquals("abcdef", TextFieldNode.create(100D, 100D, 200D).maxTextLength(-1).text("abcdef").getText());
	}

	@Test
	public void stopsAPasteAtItsMaximumLength() {
		final TextFieldNode field = this.field("ab").maxTextLength(4).cursorPosition(2);
		this.bridges.getWindow().setClipboard("xyz");
		this.control(field, Key.V);
		Assert.assertEquals("abxy", field.getText());
		Assert.assertEquals(4, field.getCursorPos());
	}

	@Test
	public void reportsOnlyARealChangeOfFocus() {
		final List<Boolean> focuses = new ArrayList<>();
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).onFocus(node -> focuses.add(node.isFocused()));
		field.focused(true).focused(true).focused(false);
		Assert.assertEquals(Arrays.asList(true, false), focuses);
	}

	@Test
	public void losesItsFocusWhenDetached() {
		final List<Boolean> focuses = new ArrayList<>();
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).onFocus(node -> focuses.add(node.isFocused()));
		this.bridges.open(new NodeUI(field));
		field.focused(true);
		field.onDetach();
		Assert.assertFalse(field.isFocused());
		Assert.assertFalse(field.isInputting());
		Assert.assertEquals(Arrays.asList(true, false), focuses);
	}

	@Test
	public void focusesOnAClickAndPutsTheCursorUnderTheMouse() {
		final List<Boolean> focuses = new ArrayList<>();
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).text("hello").onFocus(node -> focuses.add(node.isFocused()));
		this.bridges.open(new NodeUI(field));
		this.click(124D, 120D);
		Assert.assertTrue(field.isFocused());
		Assert.assertEquals(2, field.getCursorPos());
		Assert.assertEquals(Collections.singletonList(true), focuses);
	}

	@Test
	public void putsTheCursorAtTheEndOnAClickPastTheText() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).text("hello");
		this.bridges.open(new NodeUI(field));
		this.click(250D, 120D);
		Assert.assertEquals(5, field.getCursorPos());
	}

	@Test
	public void focusesAnEmptyFieldOnAClick() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info());
		this.bridges.open(new NodeUI(field));
		this.click(250D, 120D);
		Assert.assertTrue(field.isFocused());
		Assert.assertEquals(0, field.getCursorPos());
	}

	@Test
	public void putsTheCursorUnderAClickOnAnAlignedText() {
		final TextFieldNode centered = TextFieldNode.create(100D, 100D, 200D).info(this.info()).<TextFieldNode>text("abcd").horizontalAlign(Align.CENTER);
		final TextFieldNode end = TextFieldNode.create(100D, 200D, 200D).info(this.info()).<TextFieldNode>text("abcd").horizontalAlign(Align.END);
		this.bridges.open(new NodeUI(centered, end));
		this.click(196D, 120D);
		Assert.assertEquals(2, centered.getCursorPos());
		this.click(270D, 220D);
		Assert.assertEquals(1, end.getCursorPos());
	}

	@Test
	public void unfocusesAndDropsItsSelectionOnAClickBesideIt() {
		final TextFieldNode field = this.field("hello");
		this.control(field, Key.A);
		this.click(500D, 500D);
		Assert.assertFalse(field.isFocused());
		Assert.assertEquals(-1, field.getSelectionStart());
	}

	@Test
	public void unfocusesWhenTheClickWasAlreadyTaken() {
		final TextFieldNode field = this.field("hello");
		field.mousePressed(124D, 120D, ClickType.LEFT, InternalContext.create(true));
		Assert.assertFalse(field.isFocused());
	}

	@Test
	public void extendsItsSelectionOnAShiftClick() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).text("hello");
		this.bridges.open(new NodeUI(field));
		this.click(114D, 120D);
		Assert.assertEquals(1, field.getCursorPos());
		this.bridges.getWindow().getKeys().add(Key.LEFT_SHIFT);
		this.click(144D, 120D);
		Assert.assertEquals(1, field.getSelectionStart());
		Assert.assertEquals(4, field.getCursorPos());
		this.click(124D, 120D);
		Assert.assertEquals(1, field.getSelectionStart());
		Assert.assertEquals(2, field.getCursorPos());
		this.bridges.getWindow().getKeys().remove(Key.LEFT_SHIFT);
		this.bridges.getClock().advance(600L);
		this.click(124D, 120D);
		Assert.assertEquals(-1, field.getSelectionStart());
	}

	@Test
	public void ignoresTheKeyboardWhileUnfocused() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).text("abc");
		this.bridges.open(new NodeUI(field));
		final InternalContext context = InternalContext.create();
		field.keyPressed('x', Key.X, context);
		Assert.assertEquals("abc", field.getText());
		Assert.assertFalse(context.isCancelled());
	}

	@Test
	public void leavesAKeyAlreadyTakenElsewhere() {
		final TextFieldNode field = this.field("abc");
		field.keyPressed('x', Key.X, InternalContext.create(true));
		Assert.assertEquals("abc", field.getText());
	}

	@Test
	public void consumesTheKeysItReceives() {
		final InternalContext context = InternalContext.create();
		this.field("abc").keyPressed('x', Key.X, context);
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void receivesTheKeysTypedInItsUi() {
		final TextFieldNode field = this.field("ab").cursorPosition(2);
		this.bridges.getUi().keyTyped('c', Key.C);
		Assert.assertEquals("abc", field.getText());
	}

	@Test
	public void insertsWhatIsTypedAtTheCursor() {
		final TextFieldNode field = this.field("hllo").cursorPosition(1);
		this.type(field, "e");
		Assert.assertEquals("hello", field.getText());
		Assert.assertEquals(2, field.getCursorPos());
	}

	@Test
	public void dropsTheCharactersItCannotShow() {
		final List<String> changes = new ArrayList<>();
		final TextFieldNode field = this.field("ab").onChange((node, text, value, valid) -> changes.add(text));
		this.type(field, "\u0007\u001b\u007f€");
		Assert.assertEquals("ab", field.getText());
		Assert.assertTrue(changes.isEmpty());
		this.type(field, "é");
		Assert.assertEquals("éab", field.getText());
	}

	@Test
	public void replacesTheSelectionWithWhatIsTyped() {
		final TextFieldNode field = this.field("hello");
		this.control(field, Key.A);
		this.type(field, "x");
		Assert.assertEquals("x", field.getText());
		Assert.assertEquals(1, field.getCursorPos());
		Assert.assertEquals(-1, field.getSelectionStart());
	}

	@Test
	public void replacesABackwardSelectionWithWhatIsTyped() {
		final TextFieldNode field = this.field("hello").cursorPosition(5);
		this.bridges.getWindow().getKeys().add(Key.LEFT_SHIFT);
		for (int i = 0; i < 4; i++) {
			this.press(field, Key.LEFT);
		}
		this.bridges.getWindow().getKeys().remove(Key.LEFT_SHIFT);
		this.type(field, "x");
		Assert.assertEquals("hx", field.getText());
		Assert.assertEquals(2, field.getCursorPos());
	}

	@Test
	public void movesItsCursorWithTheArrows() {
		final TextFieldNode field = this.field("abc").cursorPosition(1);
		this.press(field, Key.RIGHT);
		Assert.assertEquals(2, field.getCursorPos());
		this.press(field, Key.RIGHT);
		this.press(field, Key.RIGHT);
		Assert.assertEquals(3, field.getCursorPos());
		this.press(field, Key.LEFT);
		Assert.assertEquals(2, field.getCursorPos());
		for (int i = 0; i < 3; i++) {
			this.press(field, Key.LEFT);
		}
		Assert.assertEquals(0, field.getCursorPos());
	}

	@Test
	public void movesTheCursorOfAnAlignedText() {
		final TextFieldNode centered = TextFieldNode.create(100D, 100D, 200D).info(this.info()).<TextFieldNode>text("abcd").horizontalAlign(Align.CENTER).focused(true).cursorPosition(2);
		final TextFieldNode end = TextFieldNode.create(100D, 200D, 200D).info(this.info()).<TextFieldNode>text("abcd").horizontalAlign(Align.END).focused(true).cursorPosition(2);
		this.bridges.open(new NodeUI(centered, end));
		this.press(centered, Key.LEFT);
		this.press(centered, Key.RIGHT);
		this.press(centered, Key.RIGHT);
		this.press(end, Key.LEFT);
		this.press(end, Key.RIGHT);
		this.press(end, Key.RIGHT);
		Assert.assertEquals(3, centered.getCursorPos());
		Assert.assertEquals(3, end.getCursorPos());
	}

	@Test
	public void jumpsOverWholeWordsWithControl() {
		final TextFieldNode field = this.field("one two  three");
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.press(field, Key.RIGHT);
		Assert.assertEquals(4, field.getCursorPos());
		this.press(field, Key.RIGHT);
		Assert.assertEquals(9, field.getCursorPos());
		this.press(field, Key.RIGHT);
		Assert.assertEquals(14, field.getCursorPos());
		this.press(field, Key.LEFT);
		Assert.assertEquals(9, field.getCursorPos());
		this.press(field, Key.LEFT);
		Assert.assertEquals(4, field.getCursorPos());
		this.press(field, Key.LEFT);
		Assert.assertEquals(0, field.getCursorPos());
	}

	@Test
	public void selectsWithShiftAndTheArrows() {
		final TextFieldNode field = this.field("abcdef").cursorPosition(1);
		this.bridges.getWindow().getKeys().add(Key.LEFT_SHIFT);
		for (int i = 0; i < 3; i++) {
			this.press(field, Key.RIGHT);
		}
		Assert.assertEquals(1, field.getSelectionStart());
		Assert.assertEquals(4, field.getCursorPos());
		this.bridges.getWindow().getKeys().remove(Key.LEFT_SHIFT);
		this.bridges.getWindow().getKeys().add(Key.RIGHT_SHIFT);
		this.press(field, Key.LEFT);
		Assert.assertEquals(1, field.getSelectionStart());
		Assert.assertEquals(3, field.getCursorPos());
		this.bridges.getWindow().getKeys().remove(Key.RIGHT_SHIFT);
		this.press(field, Key.RIGHT);
		Assert.assertEquals(-1, field.getSelectionStart());
		Assert.assertEquals(4, field.getCursorPos());
	}

	@Test
	public void dropsItsSelectionOnALeftArrowWithoutShift() {
		final TextFieldNode field = this.field("abcdef").cursorPosition(1);
		this.bridges.getWindow().getKeys().add(Key.RIGHT_SHIFT);
		this.press(field, Key.RIGHT);
		Assert.assertEquals(1, field.getSelectionStart());
		this.bridges.getWindow().getKeys().remove(Key.RIGHT_SHIFT);
		this.press(field, Key.LEFT);
		Assert.assertEquals(-1, field.getSelectionStart());
		Assert.assertEquals(1, field.getCursorPos());
	}

	@Test
	public void selectsWholeWordsWithShiftAndControl() {
		final TextFieldNode field = this.field("one two");
		this.bridges.getWindow().getKeys().addAll(Arrays.asList(Key.LEFT_SHIFT, Key.LEFT_CONTROL));
		this.press(field, Key.RIGHT);
		Assert.assertEquals(0, field.getSelectionStart());
		Assert.assertEquals(4, field.getCursorPos());
	}

	@Test
	public void reachesBothEndsWithHomeAndEnd() {
		final TextFieldNode field = this.field("abc").cursorPosition(1);
		this.press(field, Key.END);
		Assert.assertEquals(3, field.getCursorPos());
		this.press(field, Key.HOME);
		Assert.assertEquals(0, field.getCursorPos());
	}

	@Test
	public void erasesTheCharacterBeforeTheCursorWithBackspace() {
		final TextFieldNode field = this.field("abc").cursorPosition(2);
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("ac", field.getText());
		Assert.assertEquals(1, field.getCursorPos());
		this.press(field, Key.HOME);
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("ac", field.getText());
		Assert.assertEquals(0, field.getCursorPos());
	}

	@Test
	public void erasesTheWordBeforeTheCursorWithControlBackspace() {
		final TextFieldNode field = this.field("one two").cursorPosition(7);
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("one ", field.getText());
		Assert.assertEquals(4, field.getCursorPos());
	}

	@Test
	public void erasesTheSelectionWithBackspace() {
		final TextFieldNode forward = this.selected(1, 4);
		this.press(forward, Key.BACKSPACE);
		Assert.assertEquals("aef", forward.getText());
		Assert.assertEquals(1, forward.getCursorPos());
		Assert.assertEquals(-1, forward.getSelectionStart());
		final TextFieldNode backward = this.selected(4, 1);
		this.press(backward, Key.BACKSPACE);
		Assert.assertEquals("aef", backward.getText());
		Assert.assertEquals(1, backward.getCursorPos());
	}

	@Test
	public void erasesTheCharacterAfterTheCursorWithDelete() {
		final TextFieldNode field = this.field("abc").cursorPosition(1);
		this.press(field, Key.DELETE);
		Assert.assertEquals("ac", field.getText());
		Assert.assertEquals(1, field.getCursorPos());
		this.press(field, Key.END);
		this.press(field, Key.DELETE);
		Assert.assertEquals("ac", field.getText());
	}

	@Test
	public void erasesTheWordAfterTheCursorWithControlDelete() {
		final TextFieldNode field = this.field("one two");
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.press(field, Key.DELETE);
		Assert.assertEquals("two", field.getText());
		Assert.assertEquals(0, field.getCursorPos());
	}

	@Test
	public void erasesTheSelectionWithDelete() {
		final TextFieldNode forward = this.selected(1, 4);
		this.press(forward, Key.DELETE);
		Assert.assertEquals("aef", forward.getText());
		Assert.assertEquals(1, forward.getCursorPos());
		final TextFieldNode backward = this.selected(4, 1);
		this.press(backward, Key.DELETE);
		Assert.assertEquals("aef", backward.getText());
		Assert.assertEquals(1, backward.getCursorPos());
		Assert.assertEquals(-1, backward.getSelectionStart());
	}

	@Test
	public void selectsEverythingWithControlA() {
		final TextFieldNode field = this.field("hello").cursorPosition(2);
		this.control(field, Key.A);
		Assert.assertEquals(0, field.getSelectionStart());
		Assert.assertEquals(5, field.getCursorPos());
	}

	@Test
	public void copiesTheSelectionWithControlC() {
		final TextFieldNode forward = this.selected(1, 4);
		this.control(forward, Key.C);
		Assert.assertEquals("bcd", this.bridges.getWindow().getClipboard());
		Assert.assertEquals("abcdef", forward.getText());
		final TextFieldNode backward = this.selected(5, 2);
		this.control(backward, Key.C);
		Assert.assertEquals("cde", this.bridges.getWindow().getClipboard());
		Assert.assertEquals(5, backward.getSelectionStart());
	}

	@Test
	public void keepsTheClipboardWithoutASelection() {
		final TextFieldNode field = this.field("abcdef");
		this.bridges.getWindow().setClipboard("clip");
		this.control(field, Key.C);
		this.control(field, Key.X);
		Assert.assertEquals("clip", this.bridges.getWindow().getClipboard());
		Assert.assertEquals("abcdef", field.getText());
	}

	@Test
	public void cutsTheSelectionWithControlX() {
		final TextFieldNode forward = this.selected(1, 4);
		this.control(forward, Key.X);
		Assert.assertEquals("bcd", this.bridges.getWindow().getClipboard());
		Assert.assertEquals("aef", forward.getText());
		Assert.assertEquals(1, forward.getCursorPos());
		Assert.assertEquals(-1, forward.getSelectionStart());
		final TextFieldNode backward = this.selected(5, 2);
		this.control(backward, Key.X);
		Assert.assertEquals("cde", this.bridges.getWindow().getClipboard());
		Assert.assertEquals("abf", backward.getText());
		Assert.assertEquals(2, backward.getCursorPos());
		Assert.assertEquals(-1, backward.getSelectionStart());
	}

	@Test
	public void pastesTheClipboardAtTheCursor() {
		final TextFieldNode field = this.field("abc").cursorPosition(1);
		this.bridges.getWindow().setClipboard("xy");
		this.control(field, Key.V);
		Assert.assertEquals("axybc", field.getText());
		Assert.assertEquals(3, field.getCursorPos());
	}

	@Test
	public void pastesTheClipboardOnOneLine() {
		final TextFieldNode field = this.field("");
		this.bridges.getWindow().setClipboard("x\ny\tz\r");
		this.control(field, Key.V);
		Assert.assertEquals("xyz", field.getText());
	}

	@Test
	public void replacesTheSelectionWithThePastedText() {
		final TextFieldNode field = this.selected(1, 4);
		this.bridges.getWindow().setClipboard("xy");
		this.control(field, Key.V);
		Assert.assertEquals("axyef", field.getText());
		Assert.assertEquals(3, field.getCursorPos());
	}

	@Test
	public void ignoresAnEmptyClipboard() {
		final List<String> changes = new ArrayList<>();
		final TextFieldNode field = this.field("abc").onChange((node, text, value, valid) -> changes.add(text));
		this.control(field, Key.V);
		Assert.assertEquals("abc", field.getText());
		Assert.assertTrue(changes.isEmpty());
	}

	@Test
	public void unfocusesOnEnterAndEscapeButReportsOnlyEnter() {
		final List<String> entered = new ArrayList<>();
		final TextFieldNode field = this.field("hello").onEnter((node, text) -> entered.add(text));
		for (final Key key : Arrays.asList(Key.ENTER, Key.NUMPAD_ENTER, Key.ESCAPE)) {
			field.focused(true);
			this.press(field, key);
			Assert.assertFalse(field.isFocused());
		}
		Assert.assertEquals(Arrays.asList("hello", "hello"), entered);
	}

	@Test
	public void restoresTheTextItHadBeforeItsFocusOnEscape() {
		final Signal<String> signal = new Signal<>("hello");
		final TextFieldNode field = this.field("").signal(signal).cursorPosition(5);
		field.focused(false).focused(true);
		this.type(field, " world");
		Assert.assertEquals("hello world", signal.get());
		this.press(field, Key.ESCAPE);
		Assert.assertEquals("hello", field.getText());
		Assert.assertEquals("hello", signal.get());
		Assert.assertFalse(field.isFocused());
	}

	@Test
	public void unfocusesOnEscapeInsideAUiThatStaysOpen() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).text("hello").focused(true);
		final PinnedUI ui = new PinnedUI(field);
		this.bridges.open(ui);
		this.bridges.getUi().keyTyped('\u001b', Key.ESCAPE);
		Assert.assertFalse(field.isFocused());
		Assert.assertTrue(this.bridges.getUi().isOpen(ui));
	}

	@Test
	public void cancelsItsEditOnEscapeBeforeItsUiCloses() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).text("hello").focused(true);
		final NodeUI ui = new NodeUI(field);
		this.bridges.open(ui);
		this.type(field, "!");
		this.bridges.getUi().keyTyped('\u001b', Key.ESCAPE);
		Assert.assertFalse(field.isFocused());
		Assert.assertEquals("hello", field.getText());
		Assert.assertTrue(this.bridges.getUi().isOpen(ui));
		this.bridges.getUi().keyTyped('\u001b', Key.ESCAPE);
		Assert.assertFalse(this.bridges.getUi().isOpen(ui));
	}

	@Test
	public void repeatsAHeldArrowAfterHalfASecond() {
		final TextFieldNode field = this.field("abcdef");
		this.bridges.getWindow().getKeys().add(Key.RIGHT);
		this.press(field, Key.RIGHT);
		Assert.assertTrue(field.isInputting());
		Assert.assertSame(Key.RIGHT, field.getInputType());
		this.bridges.getClock().advance(480L);
		this.bridges.frame();
		Assert.assertEquals(1, field.getCursorPos());
		this.bridges.frame();
		Assert.assertEquals(2, field.getCursorPos());
		this.bridges.getClock().advance(68L);
		this.bridges.frame();
		Assert.assertEquals(2, field.getCursorPos());
		this.bridges.frame();
		Assert.assertEquals(3, field.getCursorPos());
	}

	@Test
	public void repeatsAHeldLeftArrow() {
		final TextFieldNode field = this.field("abcdef").cursorPosition(6);
		this.bridges.getWindow().getKeys().add(Key.LEFT);
		this.press(field, Key.LEFT);
		this.elapse(500L);
		Assert.assertEquals(4, field.getCursorPos());
		this.elapse(100L);
		Assert.assertEquals(3, field.getCursorPos());
	}

	@Test
	public void stopsRepeatingOnceTheKeyIsReleased() {
		final TextFieldNode field = this.field("abcdef");
		this.press(field, Key.RIGHT);
		this.elapse(600L);
		Assert.assertFalse(field.isInputting());
		Assert.assertEquals(1, field.getCursorPos());
	}

	@Test
	public void repeatsAHeldBackspaceUntilTheTextIsEmpty() {
		final TextFieldNode field = this.field("abc").cursorPosition(3);
		this.bridges.getWindow().getKeys().add(Key.BACKSPACE);
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("ab", field.getText());
		this.elapse(500L);
		Assert.assertEquals("a", field.getText());
		this.elapse(100L);
		Assert.assertEquals("", field.getText());
		this.elapse(100L);
		Assert.assertFalse(field.isInputting());
		Assert.assertEquals(0, field.getCursorPos());
	}

	@Test
	public void repeatsAHeldDeleteUntilTheTextIsEmpty() {
		final TextFieldNode field = this.field("abc");
		this.bridges.getWindow().getKeys().add(Key.DELETE);
		this.press(field, Key.DELETE);
		Assert.assertEquals("bc", field.getText());
		this.elapse(500L);
		Assert.assertEquals("c", field.getText());
		this.elapse(100L);
		Assert.assertEquals("", field.getText());
		this.elapse(100L);
		Assert.assertFalse(field.isInputting());
	}

	@Test
	public void repeatsWholeWordsWhileControlIsHeld() {
		final TextFieldNode field = this.field("one two three").cursorPosition(13);
		this.bridges.getWindow().getKeys().addAll(Arrays.asList(Key.BACKSPACE, Key.LEFT_CONTROL));
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("one two ", field.getText());
		this.elapse(500L);
		Assert.assertEquals("one ", field.getText());
		Assert.assertEquals(4, field.getCursorPos());
		this.bridges.getWindow().getKeys().remove(Key.BACKSPACE);
		this.elapse(100L);
		field.text("one two three").cursorPosition(0);
		this.bridges.getWindow().getKeys().add(Key.DELETE);
		this.press(field, Key.DELETE);
		this.elapse(500L);
		Assert.assertEquals("three", field.getText());
	}

	@Test
	public void repeatsWholeWordMovesWhileControlIsHeld() {
		final TextFieldNode field = this.field("one two three");
		this.bridges.getWindow().getKeys().addAll(Arrays.asList(Key.RIGHT, Key.LEFT_CONTROL));
		this.press(field, Key.RIGHT);
		this.elapse(500L);
		Assert.assertEquals(8, field.getCursorPos());
		this.bridges.getWindow().getKeys().remove(Key.RIGHT);
		this.elapse(100L);
		this.bridges.getWindow().getKeys().add(Key.LEFT);
		this.press(field, Key.LEFT);
		this.elapse(500L);
		Assert.assertEquals(0, field.getCursorPos());
	}

	@Test
	public void drawsItsTextInsideItsMargins() {
		this.bridges.open(new NodeUI(TextFieldNode.create(100D, 100D, 200D).info(this.info()).text("abc")));
		Assert.assertEquals("abc", this.font.last().getText());
		Assert.assertEquals(102D, this.font.last().getX(), 0D);
		Assert.assertEquals(110D, this.font.last().getY(), 0D);
		Assert.assertEquals(1F, this.font.last().getAlpha(), 0F);
		final Draw mask = this.bridges.getRender().getDraws(1F, 0F, 0F).get(0);
		Assert.assertEquals(102D, mask.getLeft(), 0.001D);
		Assert.assertEquals(298D, mask.getRight(), 0.001D);
		Assert.assertEquals(100D, mask.getTop(), 0.001D);
		Assert.assertEquals(140D, mask.getBottom(), 0.001D);
	}

	@Test
	public void drawsItsPlaceholderAtHalfOpacityWhileEmpty() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).placeholder("Search");
		this.bridges.open(new NodeUI(field));
		Assert.assertEquals("Search", this.font.last().getText());
		Assert.assertEquals(0.5F, this.font.last().getAlpha(), 0F);
		field.focused(true);
		this.bridges.frame();
		Assert.assertEquals("", this.font.last().getText());
		Assert.assertEquals(1F, this.font.last().getAlpha(), 0F);
	}

	@Test
	public void centersItsTextHorizontally() {
		this.bridges.open(new NodeUI(TextFieldNode.create(100D, 100D, 200D, 60D).info(this.info()).<TextFieldNode>text("abcd").horizontalAlign(Align.CENTER)));
		Assert.assertEquals(180D, this.font.last().getX(), 0D);
		Assert.assertEquals(120D, this.font.last().getY(), 0D);
	}

	@Test
	public void alignsItsTextToTheEnd() {
		this.bridges.open(new NodeUI(TextFieldNode.create(100D, 100D, 200D, 60D).info(this.info()).<TextFieldNode>text("abcd").horizontalAlign(Align.END).verticalAlign(Align.END)));
		Assert.assertEquals(256D, this.font.last().getX(), 0D);
		Assert.assertEquals(130D, this.font.last().getY(), 0D);
	}

	@Test
	public void alignsItsTextToTheTop() {
		this.bridges.open(new NodeUI(TextFieldNode.create(100D, 100D, 200D, 60D).info(this.info()).<TextFieldNode>text("abcd").verticalAlign(Align.START)));
		Assert.assertEquals(102D, this.font.last().getX(), 0D);
		Assert.assertEquals(110D, this.font.last().getY(), 0D);
	}

	@Test
	public void alignsItsPlaceholderOnItsOwnWidth() {
		this.bridges.open(new NodeUI(TextFieldNode.create(100D, 100D, 200D).info(this.info()).<TextFieldNode>placeholder("Search").horizontalAlign(Align.END)));
		Assert.assertEquals(236D, this.font.last().getX(), 0D);
	}

	@Test
	public void drawsTheCursorAfterTheTextBeforeIt() {
		this.field("abcd").cursorPosition(2);
		this.bridges.frame();
		final Draw cursor = this.cursor();
		Assert.assertEquals(122D, cursor.getLeft(), 0.001D);
		Assert.assertEquals(124D, cursor.getRight(), 0.001D);
		Assert.assertEquals(110D, cursor.getTop(), 0.001D);
		Assert.assertEquals(130D, cursor.getBottom(), 0.001D);
	}

	@Test
	public void pulsesTheOpacityOfItsCursor() {
		this.field("abcd");
		this.bridges.getClock().setTime(234L);
		this.bridges.frame();
		Assert.assertEquals(1F, this.cursor().getAlpha(), 0.0001F);
		this.bridges.getClock().setTime(984L);
		this.bridges.frame();
		Assert.assertEquals(0.5F, this.cursor().getAlpha(), 0.0001F);
	}

	@Test
	public void hidesItsCursorWhileUnfocused() {
		this.bridges.open(new NodeUI(TextFieldNode.create(100D, 100D, 200D).info(this.info()).text("abcd")));
		Assert.assertTrue(this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F).isEmpty());
	}

	@Test
	public void highlightsItsSelectionInEitherDirection() {
		final TextFieldNode field = this.selected(1, 4);
		this.bridges.frame();
		Assert.assertEquals(1, this.selections().size());
		Assert.assertEquals(112D, this.selections().get(0).getLeft(), 0.001D);
		Assert.assertEquals(142D, this.selections().get(0).getRight(), 0.001D);
		Assert.assertEquals(110D, this.selections().get(0).getTop(), 0.001D);
		Assert.assertEquals(130D, this.selections().get(0).getBottom(), 0.001D);
		this.press(field, Key.RIGHT);
		this.bridges.getWindow().getKeys().add(Key.LEFT_SHIFT);
		for (int i = 0; i < 3; i++) {
			this.press(field, Key.LEFT);
		}
		this.bridges.frame();
		Assert.assertEquals(122D, this.selections().get(0).getLeft(), 0.001D);
		Assert.assertEquals(152D, this.selections().get(0).getRight(), 0.001D);
	}

	@Test
	public void scrollsToKeepItsCursorInView() {
		final TextFieldNode field = this.field("abcdefghijklmnopqrst");
		this.press(field, Key.END);
		Assert.assertEquals(19D, field.getXOffset(), 0D);
		this.bridges.frame();
		Assert.assertEquals(83D, this.font.last().getX(), 0D);
		Assert.assertEquals(283D, this.cursor().getLeft(), 0.001D);
		this.press(field, Key.HOME);
		this.bridges.frame();
		Assert.assertEquals(0D, field.getXOffset(), 0D);
		Assert.assertEquals(102D, this.font.last().getX(), 0D);
	}

	@Test
	public void scrollsWhileTypingPastItsWidth() {
		final TextFieldNode field = this.field("");
		this.type(field, "abcdefghijklmnopqrst");
		Assert.assertEquals(19D, field.getXOffset(), 0D);
		Assert.assertEquals(20, field.getCursorPos());
	}

	@Test
	public void scrollsBackOnceItsCursorReachesItsStartMargin() {
		final TextFieldNode field = this.field("abcdefghijklmnopqrst");
		this.press(field, Key.END);
		for (int i = 0; i < 17; i++) {
			this.press(field, Key.LEFT);
		}
		Assert.assertEquals(15D, field.getXOffset(), 0D);
		this.bridges.frame();
		Assert.assertEquals(117D, this.cursor().getLeft(), 0.001D);
		this.press(field, Key.LEFT);
		this.press(field, Key.LEFT);
		this.bridges.frame();
		Assert.assertEquals(0D, field.getXOffset(), 0D);
		Assert.assertEquals(1, field.getCursorPos());
	}

	@Test
	public void scrollsBackOnceItsTextShrinks() {
		final TextFieldNode field = this.field("abcdefghijklmnopqrst");
		this.press(field, Key.END);
		field.cursorPosition(1).text("a");
		this.bridges.frame();
		Assert.assertEquals(0D, field.getXOffset(), 0D);
		Assert.assertEquals(102D, this.font.last().getX(), 0D);
	}

	@Test
	public void keepsItsCursorAndSelectionAtTheirDistanceFromTheEndOfARewrittenText() {
		final TextFieldNode field = this.selected(6, 5);
		field.text("ab");
		this.bridges.frame();
		Assert.assertEquals(1, field.getCursorPos());
		Assert.assertEquals(2, field.getSelectionStart());
	}

	@Test
	public void keepsItsSelectionWhenItsAcceptRefusesTheDeletion() {
		final TextFieldNode field = this.selected(2, 3).accept(text -> text.length() > 5);
		this.press(field, Key.BACKSPACE);
		this.bridges.frame();
		Assert.assertEquals("abcdef", field.getText());
		Assert.assertEquals(3, field.getCursorPos());
		Assert.assertEquals(2, field.getSelectionStart());
	}

	@Test
	public void keepsDrawingOnceAScrolledTextIsCleared() {
		final TextFieldNode field = this.field("abcdefghijklmnopqrst").<TextFieldNode>placeholder("Message").onEnter((node, text) -> node.text(""));
		this.press(field, Key.END);
		this.bridges.frame();
		this.press(field, Key.ENTER);
		this.bridges.frame();
		Assert.assertEquals("Message", this.font.last().getText());
		Assert.assertEquals(0D, field.getXOffset(), 0D);
	}

	@Test
	public void putsTheCursorUnderAClickInsideAParent() {
		final RectNode wrapper = RectNode.create(40D, 40D, 400D, 40D);
		final TextFieldNode field = TextFieldNode.create(8D, 0D, 384D).info(this.info()).text("hello").attach(wrapper);
		this.bridges.open(new NodeUI(wrapper));
		this.bridges.move(74D, 60D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertTrue(field.isFocused());
		Assert.assertEquals(2, field.getCursorPos());
	}

	@Test
	public void selectsEverythingWithTheRightControlKey() {
		final TextFieldNode field = this.field("hello").cursorPosition(2);
		this.bridges.getWindow().getKeys().add(Key.RIGHT_CONTROL);
		field.keyPressed('a', Key.A, InternalContext.create());
		Assert.assertEquals("hello", field.getText());
		Assert.assertEquals(0, field.getSelectionStart());
		Assert.assertEquals(5, field.getCursorPos());
	}

	@Test
	public void erasesTheCharacterBeforeACollapsedSelection() {
		final TextFieldNode field = this.field("hello").cursorPosition(2);
		this.bridges.getWindow().getKeys().add(Key.LEFT_SHIFT);
		this.press(field, Key.RIGHT);
		this.press(field, Key.LEFT);
		this.bridges.getWindow().getKeys().remove(Key.LEFT_SHIFT);
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("hllo", field.getText());
		Assert.assertEquals(1, field.getCursorPos());
	}

	@Test
	public void reportsASingleChangeWhenTypingOverASelection() {
		final List<String> changes = new ArrayList<>();
		final TextFieldNode field = this.field("ab").<TextFieldNode>accept(text -> !text.isEmpty()).onChange((node, text, value, valid) -> changes.add(text));
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.press(field, Key.A);
		this.bridges.getWindow().getKeys().remove(Key.LEFT_CONTROL);
		field.keyPressed('c', Key.C, InternalContext.create());
		Assert.assertEquals("c", field.getText());
		Assert.assertEquals(Collections.singletonList("c"), changes);
	}

	@Test
	public void refusesWhatIsTypedOnceFull() {
		final TextFieldNode field = this.field("abc").maxTextLength(3).cursorPosition(1);
		field.keyPressed('x', Key.X, InternalContext.create());
		Assert.assertEquals("abc", field.getText());
	}

	@Test
	public void showsTheCursorAtTheEndOfAnEndAlignedText() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).<TextFieldNode>text("abcd").horizontalAlign(Align.END).focused(true).cursorPosition(4);
		this.bridges.open(new NodeUI(field));
		final Draw mask = this.bridges.getRender().getDraws(1F, 0F, 0F).get(0);
		final Draw cursor = this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F).get(0);
		Assert.assertTrue(cursor.getRight() + " > " + mask.getRight(), cursor.getRight() <= mask.getRight() + 0.001D);
	}

	@Test
	public void keepsTheCursorOfACenteredTextInView() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).<TextFieldNode>text("abcdefghijklmnopqrstuvwxyzabcd").horizontalAlign(Align.CENTER).focused(true);
		this.bridges.open(new NodeUI(field));
		this.press(field, Key.END);
		this.bridges.frame();
		final Draw mask = this.bridges.getRender().getDraws(1F, 0F, 0F).get(0);
		final Draw cursor = this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F).get(0);
		Assert.assertTrue(cursor.getRight() + " > " + mask.getRight(), cursor.getRight() <= mask.getRight() + 0.001D);
	}

	@Test
	public void extendsItsSelectionToBothEndsWithShiftHomeAndEnd() {
		final TextFieldNode field = this.field("abcdef").cursorPosition(2);
		this.bridges.getWindow().getKeys().add(Key.LEFT_SHIFT);
		this.press(field, Key.END);
		Assert.assertEquals(2, field.getSelectionStart());
		Assert.assertEquals(6, field.getCursorPos());
		this.press(field, Key.HOME);
		Assert.assertEquals(2, field.getSelectionStart());
		Assert.assertEquals(0, field.getCursorPos());
	}

	@Test
	public void dropsItsSelectionOnHomeAndEndWithoutShift() {
		final TextFieldNode field = this.selected(1, 3);
		this.press(field, Key.END);
		Assert.assertEquals(-1, field.getSelectionStart());
		Assert.assertEquals(6, field.getCursorPos());
		this.control(field, Key.A);
		this.press(field, Key.HOME);
		Assert.assertEquals(-1, field.getSelectionStart());
		Assert.assertEquals(0, field.getCursorPos());
	}

	@Test
	public void dropsItsSelectionOnceUnfocused() {
		final TextFieldNode field = this.selected(1, 3);
		field.focused(false);
		Assert.assertEquals(-1, field.getSelectionStart());
		field.focused(true);
		this.control(field, Key.A);
		this.press(field, Key.ENTER);
		Assert.assertEquals(-1, field.getSelectionStart());
	}

	@Test
	public void dropsTheCharactersItCannotShowFromEveryText() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).text("a\u00a7b\tc\u20ac\nd");
		Assert.assertEquals("abcd", field.getText());
		Assert.assertEquals("xy", field.format(text -> "x\ty\u20ac").text("z").getText());
	}

	@Test
	public void drawsItsTextWithoutMarkupUnlessAllowed() {
		final List<TextInfo> infos = new ArrayList<>();
		final IFontProvider provider = new IFontProvider() {

			@Override
			public FontBounds drawText(final double x, final double y, final String text, final TextInfo info) {
				infos.add(info);
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
				return 20D;
			}

		};
		final IFont font = () -> provider;
		final ITextMarkup markup = (text, index, style) -> 0;
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(TextInfo.create(font, 10F).markups(markup)).text("ab");
		this.bridges.open(new NodeUI(field));
		Assert.assertFalse(field.isMarkup());
		Assert.assertTrue(infos.get(infos.size() - 1).getMarkups().isEmpty());
		field.markup(true);
		this.bridges.frame();
		Assert.assertEquals(Collections.singletonList(markup), infos.get(infos.size() - 1).getMarkups());
	}

	@Test
	public void usesCommandForShortcutsAndAltForWordsOnMac() {
		final String system = System.getProperty("os.name");
		System.setProperty("os.name", "Mac OS X");
		try {
			final TextFieldNode field = this.field("one two");
			this.control(field, Key.A);
			Assert.assertEquals(-1, field.getSelectionStart());
			this.bridges.getWindow().getKeys().add(Key.LEFT_SUPER);
			this.press(field, Key.A);
			this.bridges.getWindow().getKeys().remove(Key.LEFT_SUPER);
			Assert.assertEquals(0, field.getSelectionStart());
			this.press(field, Key.END);
			this.bridges.getWindow().getKeys().add(Key.LEFT_ALT);
			this.press(field, Key.LEFT);
			this.bridges.getWindow().getKeys().remove(Key.LEFT_ALT);
			Assert.assertEquals(5, field.getCursorPos());
		} finally {
			System.setProperty("os.name", system);
		}
	}

	@Test
	public void bindsItsTextToASignalBothWays() {
		final List<String> changes = new ArrayList<>();
		final Signal<String> signal = new Signal<>("hi");
		final TextFieldNode field = this.field("").onChange((node, text, value, valid) -> changes.add(text)).signal(signal);
		Assert.assertEquals("hi", field.getText());
		this.type(field.cursorPosition(2), "!");
		Assert.assertEquals("hi!", signal.get());
		signal.set("yo");
		Assert.assertEquals("yo", field.getText());
		signal.set("a	b");
		Assert.assertEquals("ab", field.getText());
		Assert.assertEquals("ab", signal.get());
		field.text("cd");
		Assert.assertEquals("cd", signal.get());
		Assert.assertEquals(Arrays.asList("hi", "hi!", "yo", "ab", "cd"), changes);
	}

	@Test
	public void followsOnlyItsLastSignal() {
		final Signal<String> name = new Signal<>("Alex");
		final Signal<String> nickname = new Signal<>("Al");
		final TextFieldNode field = this.field("").signal(name).signal(nickname);
		Assert.assertEquals("Al", field.getText());
		field.text("Sam");
		Assert.assertEquals("Sam", nickname.get());
		Assert.assertEquals("Alex", name.get());
		name.set("Max");
		Assert.assertEquals("Sam", field.getText());
		Assert.assertSame(nickname, field.getSignal());
		Assert.assertTrue(name.getEventSet().isEmpty());
		Assert.assertEquals(1, nickname.getEventSet().size());
	}

	@Test
	public void refusesAComputedSignal() {
		final Signal<String> name = new Signal<>("Alex");
		final TextFieldNode field = this.field("").signal(name);
		try {
			field.signal(name.map(String::toUpperCase));
			Assert.fail("A ComputedSignal cannot be bound in both directions");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("TextFieldNode.signal(...) needs a writable signal: a ComputedSignal is read-only, pass it to a setter instead", expected.getMessage());
		}
		name.set("Max");
		Assert.assertEquals("Max", field.getText());
		Assert.assertSame(name, field.getSignal());
	}

	@Test
	public void readsItsTextAsItsValue() {
		final TextFieldNode field = this.field("abc");
		Assert.assertEquals("abc", field.getValue());
		Assert.assertEquals("", field.text("").getValue());
	}

	private TextInfo info() {
		return TextInfo.create(this.font, 10F, TextFieldNodeTest.INK);
	}

	private TextFieldNode field(final String text) {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).text(text).focused(true);
		this.bridges.open(new NodeUI(field));
		return field;
	}

	private TextFieldNode selected(final int start, final int cursor) {
		final TextFieldNode field = this.field("abcdef").cursorPosition(start);
		this.bridges.getWindow().getKeys().add(Key.LEFT_SHIFT);
		for (int i = start; i < cursor; i++) {
			this.press(field, Key.RIGHT);
		}
		for (int i = start; i > cursor; i--) {
			this.press(field, Key.LEFT);
		}
		this.bridges.getWindow().getKeys().remove(Key.LEFT_SHIFT);
		return field;
	}

	private void press(final TextFieldNode field, final Key key) {
		field.keyPressed(' ', key, InternalContext.create());
	}

	private void control(final TextFieldNode field, final Key key) {
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.press(field, key);
		this.bridges.getWindow().getKeys().remove(Key.LEFT_CONTROL);
	}

	private void type(final TextFieldNode field, final String text) {
		for (final char c : text.toCharArray()) {
			field.keyPressed(c, Key.UNKNOWN, InternalContext.create());
		}
	}

	private void click(final double x, final double y) {
		this.bridges.move(x, y).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
	}

	private void elapse(final long milliseconds) {
		this.bridges.getClock().advance(milliseconds - 16L);
		this.bridges.frame();
	}

	private Draw cursor() {
		final List<Draw> draws = this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F);
		Assert.assertEquals(1, draws.size());
		return draws.get(0);
	}

	private List<Draw> selections() {
		return this.bridges.getRender().getDraws(50 / 255F, 152 / 255F, 253 / 255F);
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

	@UIData(closeable = false)
	public static final class PinnedUI extends UI {

		private final Node[] nodes;

		private PinnedUI(final Node... nodes) {
			this.nodes = nodes;
		}

		@Override
		public void init() {
			super.add(this.nodes);
		}

	}

	public static final class FieldFont implements IFont, IFontProvider {

		private final List<Drawn> drawn = new ArrayList<>();

		@Override
		public IFontProvider getFontProvider() {
			return this;
		}

		@Override
		public FontBounds drawText(final double x, final double y, final String text, final TextInfo info) {
			this.drawn.add(new Drawn(text, x, y, info.getColor().a));
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
			return 20D;
		}

		public Drawn last() {
			return this.drawn.get(this.drawn.size() - 1);
		}

	}

	@Getter
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static final class Drawn {

		private final String text;
		private final double x;
		private final double y;
		private final float  alpha;

	}

}