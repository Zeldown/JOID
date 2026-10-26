package dev.joid.lib.ui.node.impl.control.field;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.bridge.ui.UIBridgeCursorTest.OverlayUI;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.FontBounds;
import dev.joid.lib.font.IFont;
import dev.joid.lib.font.ITextRenderer;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.font.markup.ITextMarkup;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.core.NodeUI;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.core.data.UIDataDebug;
import dev.joid.lib.ui.event.impl.node.NodeCharEvent;
import dev.joid.lib.ui.event.impl.node.NodeKeyEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.property.OverflowProperty;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.geom.Point;
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
		Assert.assertEquals(0, field.getCursorPosition());
		Assert.assertEquals(-1, TextFieldNodeTest.anchor(field));
		Assert.assertTrue(field.getAccept().test("abc"));
		Assert.assertFalse(field.isAllowEmpty());
		Assert.assertSame(Align.START, field.getAlignX());
		Assert.assertSame(Align.CENTER, field.getAlignY());
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
	public void followsTheHeightOfItsTextWhenItsInfoChanges() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info());
		this.bridges.open(new NodeUI(field));
		final TextInfo large = TextInfo.create(this.font, 20F);
		field.info(large);
		this.bridges.frame();
		Assert.assertEquals(large.ah(20D), field.getHeight(), 0D);
	}

	@Test
	public void sizesItsBackgroundOnItsFirstFrame() {
		final RectNode background = RectNode.create(0D, 0D, 0D, 0D);
		this.bridges.open(new NodeUI(TextFieldNode.create(100D, 100D, 200D).info(this.info()).background(background)));
		Assert.assertEquals(200D, background.getWidth(), 0D);
		Assert.assertEquals(40D, background.getHeight(), 0D);
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
		Assert.assertSame(field, field.alignX(Align.END).alignY(Align.START));
		Assert.assertSame(Align.END, field.getAlignX());
		Assert.assertSame(Align.START, field.getAlignY());
		Assert.assertSame(field, field.alignX(Align.CENTER));
		Assert.assertSame(field, field.alignY(Align.END));
		Assert.assertSame(Align.CENTER, field.getAlignX());
		Assert.assertSame(Align.END, field.getAlignY());
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
		Assert.assertEquals(2, field.cursorPosition(2).getCursorPosition());
		Assert.assertEquals(0, field.cursorPosition(-3).getCursorPosition());
		Assert.assertEquals(3, field.cursorPosition(9).getCursorPosition());
	}

	@Test
	public void keepsACursorPositionAndASelectionGivenBeforeItsText() {
		Assert.assertEquals(3, TextFieldNode.create(100D, 100D, 200D).cursorPosition(3).text("hello").getCursorPosition());
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).cursorPosition(3).selection(1, 4).text("hello");
		Assert.assertEquals(4, field.getCursorPosition());
		Assert.assertEquals(1, field.getSelection().getStart());
		Assert.assertEquals(4, field.getSelection().getEnd());
		Assert.assertEquals(3, TextFieldNode.create(100D, 100D, 200D).focused(true).cursorPosition(3).text("hello").getCursorPosition());
		final TextFieldNode focused = TextFieldNode.create(100D, 100D, 200D).focused(true).cursorPosition(3).selection(1, 4).text("hello");
		Assert.assertEquals(4, focused.getCursorPosition());
		Assert.assertEquals(1, focused.getSelection().getStart());
		Assert.assertEquals(4, focused.getSelection().getEnd());
	}

	@Test
	public void movesItsCursorToTheEndOfItsSelectionWhateverTheOrderOfItsSetters() {
		final TextFieldNode cursorFirst = TextFieldNode.create(100D, 100D, 200D).text("hello").cursorPosition(3).selection(1, 4);
		final TextFieldNode selectionFirst = TextFieldNode.create(100D, 100D, 200D).text("hello").selection(1, 4).cursorPosition(3);
		final TextFieldNode textLast = TextFieldNode.create(100D, 100D, 200D).selection(1, 4).cursorPosition(3).text("hello");
		for (final TextFieldNode field : Arrays.asList(cursorFirst, selectionFirst, textLast)) {
			Assert.assertEquals(4, field.getCursorPosition());
			Assert.assertEquals(1, field.getSelection().getStart());
			Assert.assertEquals(4, field.getSelection().getEnd());
		}
	}

	@Test
	public void keepsTheCursorOfItsSignalOnceItsTextIsLongEnough() {
		final Signal<Integer> cursor = new Signal<>(4);
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).text("ab").cursorPosition(cursor);
		this.bridges.open(new NodeUI(field)).frame();
		Assert.assertEquals(2, field.getCursorPosition());
		Assert.assertEquals(2, cursor.peek().intValue());
		field.text("abcdef");
		this.bridges.frame();
		Assert.assertEquals(4, field.getCursorPosition());
		Assert.assertEquals(4, cursor.peek().intValue());
	}

	@Test
	public void reportsEachChangeOfItsText() {
		final List<Object> changes = new ArrayList<>();
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).onChange(event -> changes.addAll(Arrays.asList(event.getNode(), event.getText(), event.getValue(), event.isValid())));
		this.bridges.open(new NodeUI(field));
		field.text("ab").text("ab");
		Assert.assertEquals(Arrays.asList(field, "ab", "ab", true), changes);
		Assert.assertEquals("ab", field.getText());
	}

	@Test
	public void typesItsCallbacksByTheTypeInferredFromTheContext() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).onChange(event -> event.getNode().alignX(Align.END));
		final MultilineTextFieldNode notes = MultilineTextFieldNode.create(100D, 100D, 200D, 100D).info(this.info()).placeholder("Notes").onFocus(event -> event.getNode().text("focused"));
		this.bridges.open(new NodeUI(field, notes));
		field.text("ab");
		notes.focused(true);
		Assert.assertSame(Align.END, field.getAlignX());
		Assert.assertEquals("focused", notes.getText());
	}

	@Test
	public void normalizesAGivenTextAtOnce() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).normalize(String::toUpperCase);
		Assert.assertEquals("AB", field.text("ab").getText());
		Assert.assertEquals("CD", field.text("cd").getText());
	}

	@Test
	public void normalizesAGivenTextWhateverTheOrderOfItsSetters() {
		Assert.assertEquals("ab", TextFieldNode.create(100D, 100D, 200D).<TextFieldNode>text(" ab ").normalize(String::trim).getText());
	}

	@Test
	public void normalizesWhatIsTypedOnlyOnCommit() {
		final List<Object> changes = new ArrayList<>();
		final TextFieldNode field = this.field("ab").<TextFieldNode>normalize(String::toUpperCase).<TextFieldNode>onChange(event -> changes.addAll(Arrays.asList(event.getText(), event.getValue(), event.isValid()))).cursorPosition(2);
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
		Assert.assertEquals(1, field.getCursorPosition());
		this.type(field, "c");
		Assert.assertEquals("acb", field.getText());
		Assert.assertEquals(2, field.getCursorPosition());
	}

	@Test
	public void reportsATextItsAcceptRefusesAsInvalid() {
		final List<Boolean> valid = new ArrayList<>();
		final TextFieldNode field = this.field("ab").<TextFieldNode>accept(text -> text.length() <= 3).onChange(event -> valid.add(event.isValid()));
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
		Assert.assertEquals("abc", TextFieldNode.create(100D, 100D, 200D).text("abcdef").maxTextLength(3).getText());
	}

	@Test
	public void takesBackItsWholeTextOnceItsMaximumLengthAllowsIt() {
		final Signal<Integer> length = new Signal<>(3);
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).text("abcdef").maxTextLength(length);
		this.bridges.open(new NodeUI(field)).frame();
		Assert.assertEquals("abc", field.getText());
		length.set(10);
		this.bridges.frame();
		Assert.assertEquals("abcdef", field.getText());
	}

	@Test
	public void takesBackItsTypedTextOnceItsMaximumLengthAllowsItAfterItsBlur() {
		final Signal<Integer> length = new Signal<>(20);
		final TextFieldNode field = this.field("").maxTextLength(length);
		this.type(field, "hello world");
		field.blur();
		length.set(5);
		this.bridges.frame();
		Assert.assertEquals("hello", field.getText());
		length.set(20);
		this.bridges.frame();
		Assert.assertEquals("hello world", field.getText());
	}

	@Test
	public void keepsItsTextOnceItsMaximumLengthChangesAfterARefusedChange() {
		final Signal<Integer> length = new Signal<>(20);
		final Signal<String> text = new Signal<>("abc");
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).maxTextLength(length).text(text).beforeChange(event -> event.cancel());
		this.bridges.open(new NodeUI(field)).frame();
		field.change("refused");
		length.set(10);
		this.bridges.frame();
		Assert.assertEquals("abc", field.getText());
		Assert.assertEquals("abc", text.peek());
	}

	@Test
	public void stopsAPasteAtItsMaximumLength() {
		final TextFieldNode field = this.field("ab").maxTextLength(4).cursorPosition(2);
		this.bridges.getWindow().setClipboard("xyz");
		this.control(field, Key.V);
		Assert.assertEquals("abxy", field.getText());
		Assert.assertEquals(4, field.getCursorPosition());
	}

	@Test
	public void reportsOnlyARealChangeOfFocus() {
		final List<Boolean> focuses = new ArrayList<>();
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).onFocus(event -> focuses.add(event.getNode().isFocused())).onBlur(event -> focuses.add(event.getNode().isFocused()));
		this.bridges.open(new NodeUI(field));
		field.focused(true).focused(true).focused(false);
		Assert.assertEquals(Arrays.asList(true, false), focuses);
	}

	@Test
	public void losesItsFocusWhenDetached() {
		final List<Boolean> focuses = new ArrayList<>();
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).onFocus(event -> focuses.add(event.getNode().isFocused())).onBlur(event -> focuses.add(event.getNode().isFocused()));
		this.bridges.open(new NodeUI(field));
		field.focused(true);
		field.fireDetach();
		Assert.assertFalse(field.isFocused());
		Assert.assertFalse(TextFieldNodeTest.<Boolean>read(field, FieldNode.class, "inputting"));
		Assert.assertEquals(Arrays.asList(true, false), focuses);
	}

	@Test
	public void writesItsFocusIntoItsSignal() {
		final Signal<Boolean> focused = new Signal<>(false);
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).text("hello").focused(focused);
		this.bridges.open(new NodeUI(field));
		this.click(124D, 120D);
		Assert.assertTrue(focused.get());
		this.press(field, Key.ESCAPE);
		Assert.assertFalse(focused.get());
		this.click(124D, 120D);
		this.click(500D, 500D);
		Assert.assertFalse(focused.get());
		focused.set(true);
		Assert.assertTrue(field.isFocused());
	}

	@Test
	public void forgetsItsFocusSignalOnceGivenAValue() {
		final Signal<Boolean> focused = new Signal<>(false);
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).text("hello").focused(focused).focused(true);
		this.bridges.open(new NodeUI(field));
		Assert.assertTrue(field.isFocused());
		this.press(field, Key.ESCAPE);
		focused.set(true);
		Assert.assertFalse(field.isFocused());
		Assert.assertTrue(TextFieldNodeTest.subscribers(focused).isEmpty());
	}

	@Test
	public void focusesLikeAClickThroughItsGestures() {
		final List<String> events = new ArrayList<>();
		final TextFieldNode field = TextFieldNode
		.create(100D, 100D, 200D)
		.info(this.info())
		.beforeFocus(event -> events.add("before focus"))
		.onFocus(event -> events.add("focus"))
		.beforeBlur(event -> events.add("before blur"))
		.onBlur(event -> events.add("blur"));
		this.bridges.open(new NodeUI(field));
		Assert.assertSame(field, field.focus());
		Assert.assertSame(field, field.blur());
		field.focused(true);
		Assert.assertEquals(Arrays.asList("before focus", "focus", "before blur", "blur", "focus"), events);
	}

	@Test
	public void staysUnfocusedWhenItsFocusIsRefused() {
		final Signal<Boolean> focused = new Signal<>(false);
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).text("hello").focused(focused).beforeFocus(event -> event.cancel());
		this.bridges.open(new NodeUI(field));
		this.click(124D, 120D);
		field.focus();
		Assert.assertFalse(field.isFocused());
		Assert.assertFalse(focused.get());
		Assert.assertEquals(0, field.getCursorPosition());
		focused.set(true);
		Assert.assertTrue(field.isFocused());
	}

	@Test
	public void staysFocusedWhenItsBlurIsRefused() {
		final TextFieldNode field = this.field("hello").beforeBlur(event -> event.cancel());
		this.press(field, Key.ESCAPE);
		this.click(500D, 500D);
		field.blur();
		Assert.assertTrue(field.isFocused());
		field.focused(false);
		Assert.assertFalse(field.isFocused());
	}

	@Test
	public void focusesOnAClickAndPutsTheCursorUnderTheMouse() {
		final List<Boolean> focuses = new ArrayList<>();
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).text("hello").onFocus(event -> focuses.add(event.getNode().isFocused()));
		this.bridges.open(new NodeUI(field));
		this.click(124D, 120D);
		Assert.assertTrue(field.isFocused());
		Assert.assertEquals(2, field.getCursorPosition());
		Assert.assertEquals(Collections.singletonList(true), focuses);
	}

	@Test
	public void focusesOnAClickItAlsoGivesToItsListener() {
		final List<String> clicks = new ArrayList<>();
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).text("hello").onClick(event -> clicks.add("click"));
		this.bridges.open(new NodeUI(field));
		this.click(124D, 120D);
		Assert.assertTrue(field.isFocused());
		Assert.assertEquals(Collections.singletonList("click"), clicks);
	}

	@Test
	public void putsTheCursorAtTheEndOnAClickPastTheText() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).text("hello");
		this.bridges.open(new NodeUI(field));
		this.click(250D, 120D);
		Assert.assertEquals(5, field.getCursorPosition());
	}

	@Test
	public void focusesAnEmptyFieldOnAClick() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info());
		this.bridges.open(new NodeUI(field));
		this.click(250D, 120D);
		Assert.assertTrue(field.isFocused());
		Assert.assertEquals(0, field.getCursorPosition());
	}

	@Test
	public void putsTheCursorUnderAClickOnAnAlignedText() {
		final TextFieldNode centered = TextFieldNode.create(100D, 100D, 200D).info(this.info()).<TextFieldNode>text("abcd").alignX(Align.CENTER);
		final TextFieldNode end = TextFieldNode.create(100D, 200D, 200D).info(this.info()).<TextFieldNode>text("abcd").alignX(Align.END);
		this.bridges.open(new NodeUI(centered, end));
		this.click(196D, 120D);
		Assert.assertEquals(2, centered.getCursorPosition());
		this.click(270D, 220D);
		Assert.assertEquals(1, end.getCursorPosition());
	}

	@Test
	public void unfocusesAndDropsItsSelectionOnAClickBesideIt() {
		final TextFieldNode field = this.field("hello");
		this.control(field, Key.A);
		this.click(500D, 500D);
		Assert.assertFalse(field.isFocused());
		Assert.assertEquals(-1, TextFieldNodeTest.anchor(field));
	}

	@Test
	public void unfocusesWhenTheClickWasAlreadyTaken() {
		final TextFieldNode field = this.field("hello");
		final NodeMouseEvent<?> consumedPress = NodeMouseEvent.create(field, 124D, 120D, MouseButton.LEFT, 0L, null);
		consumedPress.consume();
		field.mousePressed(consumedPress);
		Assert.assertFalse(field.isFocused());
	}

	@Test
	public void extendsItsSelectionOnAShiftClick() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).text("hello");
		this.bridges.open(new NodeUI(field));
		this.click(114D, 120D);
		Assert.assertEquals(1, field.getCursorPosition());
		this.bridges.getWindow().getKeys().add(Key.LEFT_SHIFT);
		this.click(144D, 120D);
		Assert.assertEquals(1, TextFieldNodeTest.anchor(field));
		Assert.assertEquals(4, field.getCursorPosition());
		this.click(124D, 120D);
		Assert.assertEquals(1, TextFieldNodeTest.anchor(field));
		Assert.assertEquals(2, field.getCursorPosition());
		this.bridges.getWindow().getKeys().remove(Key.LEFT_SHIFT);
		this.bridges.getClock().advance(600L);
		this.click(124D, 120D);
		Assert.assertEquals(-1, TextFieldNodeTest.anchor(field));
	}

	@Test
	public void movesAndErasesAnEmojiAsOneCharacter() {
		final TextFieldNode field = this.field("a\uD83D\uDE00b").cursorPosition(2);
		Assert.assertEquals(1, field.getCursorPosition());
		this.press(field, Key.RIGHT);
		Assert.assertEquals(3, field.getCursorPosition());
		this.press(field, Key.LEFT);
		Assert.assertEquals(1, field.getCursorPosition());
		this.press(field, Key.DELETE);
		Assert.assertEquals("ab", field.getText());
		final TextFieldNode other = this.field("a\uD83D\uDE00b").cursorPosition(3);
		this.press(other, Key.BACKSPACE);
		Assert.assertEquals("ab", other.getText());
		Assert.assertEquals(1, other.getCursorPosition());
	}

	@Test
	public void countsAnEmojiAsOneCharacterOfItsMaximumLength() {
		Assert.assertEquals("a\uD83D\uDE00", TextFieldNode.create(100D, 100D, 200D).maxTextLength(2).text("a\uD83D\uDE00b").getText());
		Assert.assertEquals("a", TextFieldNode.create(100D, 100D, 200D).maxTextLength(1).text("a\uD83D\uDE00").getText());
		final TextFieldNode field = this.field("a").maxTextLength(2).cursorPosition(1);
		Assert.assertTrue(this.bridges.getUi().charTyped(0x1F600));
		Assert.assertTrue(this.bridges.getUi().charTyped(0x1F600));
		Assert.assertEquals("a\uD83D\uDE00", field.getText());
	}

	@Test
	public void selectsAnEmojiWholeWithShiftAndArrows() {
		final TextFieldNode field = this.field("\uD83D\uDE00b").cursorPosition(0);
		this.bridges.getWindow().getKeys().add(Key.LEFT_SHIFT);
		this.press(field, Key.RIGHT);
		this.bridges.getWindow().getKeys().remove(Key.LEFT_SHIFT);
		Assert.assertEquals(0, TextFieldNodeTest.anchor(field));
		Assert.assertEquals(2, field.getCursorPosition());
		this.type(field, "x");
		Assert.assertEquals("xb", field.getText());
	}

	@Test
	public void refusesToBeAttachedWithoutInfo() {
		try {
			this.bridges.open(new NodeUI(TextFieldNode.create(100D, 100D, 200D)));
			Assert.fail("A field without info cannot be attached");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The TextFieldNode has no info, call info(...) before attaching it", expected.getMessage());
		}
	}

	@Test
	public void insertsACharacterOutsideTheBasicPlane() {
		final TextFieldNode field = this.field("ab").cursorPosition(2);
		Assert.assertTrue(this.bridges.getUi().charTyped(0x1F600));
		Assert.assertEquals("ab\uD83D\uDE00", field.getText());
		Assert.assertEquals(4, field.getCursorPosition());
	}

	@Test
	public void typesNoCharacterWhileControlIsDown() {
		final TextFieldNode field = this.field("ab").cursorPosition(2);
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		Assert.assertTrue(this.bridges.getUi().keyPressed(Key.B, Key.B));
		Assert.assertTrue(this.bridges.getUi().charTyped('b'));
		Assert.assertEquals("ab", field.getText());
	}

	@Test
	public void typesTheCharacterOfAltGr() {
		final TextFieldNode field = this.field("ab").cursorPosition(2);
		this.bridges.getWindow().getKeys().addAll(Arrays.asList(Key.LEFT_CONTROL, Key.RIGHT_ALT));
		this.bridges.getUi().keyPressed(Key.DIGIT_0, Key.DIGIT_0);
		this.bridges.getUi().charTyped('@');
		Assert.assertEquals("ab@", field.getText());
	}

	@Test
	public void copiesAndPastesWithoutTypingTheShortcutLetter() {
		final TextFieldNode field = this.field("ab").cursorPosition(2);
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.bridges.getUi().keyPressed(Key.A, Key.A);
		this.bridges.getUi().charTyped('a');
		this.bridges.getUi().keyPressed(Key.C, Key.C);
		this.bridges.getUi().charTyped('\u0003');
		this.bridges.getWindow().getKeys().remove(Key.LEFT_CONTROL);
		this.bridges.getUi().keyPressed(Key.END, Key.END);
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.bridges.getUi().keyPressed(Key.V, Key.V);
		this.bridges.getUi().charTyped('v');
		Assert.assertEquals("ab", this.bridges.getWindow().getClipboard());
		Assert.assertEquals("abab", field.getText());
	}

	@Test
	public void ignoresTheKeyboardWhileUnfocused() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).text("abc");
		this.bridges.open(new NodeUI(field));
		final NodeCharEvent<?> context = NodeCharEvent.create(field, 'x', null);
		field.charTyped(context);
		Assert.assertEquals("abc", field.getText());
		Assert.assertFalse(context.isConsumed());
	}

	@Test
	public void leavesAKeyAlreadyTakenElsewhere() {
		final TextFieldNode field = this.field("abc");
		final NodeKeyEvent<?> consumedKey = NodeKeyEvent.create(field, Key.X, Key.X, null);
		consumedKey.consume();
		field.keyPressed(consumedKey);
		final NodeCharEvent<?> consumedChar = NodeCharEvent.create(field, 'x', null);
		consumedChar.consume();
		field.charTyped(consumedChar);
		Assert.assertEquals("abc", field.getText());
	}

	@Test
	public void consumesTheKeysItReceives() {
		final TextFieldNode field = this.field("abc");
		final NodeKeyEvent<?> key = NodeKeyEvent.create(field, Key.X, Key.X, null);
		final NodeCharEvent<?> character = NodeCharEvent.create(field, 'x', null);
		field.keyPressed(key);
		field.charTyped(character);
		Assert.assertTrue(key.isConsumed());
		Assert.assertTrue(character.isConsumed());
	}

	@Test
	public void receivesTheKeysTypedInItsUi() {
		final TextFieldNode field = this.field("ab").cursorPosition(2);
		this.bridges.getUi().keyPressed(Key.C, Key.C);
		this.bridges.getUi().charTyped('c');
		Assert.assertEquals("abc", field.getText());
	}

	@Test
	public void receivesTheKeysTypedWhileScrolledOutOfView() {
		final TextFieldNode field = TextFieldNode.create(10D, 10D, 100D).info(this.info());
		final ContainerNode box = ContainerNode.create(100D, 100D, 300D, 300D).overflow(OverflowProperty.SCROLL).append(field, RectNode.create(10D, 600D, 100D, 40D));
		this.bridges.open(new NodeUI(box));
		this.click(120D, 120D);
		box.scrollTo(0D, -550D);
		this.bridges.frames(60);
		Assert.assertFalse(field.isVisible());
		this.bridges.getUi().charTyped('a');
		this.bridges.getUi().charTyped('b');
		this.bridges.getUi().keyPressed(Key.BACKSPACE, Key.BACKSPACE);
		Assert.assertEquals("a", field.getText());
		Assert.assertTrue(field.isFocused());
	}

	@Test
	public void losesItsFocusOutOfViewWhenAnotherFieldIsClicked() {
		final TextFieldNode hidden = TextFieldNode.create(10D, 10D, 100D).info(this.info());
		final TextFieldNode shown = TextFieldNode.create(10D, 600D, 100D).info(this.info());
		final ContainerNode box = ContainerNode.create(100D, 100D, 300D, 300D).overflow(OverflowProperty.SCROLL).append(hidden, shown);
		this.bridges.open(new NodeUI(box));
		this.click(120D, 120D);
		box.scrollTo(0D, -550D);
		this.bridges.frames(60);
		this.click(120D, shown.getAbsoluteY() + 5D);
		this.bridges.getUi().charTyped('c');
		Assert.assertFalse(hidden.isFocused());
		Assert.assertTrue(shown.isFocused());
		Assert.assertEquals("", hidden.getText());
		Assert.assertEquals("c", shown.getText());
	}

	@Test
	public void blursItsFocusedFieldBeforeFocusingTheClickedOne() {
		final List<String> events = new ArrayList<>();
		final TextFieldNode first = TextFieldNode.create(100D, 100D, 200D).info(this.info());
		final TextFieldNode second = TextFieldNode.create(100D, 200D, 200D).info(this.info());
		first.onBlur(event -> events.add("blur first " + second.isFocused()));
		second.onFocus(event -> events.add("focus second " + first.isFocused()));
		this.bridges.open(new NodeUI(first, second));
		first.focus();
		this.click(124D, 220D);
		Assert.assertEquals(Arrays.asList("blur first false", "focus second false"), events);
	}

	@Test
	public void blursTheFocusedFieldOfAnotherUiBeforeFocusingTheClickedOne() {
		final List<String> events = new ArrayList<>();
		final TextFieldNode first = TextFieldNode.create(100D, 100D, 200D).info(this.info());
		final TextFieldNode second = TextFieldNode.create(100D, 200D, 200D).info(this.info());
		first.onBlur(event -> events.add("blur first " + second.isFocused()));
		second.onFocus(event -> events.add("focus second " + first.isFocused()));
		this.bridges.open(new NodeUI(first)).open(new NodeUI(second));
		first.focus();
		this.click(124D, 220D);
		this.bridges.getUi().keyPressed(Key.ESCAPE, Key.ESCAPE);
		this.bridges.getUi().charTyped('z');
		Assert.assertEquals(Arrays.asList("blur first false", "focus second false"), events);
		Assert.assertFalse(first.isFocused());
		Assert.assertEquals("", first.getText());
	}

	@Test
	public void leavesTheClickedFieldUnfocusedWhenTheBlurOfTheFocusedOneDetachesIt() {
		final TextFieldNode first = TextFieldNode.create(100D, 100D, 200D).info(this.info());
		final TextFieldNode second = TextFieldNode.create(100D, 200D, 200D).info(this.info());
		final NodeUI ui = new NodeUI(first, second);
		first.onBlur(event -> ui.remove(second));
		this.bridges.open(ui);
		first.focus();
		this.click(124D, 220D);
		Assert.assertFalse(second.isFocused());
		second.attach(ui);
		this.bridges.frames(2);
		Assert.assertFalse(second.isFocused());
	}

	@Test
	public void blursTheFocusedFieldOfAScreenBelowTheClickedFieldOfAnOverlay() {
		final List<String> events = new ArrayList<>();
		final TextFieldNode first = TextFieldNode.create(100D, 100D, 200D).info(this.info());
		final TextFieldNode second = TextFieldNode.create(100D, 100D, 200D).info(this.info());
		first.onBlur(event -> events.add("blur first " + second.isFocused()));
		second.onFocus(event -> events.add("focus second " + first.isFocused()));
		this.bridges.open(new NodeUI(first)).open(new OverlayUI(second));
		first.focus();
		this.click(124D, 120D);
		this.bridges.getUi().keyPressed(Key.ESCAPE, Key.ESCAPE);
		this.bridges.getUi().charTyped('z');
		Assert.assertEquals(Arrays.asList("blur first false", "focus second false"), events);
		Assert.assertFalse(first.isFocused());
		Assert.assertEquals("", first.getText());
	}

	@Test
	public void blursTheFocusedFieldThatHoldsTheClickedOneFirst() {
		final List<String> events = new ArrayList<>();
		final TextFieldNode outer = TextFieldNode.create(100D, 100D, 400D).info(this.info());
		final TextFieldNode inner = TextFieldNode.create(0D, 0D, 100D).info(this.info());
		inner.attach(outer);
		outer.onBlur(event -> events.add("blur outer " + inner.isFocused()));
		inner.onFocus(event -> events.add("focus inner " + outer.isFocused()));
		this.bridges.open(new NodeUI(outer));
		outer.focus();
		this.click(110D, 120D);
		Assert.assertEquals(Arrays.asList("blur outer false", "focus inner false"), events);
	}

	@Test
	public void insertsWhatIsTypedAtTheCursor() {
		final TextFieldNode field = this.field("hllo").cursorPosition(1);
		this.type(field, "e");
		Assert.assertEquals("hello", field.getText());
		Assert.assertEquals(2, field.getCursorPosition());
	}

	@Test
	public void dropsTheControlCharacters() {
		final List<String> changes = new ArrayList<>();
		final TextFieldNode field = this.field("ab").onChange(event -> changes.add(event.getText()));
		this.type(field, "\u0007\u001b\u007f");
		Assert.assertEquals("ab", field.getText());
		Assert.assertTrue(changes.isEmpty());
		this.type(field, "€");
		Assert.assertEquals("€ab", field.getText());
	}

	@Test
	public void replacesTheSelectionWithWhatIsTyped() {
		final TextFieldNode field = this.field("hello");
		this.control(field, Key.A);
		this.type(field, "x");
		Assert.assertEquals("x", field.getText());
		Assert.assertEquals(1, field.getCursorPosition());
		Assert.assertEquals(-1, TextFieldNodeTest.anchor(field));
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
		Assert.assertEquals(2, field.getCursorPosition());
	}

	@Test
	public void movesItsCursorWithTheArrows() {
		final TextFieldNode field = this.field("abc").cursorPosition(1);
		this.press(field, Key.RIGHT);
		Assert.assertEquals(2, field.getCursorPosition());
		this.press(field, Key.RIGHT);
		this.press(field, Key.RIGHT);
		Assert.assertEquals(3, field.getCursorPosition());
		this.press(field, Key.LEFT);
		Assert.assertEquals(2, field.getCursorPosition());
		for (int i = 0; i < 3; i++) {
			this.press(field, Key.LEFT);
		}
		Assert.assertEquals(0, field.getCursorPosition());
	}

	@Test
	public void writesItsCursorMovesIntoItsSignal() {
		final Signal<Integer> cursor = new Signal<>(1);
		final TextFieldNode field = this.field("abc").cursorPosition(cursor);
		this.press(field, Key.RIGHT);
		Assert.assertEquals(2, cursor.get().intValue());
		this.type(field, "x");
		Assert.assertEquals(3, cursor.get().intValue());
		cursor.set(0);
		Assert.assertEquals(0, field.getCursorPosition());
		cursor.set(99);
		Assert.assertEquals(4, field.getCursorPosition());
		Assert.assertEquals(4, cursor.get().intValue());
	}

	@Test
	public void movesItsCursorLikeTheArrowsThroughItsGesture() {
		final List<String> events = new ArrayList<>();
		final TextFieldNode field = this.field("abc").beforeCursorMove(event -> events.add("before " + event.getValue())).onCursorMove(event -> events.add("after " + event.getPrevious() + " " + event.getValue()));
		Assert.assertSame(field, field.moveCursor(1));
		field.cursorPosition(3);
		Assert.assertEquals(Arrays.asList("before 1", "after 0 1", "after 1 3"), events);
	}

	@Test
	public void announcesTheCursorTheListenerOfItsMovePlacesLast() {
		final List<Object> moves = new ArrayList<>();
		final TextFieldNode field = this.field("abcdef");
		field.onCursorMove(event -> {
			if (event.getValue() > 2) {
				field.moveCursor(2);
			}
		});
		field.onCursorMove(event -> moves.add(event.getValue()));
		field.moveCursor(5);
		Assert.assertEquals(2, field.getCursorPosition());
		Assert.assertEquals(Arrays.asList(5, 2), moves);
	}

	@Test
	public void keepsItsCursorWhenItsMoveIsRefused() {
		final TextFieldNode field = this.field("abc").cursorPosition(1).beforeCursorMove(event -> event.cancel());
		this.press(field, Key.RIGHT);
		field.moveCursor(3);
		Assert.assertEquals(1, field.getCursorPosition());
		field.cursorPosition(2);
		Assert.assertEquals(2, field.getCursorPosition());
	}

	@Test
	public void movesTheCursorOfAnAlignedText() {
		final TextFieldNode centered = TextFieldNode.create(100D, 100D, 200D).info(this.info()).<TextFieldNode>text("abcd").alignX(Align.CENTER).focused(true).cursorPosition(2);
		final TextFieldNode end = TextFieldNode.create(100D, 200D, 200D).info(this.info()).<TextFieldNode>text("abcd").alignX(Align.END).focused(true).cursorPosition(2);
		this.bridges.open(new NodeUI(centered, end));
		this.press(centered, Key.LEFT);
		this.press(centered, Key.RIGHT);
		this.press(centered, Key.RIGHT);
		this.press(end, Key.LEFT);
		this.press(end, Key.RIGHT);
		this.press(end, Key.RIGHT);
		Assert.assertEquals(3, centered.getCursorPosition());
		Assert.assertEquals(3, end.getCursorPosition());
	}

	@Test
	public void jumpsOverWholeWordsWithControl() {
		final TextFieldNode field = this.field("one two  three");
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.press(field, Key.RIGHT);
		Assert.assertEquals(4, field.getCursorPosition());
		this.press(field, Key.RIGHT);
		Assert.assertEquals(9, field.getCursorPosition());
		this.press(field, Key.RIGHT);
		Assert.assertEquals(14, field.getCursorPosition());
		this.press(field, Key.LEFT);
		Assert.assertEquals(9, field.getCursorPosition());
		this.press(field, Key.LEFT);
		Assert.assertEquals(4, field.getCursorPosition());
		this.press(field, Key.LEFT);
		Assert.assertEquals(0, field.getCursorPosition());
	}

	@Test
	public void selectsWithShiftAndTheArrows() {
		final TextFieldNode field = this.field("abcdef").cursorPosition(1);
		this.bridges.getWindow().getKeys().add(Key.LEFT_SHIFT);
		for (int i = 0; i < 3; i++) {
			this.press(field, Key.RIGHT);
		}
		Assert.assertEquals(1, TextFieldNodeTest.anchor(field));
		Assert.assertEquals(4, field.getCursorPosition());
		this.bridges.getWindow().getKeys().remove(Key.LEFT_SHIFT);
		this.bridges.getWindow().getKeys().add(Key.RIGHT_SHIFT);
		this.press(field, Key.LEFT);
		Assert.assertEquals(1, TextFieldNodeTest.anchor(field));
		Assert.assertEquals(3, field.getCursorPosition());
		this.bridges.getWindow().getKeys().remove(Key.RIGHT_SHIFT);
		this.press(field, Key.RIGHT);
		Assert.assertEquals(-1, TextFieldNodeTest.anchor(field));
		Assert.assertEquals(4, field.getCursorPosition());
	}

	@Test
	public void dropsItsSelectionOnALeftArrowWithoutShift() {
		final TextFieldNode field = this.field("abcdef").cursorPosition(1);
		this.bridges.getWindow().getKeys().add(Key.RIGHT_SHIFT);
		this.press(field, Key.RIGHT);
		Assert.assertEquals(1, TextFieldNodeTest.anchor(field));
		this.bridges.getWindow().getKeys().remove(Key.RIGHT_SHIFT);
		this.press(field, Key.LEFT);
		Assert.assertEquals(-1, TextFieldNodeTest.anchor(field));
		Assert.assertEquals(1, field.getCursorPosition());
	}

	@Test
	public void selectsWholeWordsWithShiftAndControl() {
		final TextFieldNode field = this.field("one two");
		this.bridges.getWindow().getKeys().addAll(Arrays.asList(Key.LEFT_SHIFT, Key.LEFT_CONTROL));
		this.press(field, Key.RIGHT);
		Assert.assertEquals(0, TextFieldNodeTest.anchor(field));
		Assert.assertEquals(4, field.getCursorPosition());
	}

	@Test
	public void reachesBothEndsWithHomeAndEnd() {
		final TextFieldNode field = this.field("abc").cursorPosition(1);
		this.press(field, Key.END);
		Assert.assertEquals(3, field.getCursorPosition());
		this.press(field, Key.HOME);
		Assert.assertEquals(0, field.getCursorPosition());
	}

	@Test
	public void erasesTheCharacterBeforeTheCursorWithBackspace() {
		final TextFieldNode field = this.field("abc").cursorPosition(2);
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("ac", field.getText());
		Assert.assertEquals(1, field.getCursorPosition());
		this.press(field, Key.HOME);
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("ac", field.getText());
		Assert.assertEquals(0, field.getCursorPosition());
	}

	@Test
	public void erasesTheWordBeforeTheCursorWithControlBackspace() {
		final TextFieldNode field = this.field("one two").cursorPosition(7);
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("one ", field.getText());
		Assert.assertEquals(4, field.getCursorPosition());
	}

	@Test
	public void erasesTheSelectionWithBackspace() {
		final TextFieldNode forward = this.selected(1, 4);
		this.press(forward, Key.BACKSPACE);
		Assert.assertEquals("aef", forward.getText());
		Assert.assertEquals(1, forward.getCursorPosition());
		Assert.assertEquals(-1, TextFieldNodeTest.anchor(forward));
		final TextFieldNode backward = this.selected(4, 1);
		this.press(backward, Key.BACKSPACE);
		Assert.assertEquals("aef", backward.getText());
		Assert.assertEquals(1, backward.getCursorPosition());
	}

	@Test
	public void erasesTheCharacterAfterTheCursorWithDelete() {
		final TextFieldNode field = this.field("abc").cursorPosition(1);
		this.press(field, Key.DELETE);
		Assert.assertEquals("ac", field.getText());
		Assert.assertEquals(1, field.getCursorPosition());
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
		Assert.assertEquals(0, field.getCursorPosition());
	}

	@Test
	public void erasesTheSelectionWithDelete() {
		final TextFieldNode forward = this.selected(1, 4);
		this.press(forward, Key.DELETE);
		Assert.assertEquals("aef", forward.getText());
		Assert.assertEquals(1, forward.getCursorPosition());
		final TextFieldNode backward = this.selected(4, 1);
		this.press(backward, Key.DELETE);
		Assert.assertEquals("aef", backward.getText());
		Assert.assertEquals(1, backward.getCursorPosition());
		Assert.assertEquals(-1, TextFieldNodeTest.anchor(backward));
	}

	@Test
	public void selectsEverythingWithControlA() {
		final TextFieldNode field = this.field("hello").cursorPosition(2);
		this.control(field, Key.A);
		Assert.assertEquals(0, TextFieldNodeTest.anchor(field));
		Assert.assertEquals(5, field.getCursorPosition());
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
		Assert.assertEquals(5, TextFieldNodeTest.anchor(backward));
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
		Assert.assertEquals(1, forward.getCursorPosition());
		Assert.assertEquals(-1, TextFieldNodeTest.anchor(forward));
		final TextFieldNode backward = this.selected(5, 2);
		this.control(backward, Key.X);
		Assert.assertEquals("cde", this.bridges.getWindow().getClipboard());
		Assert.assertEquals("abf", backward.getText());
		Assert.assertEquals(2, backward.getCursorPosition());
		Assert.assertEquals(-1, TextFieldNodeTest.anchor(backward));
	}

	@Test
	public void pastesTheClipboardAtTheCursor() {
		final TextFieldNode field = this.field("abc").cursorPosition(1);
		this.bridges.getWindow().setClipboard("xy");
		this.control(field, Key.V);
		Assert.assertEquals("axybc", field.getText());
		Assert.assertEquals(3, field.getCursorPosition());
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
		Assert.assertEquals(3, field.getCursorPosition());
	}

	@Test
	public void ignoresAnEmptyClipboard() {
		final List<String> changes = new ArrayList<>();
		final TextFieldNode field = this.field("abc").onChange(event -> changes.add(event.getText()));
		this.control(field, Key.V);
		Assert.assertEquals("abc", field.getText());
		Assert.assertTrue(changes.isEmpty());
	}

	@Test
	public void unfocusesOnEnterAndEscapeButReportsOnlyEnter() {
		final List<String> entered = new ArrayList<>();
		final TextFieldNode field = this.field("hello").onSubmit(event -> entered.add(event.getNode().getText()));
		for (final Key key : Arrays.asList(Key.ENTER, Key.NUMPAD_ENTER, Key.ESCAPE)) {
			field.focused(true);
			this.press(field, key);
			Assert.assertFalse(field.isFocused());
		}
		Assert.assertEquals(Arrays.asList("hello", "hello"), entered);
	}

	@Test
	public void keepsItsTypedTextOnEscape() {
		final Signal<String> signal = new Signal<>("hello");
		final TextFieldNode field = this.field("").text(signal).cursorPosition(5);
		field.focused(false).focused(true);
		this.type(field, " world");
		Assert.assertEquals("hello world", signal.get());
		this.press(field, Key.ESCAPE);
		Assert.assertEquals("hello world", field.getText());
		Assert.assertEquals("hello world", signal.get());
		Assert.assertFalse(field.isFocused());
	}

	@Test
	public void unfocusesOnEscapeInsideAUiThatStaysOpen() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).text("hello").focused(true);
		final PinnedUI ui = new PinnedUI(field);
		this.bridges.open(ui);
		this.bridges.getUi().keyPressed(Key.ESCAPE, Key.ESCAPE);
		Assert.assertFalse(field.isFocused());
		Assert.assertTrue(this.bridges.getUi().isOpen(ui));
	}

	@Test
	public void validatesItsEditOnEscapeBeforeItsUiCloses() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).text("hello").focused(true);
		final NodeUI ui = new NodeUI(field);
		this.bridges.open(ui);
		this.type(field, "!");
		this.bridges.getUi().keyPressed(Key.ESCAPE, Key.ESCAPE);
		Assert.assertFalse(field.isFocused());
		Assert.assertEquals("!hello", field.getText());
		Assert.assertTrue(this.bridges.getUi().isOpen(ui));
		this.bridges.getUi().keyPressed(Key.ESCAPE, Key.ESCAPE);
		Assert.assertFalse(this.bridges.getUi().isOpen(ui));
	}

	@Test
	public void repeatsAHeldArrowAfterHalfASecond() {
		final TextFieldNode field = this.field("abcdef");
		this.bridges.getWindow().getKeys().add(Key.RIGHT);
		this.press(field, Key.RIGHT);
		Assert.assertTrue(TextFieldNodeTest.<Boolean>read(field, FieldNode.class, "inputting"));
		Assert.assertSame(Key.RIGHT, TextFieldNodeTest.<Key>read(field, FieldNode.class, "inputType"));
		this.bridges.getClock().advance(480L);
		this.bridges.frame();
		Assert.assertEquals(1, field.getCursorPosition());
		this.bridges.frame();
		Assert.assertEquals(2, field.getCursorPosition());
		this.bridges.getClock().advance(68L);
		this.bridges.frame();
		Assert.assertEquals(2, field.getCursorPosition());
		this.bridges.frame();
		Assert.assertEquals(3, field.getCursorPosition());
	}

	@Test
	public void repeatsAHeldLeftArrow() {
		final TextFieldNode field = this.field("abcdef").cursorPosition(6);
		this.bridges.getWindow().getKeys().add(Key.LEFT);
		this.press(field, Key.LEFT);
		this.elapse(500L);
		Assert.assertEquals(4, field.getCursorPosition());
		this.elapse(100L);
		Assert.assertEquals(3, field.getCursorPosition());
	}

	@Test
	public void stopsRepeatingOnceTheKeyIsReleased() {
		final TextFieldNode field = this.field("abcdef");
		this.press(field, Key.RIGHT);
		this.elapse(600L);
		Assert.assertFalse(TextFieldNodeTest.<Boolean>read(field, FieldNode.class, "inputting"));
		Assert.assertEquals(1, field.getCursorPosition());
	}

	@Test
	public void stopsRepeatingOnceItLosesItsFocus() {
		final TextFieldNode field = this.field("abc").cursorPosition(3);
		this.bridges.getWindow().getKeys().add(Key.BACKSPACE);
		this.press(field, Key.BACKSPACE);
		this.press(field, Key.ESCAPE);
		this.elapse(500L);
		Assert.assertEquals("ab", field.getText());
		Assert.assertFalse(TextFieldNodeTest.<Boolean>read(field, FieldNode.class, "inputting"));
	}

	@Test
	public void submitsOnceWhileEnterIsHeld() {
		final List<String> entered = new ArrayList<>();
		final TextFieldNode field = this.field("hello").onSubmit(event -> entered.add(event.getNode().getText()));
		this.bridges.getWindow().getKeys().add(Key.ENTER);
		this.press(field, Key.ENTER);
		this.elapse(600L);
		Assert.assertEquals(Collections.singletonList("hello"), entered);
		Assert.assertFalse(TextFieldNodeTest.<Boolean>read(field, FieldNode.class, "inputting"));
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
		Assert.assertFalse(TextFieldNodeTest.<Boolean>read(field, FieldNode.class, "inputting"));
		Assert.assertEquals(0, field.getCursorPosition());
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
		Assert.assertFalse(TextFieldNodeTest.<Boolean>read(field, FieldNode.class, "inputting"));
	}

	@Test
	public void repeatsWholeWordsWhileControlIsHeld() {
		final TextFieldNode field = this.field("one two three").cursorPosition(13);
		this.bridges.getWindow().getKeys().addAll(Arrays.asList(Key.BACKSPACE, Key.LEFT_CONTROL));
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("one two ", field.getText());
		this.elapse(500L);
		Assert.assertEquals("one ", field.getText());
		Assert.assertEquals(4, field.getCursorPosition());
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
		Assert.assertEquals(8, field.getCursorPosition());
		this.bridges.getWindow().getKeys().remove(Key.RIGHT);
		this.elapse(100L);
		this.bridges.getWindow().getKeys().add(Key.LEFT);
		this.press(field, Key.LEFT);
		this.elapse(500L);
		Assert.assertEquals(0, field.getCursorPosition());
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
	public void drawsItsPlaceholderAtHalfTheOpacityOfItsText() {
		this.bridges.open(new NodeUI(TextFieldNode.create(100D, 100D, 200D).info(TextInfo.create(this.font, 10F).color(new Color(0.2F, 0.4F, 0.6F, 0.4F))).placeholder("Search")));
		Assert.assertEquals("Search", this.font.last().getText());
		Assert.assertEquals(0.2F, this.font.last().getAlpha(), 1E-6F);
	}

	@Test
	public void keepsTheGradientOfItsTextOnItsPlaceholderAndItsCursor() {
		final Color gradient = new Color(1F, 0F, 0F, 1F).toGradient(new Color(0F, 0F, 1F, 0.5F), Point.of(0D, 0D), Point.of(1D, 0D));
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(TextInfo.create(this.font, 10F).color(gradient)).placeholder("Search");
		Assert.assertTrue(field.getPlaceholderInfo().getColor().isGradient());
		Assert.assertEquals(0.5F, field.getPlaceholderInfo().getColor().getAlphaF(), 1E-6F);
		Assert.assertTrue(field.getShownCursorColor().isGradient());
	}

	@Test
	public void centersItsTextHorizontally() {
		this.bridges.open(new NodeUI(TextFieldNode.create(100D, 100D, 200D, 60D).info(this.info()).<TextFieldNode>text("abcd").alignX(Align.CENTER)));
		Assert.assertEquals(180D, this.font.last().getX(), 0D);
		Assert.assertEquals(120D, this.font.last().getY(), 0D);
	}

	@Test
	public void alignsItsTextToTheEnd() {
		this.bridges.open(new NodeUI(TextFieldNode.create(100D, 100D, 200D, 60D).info(this.info()).<TextFieldNode>text("abcd").alignX(Align.END).alignY(Align.END)));
		Assert.assertEquals(256D, this.font.last().getX(), 0D);
		Assert.assertEquals(130D, this.font.last().getY(), 0D);
	}

	@Test
	public void alignsItsTextToTheTop() {
		this.bridges.open(new NodeUI(TextFieldNode.create(100D, 100D, 200D, 60D).info(this.info()).<TextFieldNode>text("abcd").alignY(Align.START)));
		Assert.assertEquals(102D, this.font.last().getX(), 0D);
		Assert.assertEquals(110D, this.font.last().getY(), 0D);
	}

	@Test
	public void alignsItsPlaceholderOnItsOwnWidth() {
		this.bridges.open(new NodeUI(TextFieldNode.create(100D, 100D, 200D).info(this.info()).<TextFieldNode>placeholder("Search").alignX(Align.END)));
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
		this.bridges.getClock().time(234L);
		this.bridges.frame();
		Assert.assertEquals(1F, this.cursor().getAlpha(), 0.0001F);
		this.bridges.getClock().time(984L);
		this.bridges.frame();
		Assert.assertEquals(0.5F, this.cursor().getAlpha(), 0.0001F);
	}

	@Test
	public void hidesItsCursorWhileUnfocused() {
		this.bridges.open(new NodeUI(TextFieldNode.create(100D, 100D, 200D).info(this.info()).text("abcd")));
		Assert.assertTrue(this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F).isEmpty());
	}

	@Test
	public void drawsItsSelectionInItsSelectionColor() {
		this.selected(1, 4).selectionColor(new Color(0.9F, 0.1F, 0.1F, 1F));
		this.bridges.frame();
		Assert.assertTrue(this.selections().isEmpty());
		Assert.assertEquals(1, this.bridges.getRender().getDraws(0.9F, 0.1F, 0.1F).size());
	}

	@Test
	public void drawsItsPlaceholderInItsPlaceholderColor() {
		this.bridges.open(new NodeUI(TextFieldNode.create(100D, 100D, 200D).info(this.info()).placeholder("name").placeholderColor(new Color(0.1F, 0.1F, 0.9F, 0.3F))));
		Assert.assertEquals("name", this.font.last().getText());
		Assert.assertEquals(0.3F, this.font.last().getAlpha(), 1E-6F);
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
		Assert.assertEquals(19D, TextFieldNodeTest.<Double>read(field, LineFieldNode.class, "xOffset"), 0D);
		this.bridges.frame();
		Assert.assertEquals(83D, this.font.last().getX(), 0D);
		Assert.assertEquals(283D, this.cursor().getLeft(), 0.001D);
		this.press(field, Key.HOME);
		this.bridges.frame();
		Assert.assertEquals(0D, TextFieldNodeTest.<Double>read(field, LineFieldNode.class, "xOffset"), 0D);
		Assert.assertEquals(102D, this.font.last().getX(), 0D);
	}

	@Test
	public void scrollsWhileTypingPastItsWidth() {
		final TextFieldNode field = this.field("");
		this.type(field, "abcdefghijklmnopqrst");
		Assert.assertEquals(19D, TextFieldNodeTest.<Double>read(field, LineFieldNode.class, "xOffset"), 0D);
		Assert.assertEquals(20, field.getCursorPosition());
	}

	@Test
	public void scrollsBackOnceItsCursorReachesItsStartMargin() {
		final TextFieldNode field = this.field("abcdefghijklmnopqrst");
		this.press(field, Key.END);
		for (int i = 0; i < 17; i++) {
			this.press(field, Key.LEFT);
		}
		Assert.assertEquals(15D, TextFieldNodeTest.<Double>read(field, LineFieldNode.class, "xOffset"), 0D);
		this.bridges.frame();
		Assert.assertEquals(117D, this.cursor().getLeft(), 0.001D);
		this.press(field, Key.LEFT);
		this.press(field, Key.LEFT);
		this.bridges.frame();
		Assert.assertEquals(0D, TextFieldNodeTest.<Double>read(field, LineFieldNode.class, "xOffset"), 0D);
		Assert.assertEquals(1, field.getCursorPosition());
	}

	@Test
	public void scrollsBackOnceItsTextShrinks() {
		final TextFieldNode field = this.field("abcdefghijklmnopqrst");
		this.press(field, Key.END);
		field.cursorPosition(1).text("a");
		this.bridges.frame();
		Assert.assertEquals(0D, TextFieldNodeTest.<Double>read(field, LineFieldNode.class, "xOffset"), 0D);
		Assert.assertEquals(102D, this.font.last().getX(), 0D);
	}

	@Test
	public void keepsItsCursorAndSelectionAtTheirDistanceFromTheEndOfARewrittenText() {
		final TextFieldNode field = this.selected(6, 5);
		field.text("ab");
		this.bridges.frame();
		Assert.assertEquals(1, field.getCursorPosition());
		Assert.assertEquals(2, TextFieldNodeTest.anchor(field));
	}

	@Test
	public void keepsItsSelectionWhenItsAcceptRefusesTheDeletion() {
		final TextFieldNode field = this.selected(2, 3).accept(text -> text.length() > 5);
		this.press(field, Key.BACKSPACE);
		this.bridges.frame();
		Assert.assertEquals("abcdef", field.getText());
		Assert.assertEquals(3, field.getCursorPosition());
		Assert.assertEquals(2, TextFieldNodeTest.anchor(field));
	}

	@Test
	public void keepsDrawingOnceAScrolledTextIsCleared() {
		final TextFieldNode field = this.field("abcdefghijklmnopqrst").<TextFieldNode>placeholder("Message").onSubmit(event -> event.getNode().text(""));
		this.press(field, Key.END);
		this.bridges.frame();
		this.press(field, Key.ENTER);
		this.bridges.frame();
		Assert.assertEquals("Message", this.font.last().getText());
		Assert.assertEquals(0D, TextFieldNodeTest.<Double>read(field, LineFieldNode.class, "xOffset"), 0D);
	}

	@Test
	public void typesIntoAFieldItsSubmitClearedAndFocusedAgain() {
		final TextFieldNode field = this.field("hello").<TextFieldNode>cursorPosition(5).onSubmit(event -> event.getNode().text("").focused(true));
		this.press(field, Key.ENTER);
		Assert.assertEquals(0, field.getCursorPosition());
		this.type(field, "x");
		this.press(field, Key.BACKSPACE);
		this.type(field, "ok");
		Assert.assertEquals("ok", field.getText());
	}

	@Test
	public void putsTheCursorUnderAClickInsideAParent() {
		final RectNode wrapper = RectNode.create(40D, 40D, 400D, 40D);
		final TextFieldNode field = TextFieldNode.create(8D, 0D, 384D).info(this.info()).text("hello").attach(wrapper);
		this.bridges.open(new NodeUI(wrapper));
		this.bridges.move(74D, 60D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertTrue(field.isFocused());
		Assert.assertEquals(2, field.getCursorPosition());
	}

	@Test
	public void selectsEverythingWithTheRightControlKey() {
		final TextFieldNode field = this.field("hello").cursorPosition(2);
		this.bridges.getWindow().getKeys().add(Key.RIGHT_CONTROL);
		field.keyPressed(NodeKeyEvent.create(field, Key.A, Key.A, null));
		Assert.assertEquals("hello", field.getText());
		Assert.assertEquals(0, TextFieldNodeTest.anchor(field));
		Assert.assertEquals(5, field.getCursorPosition());
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
		Assert.assertEquals(1, field.getCursorPosition());
	}

	@Test
	public void reportsASingleChangeWhenTypingOverASelection() {
		final List<String> changes = new ArrayList<>();
		final TextFieldNode field = this.field("ab").<TextFieldNode>accept(text -> !text.isEmpty()).onChange(event -> changes.add(event.getText()));
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.press(field, Key.A);
		this.bridges.getWindow().getKeys().remove(Key.LEFT_CONTROL);
		field.charTyped(NodeCharEvent.create(field, 'c', null));
		Assert.assertEquals("c", field.getText());
		Assert.assertEquals(Collections.singletonList("c"), changes);
	}

	@Test
	public void refusesWhatIsTypedOnceFull() {
		final TextFieldNode field = this.field("abc").maxTextLength(3).cursorPosition(1);
		field.charTyped(NodeCharEvent.create(field, 'x', null));
		Assert.assertEquals("abc", field.getText());
	}

	@Test
	public void showsTheCursorAtTheEndOfAnEndAlignedText() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).<TextFieldNode>text("abcd").alignX(Align.END).focused(true).cursorPosition(4);
		this.bridges.open(new NodeUI(field));
		final Draw mask = this.bridges.getRender().getDraws(1F, 0F, 0F).get(0);
		final Draw cursor = this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F).get(0);
		Assert.assertTrue(cursor.getRight() + " > " + mask.getRight(), cursor.getRight() <= mask.getRight() + 0.001D);
	}

	@Test
	public void keepsTheCursorOfACenteredTextInView() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).<TextFieldNode>text("abcdefghijklmnopqrstuvwxyzabcd").alignX(Align.CENTER).focused(true);
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
		Assert.assertEquals(2, TextFieldNodeTest.anchor(field));
		Assert.assertEquals(6, field.getCursorPosition());
		this.press(field, Key.HOME);
		Assert.assertEquals(2, TextFieldNodeTest.anchor(field));
		Assert.assertEquals(0, field.getCursorPosition());
		this.bridges.getWindow().getKeys().remove(Key.LEFT_SHIFT);
		this.press(field, Key.END);
		Assert.assertEquals(-1, TextFieldNodeTest.anchor(field));
	}

	@Test
	public void dropsItsSelectionOnHomeAndEndWithoutShift() {
		final TextFieldNode field = this.selected(1, 3);
		this.press(field, Key.END);
		Assert.assertEquals(-1, TextFieldNodeTest.anchor(field));
		Assert.assertEquals(6, field.getCursorPosition());
		this.control(field, Key.A);
		this.press(field, Key.HOME);
		Assert.assertEquals(-1, TextFieldNodeTest.anchor(field));
		Assert.assertEquals(0, field.getCursorPosition());
	}

	@Test
	public void dropsItsSelectionOnceUnfocused() {
		final TextFieldNode field = this.selected(1, 3);
		field.focused(false);
		Assert.assertEquals(-1, TextFieldNodeTest.anchor(field));
		field.focused(true);
		this.control(field, Key.A);
		this.press(field, Key.ENTER);
		Assert.assertEquals(-1, TextFieldNodeTest.anchor(field));
	}

	@Test
	public void dropsTheControlCharactersOfEveryText() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).text("a\u00a7b\tc\u20ac\nd");
		Assert.assertEquals("a\u00a7bc\u20acd", field.getText());
		Assert.assertEquals("xy\u20ac", field.normalize(text -> "x\ty\u20ac").text("z").getText());
	}

	@Test
	public void drawsItsTextWithoutMarkupUnlessAllowed() {
		final List<TextInfo> infos = new ArrayList<>();
		final ITextRenderer renderer = new ITextRenderer() {

			@Override
			public FontBounds drawText(final double x, final double y, final String text, final TextInfo info) {
				infos.add(info);
				return FontBounds.of(this.getWidth(text, info), this.getHeight(text, info));
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
		final IFont font = () -> renderer;
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
			Assert.assertEquals(-1, TextFieldNodeTest.anchor(field));
			this.bridges.getWindow().getKeys().add(Key.LEFT_SUPER);
			this.press(field, Key.A);
			this.bridges.getWindow().getKeys().remove(Key.LEFT_SUPER);
			Assert.assertEquals(0, TextFieldNodeTest.anchor(field));
			this.press(field, Key.END);
			this.bridges.getWindow().getKeys().add(Key.LEFT_ALT);
			this.press(field, Key.LEFT);
			this.bridges.getWindow().getKeys().remove(Key.LEFT_ALT);
			Assert.assertEquals(4, field.getCursorPosition());
		} finally {
			System.setProperty("os.name", system);
		}
	}

	@Test
	public void bindsItsTextToASignalBothWays() {
		final List<String> changes = new ArrayList<>();
		final Signal<String> signal = new Signal<>("hi");
		final TextFieldNode field = this.field("").onChange(event -> changes.add(event.getText())).text(signal);
		Assert.assertEquals("hi", field.getText());
		this.type(field.cursorPosition(2), "!");
		Assert.assertEquals("hi!", signal.get());
		signal.set("yo");
		Assert.assertEquals("yo", field.getText());
		signal.set("a	b");
		Assert.assertEquals("ab", field.getText());
		Assert.assertEquals("ab", signal.get());
		field.text("cd");
		Assert.assertEquals("ab", signal.get());
		signal.set("ef");
		Assert.assertEquals("cd", field.getText());
		Assert.assertEquals(Arrays.asList("hi", "hi!", "yo", "ab", "cd"), changes);
	}

	@Test
	public void keepsAChangeToItsOwnTextMadeAfterAWriteOfItsSignalItNeverSaw() {
		final Signal<String> signal = new Signal<>("abc");
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).text(signal);
		final NodeUI ui = new NodeUI(field);
		this.bridges.open(ui).frame();
		ui.remove(field);
		signal.set("zzz");
		field.change("abc");
		ui.add(field);
		this.bridges.frames(2);
		Assert.assertEquals("abc", field.getText());
		Assert.assertEquals("abc", signal.peek());
	}

	@Test
	public void keepsAChangeToItsOwnTextMadeWhileFocusedAfterAWriteOfItsSignalItNeverSaw() {
		final Signal<String> signal = new Signal<>("abc");
		final TextFieldNode field = this.field("").text(signal);
		this.bridges.frame();
		signal.silent(value -> value.set("zzz"));
		field.change("abc");
		this.bridges.frames(2);
		Assert.assertEquals("abc", field.getText());
		Assert.assertEquals("abc", signal.peek());
	}

	@Test
	public void followsOnlyItsLastSignal() {
		final Signal<String> name = new Signal<>("Alex");
		final Signal<String> nickname = new Signal<>("Al");
		final TextFieldNode field = this.field("").text(name).text(nickname);
		Assert.assertEquals("Al", field.getText());
		this.type(field.cursorPosition(2), "ex");
		Assert.assertEquals("Alex", nickname.get());
		name.set("Max");
		Assert.assertEquals("Alex", field.getText());
		Assert.assertTrue(TextFieldNodeTest.subscribers(name).isEmpty());
		Assert.assertEquals(1, TextFieldNodeTest.subscribers(nickname).size());
	}

	@Test
	public void keepsTheCursorTheListenerOfItsTextPlacesWhileTyping() {
		final Signal<String> text = new Signal<>("hello");
		final Signal<Integer> cursor = new Signal<>(2);
		final TextFieldNode field = this.field("").text(text).cursorPosition(cursor);
		this.bridges.frame();
		text.onChange(value -> cursor.set(value.length()));
		Assert.assertTrue(this.bridges.getUi().charTyped('x'));
		Assert.assertEquals("hexllo", field.getText());
		Assert.assertEquals(6, field.getCursorPosition());
		Assert.assertEquals(6, cursor.peek().intValue());
		text.onChange(value -> cursor.set(0));
		cursor.set(2);
		this.type(field, "y");
		Assert.assertEquals("heyxllo", field.getText());
		Assert.assertEquals(0, field.getCursorPosition());
		Assert.assertEquals(0, cursor.peek().intValue());
	}

	@Test
	public void keepsTheSelectionTheListenerOfItsTextPlacesWhileTyping() {
		final Signal<String> text = new Signal<>("hello");
		final Signal<FieldSelection> selection = new Signal<>(FieldSelection.of(0, 0));
		final TextFieldNode field = this.field("").text(text).cursorPosition(2).selection(selection);
		this.bridges.frame();
		text.onChange(value -> selection.set(FieldSelection.of(0, value.length())));
		Assert.assertTrue(this.bridges.getUi().charTyped('x'));
		Assert.assertEquals("hexllo", field.getText());
		Assert.assertEquals(FieldSelection.of(0, 6), field.getSelection());
		Assert.assertEquals(FieldSelection.of(0, 6), selection.peek());
		Assert.assertEquals(6, field.getCursorPosition());
	}

	@Test
	public void keepsTheCursorTheListenerOfItsTextPlacesWhileErasing() {
		final Signal<String> text = new Signal<>("hello world");
		final Signal<Integer> cursor = new Signal<>(8);
		final TextFieldNode field = this.field("").text(text).cursorPosition(cursor);
		this.bridges.frame();
		text.onChange(value -> cursor.set(value.length()));
		this.bridges.getWindow().getKeys().add(Key.BACKSPACE);
		this.bridges.getUi().keyPressed(Key.BACKSPACE, Key.BACKSPACE);
		Assert.assertEquals("hello wrld", field.getText());
		Assert.assertEquals(10, field.getCursorPosition());
		Assert.assertEquals(10, cursor.peek().intValue());
		this.elapse(500L);
		Assert.assertEquals("hello wrl", field.getText());
		Assert.assertEquals(9, field.getCursorPosition());
		Assert.assertEquals(9, cursor.peek().intValue());
	}

	@Test
	public void formatsWhatIsTypedThroughTheListenerOfItsText() {
		final Signal<String> text = new Signal<>("");
		final Signal<Integer> cursor = new Signal<>(0);
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 400D).info(this.info()).text(text).cursorPosition(cursor).focused(true);
		this.bridges.open(new NodeUI(field)).frame();
		text.onChange(value -> {
			final String digits = value.replace(" ", "");
			final String grouped = digits.length() > 4 ? digits.substring(0, 4) + " " + digits.substring(4) : digits;
			if (!grouped.equals(value)) {
				text.set(grouped);
				cursor.set(grouped.length());
			}
		});
		for (final char digit : "12345678".toCharArray()) {
			this.bridges.getUi().charTyped(digit);
			this.bridges.frame();
		}
		Assert.assertEquals("1234 5678", field.getText());
		Assert.assertEquals(9, field.getCursorPosition());
		Assert.assertEquals(9, cursor.peek().intValue());
	}

	@Test
	public void formatsAChangeThroughTheListenerOfItsText() {
		final Signal<String> text = new Signal<>("");
		final Signal<Integer> cursor = new Signal<>(0);
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 400D).info(this.info()).text(text).cursorPosition(cursor).focused(true);
		this.bridges.open(new NodeUI(field)).frame();
		text.onChange(value -> {
			final String digits = value.replace(" ", "");
			final String grouped = digits.length() > 4 ? digits.substring(0, 4) + " " + digits.substring(4) : digits;
			if (!grouped.equals(value)) {
				text.set(grouped);
				cursor.set(grouped.length());
			}
		});
		field.change("12345678");
		Assert.assertEquals("1234 5678", field.getText());
		Assert.assertEquals(9, field.getCursorPosition());
		Assert.assertEquals(9, cursor.peek().intValue());
	}

	@Test
	public void keepsTheCursorTheListenerOfItsFocusPlacesOnABlur() {
		final Signal<String> text = new Signal<>("hello  ");
		final Signal<Integer> cursor = new Signal<>(7);
		final Signal<Boolean> focused = new Signal<>(false);
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(this.info()).text(text).cursorPosition(cursor).focused(focused);
		this.bridges.open(new NodeUI(field)).frame();
		field.focus().moveCursor(7);
		this.bridges.frame();
		focused.onChange(value -> {
			if (!value) {
				text.set(text.peek().trim());
				cursor.set(0);
			}
		});
		this.click(600D, 400D);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertFalse(field.isFocused());
		Assert.assertEquals("hello", field.getText());
		Assert.assertEquals(0, field.getCursorPosition());
		Assert.assertEquals(0, cursor.peek().intValue());
		text.set("world  ");
		field.focus().moveCursor(7);
		this.bridges.frame();
		field.blur();
		Assert.assertEquals("world", field.getText());
		Assert.assertEquals(0, field.getCursorPosition());
		Assert.assertEquals(0, cursor.peek().intValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesASignalItCannotWrite() {
		TextFieldNode.create(100D, 100D, 200D).text(new Signal<>("Alex").map(String::toUpperCase));
	}

	@Test
	public void readsItsTextAsItsValue() {
		final TextFieldNode field = this.field("abc");
		Assert.assertEquals("abc", field.getValue());
		Assert.assertEquals("", field.text("").getValue());
	}

	private TextInfo info() {
		return TextInfo.create(this.font, 10F).color(TextFieldNodeTest.INK);
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
		field.keyPressed(NodeKeyEvent.create(field, key, key, null));
	}

	private void control(final TextFieldNode field, final Key key) {
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.press(field, key);
		this.bridges.getWindow().getKeys().remove(Key.LEFT_CONTROL);
	}

	private void type(final TextFieldNode field, final String text) {
		for (final char c : text.toCharArray()) {
			field.charTyped(NodeCharEvent.create(field, c, null));
		}
	}

	private void click(final double x, final double y) {
		this.bridges.move(x, y).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
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

	public static int anchor(final FieldNode<?> field) {
		final FieldSelection selection = field.getSelection();
		if (selection.isEmpty()) {
			return -1;
		}

		return field.getCursorPosition() == selection.getStart() ? selection.getEnd() : selection.getStart();
	}

	private static Set<?> subscribers(final Signal<?> signal) {
		try {
			final Field field = Signal.class.getDeclaredField("listenerSet");
			field.setAccessible(true);
			return (Set<?>) field.get(signal);
		} catch (final ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

	private static <T> T read(final Object field, final Class<?> owner, final String name) {
		try {
			final Field declared = owner.getDeclaredField(name);
			declared.setAccessible(true);
			return (T) declared.get(field);
		} catch (final ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

	@UIData(closeable = false)
	@UIDataDebug(profiler = false, hotreload = false)
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

	public static final class FieldFont implements IFont, ITextRenderer {

		private final List<Drawn> drawn = new ArrayList<>();

		@Override
		public ITextRenderer getTextRenderer() {
			return this;
		}

		@Override
		public FontBounds drawText(final double x, final double y, final String text, final TextInfo info) {
			this.drawn.add(new Drawn(text, x, y, info.getColor().getAlphaF()));
			return FontBounds.of(this.getWidth(text, info), this.getHeight(text, info));
		}

		@Override
		public double getLineHeight(final TextInfo info) {
			return 20D;
		}

		@Override
		public double getWidth(final String text, final TextInfo info) {
			return text.length() * 10D;
		}

		@Override
		public double getHeight(final String text, final TextInfo info) {
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