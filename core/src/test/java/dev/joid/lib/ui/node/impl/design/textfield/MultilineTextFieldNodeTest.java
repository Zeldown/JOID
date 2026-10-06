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
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.structure.container.ContainerNode;
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.key.Key;
import dev.joid.lib.utils.signal.Signal;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class MultilineTextFieldNodeTest {

	private static final Color INK = new Color(0.2F, 0.4F, 0.6F, 1F);

	private static final IFont FONT = () -> MultilineTextFieldNodeTest.PROVIDER;

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

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private final FieldFont font = new FieldFont();

	@Test
	public void pastesAWindowsLineEndingAsOneLineBreak() {
		final FieldUI ui = new FieldUI("");
		this.bridges.open(ui);
		this.bridges.getWindow().setClipboard("ab\r\ncd");
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		ui.field.keyPressed('v', Key.V, InternalContext.create());
		Assert.assertEquals("ab\ncd", ui.field.getText());
	}

	@Test
	public void storesAWindowsLineEndingAsOneLineBreak() {
		final FieldUI ui = new FieldUI("ab\r\ncd\ref");
		this.bridges.open(ui);
		Assert.assertEquals("ab\ncd\nef", ui.field.getText());
		ui.field.cursorPosition(1);
		ui.field.keyPressed(' ', Key.DOWN, InternalContext.create());
		Assert.assertEquals(4, ui.field.getCursorPos());
	}

	@Test
	public void startsEmptyAndUnfocused() {
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, 400D, 200D);
		Assert.assertEquals("", field.getText());
		Assert.assertEquals("", field.getPlaceholder());
		Assert.assertFalse(field.isFocused());
		Assert.assertEquals(-1, field.getMaxTextLength());
		Assert.assertEquals(0, field.getCursorPos());
		Assert.assertEquals(-1, field.getSelectionStart());
		Assert.assertEquals("abc", field.getFilter().apply("x", "abc"));
		Assert.assertEquals(0D, field.getYOffset(), 0D);
	}

	@Test
	public void startsWithTheDocumentedMargins() {
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, 400D, 200D);
		Assert.assertEquals(2D, field.getMarginTop(), 0D);
		Assert.assertEquals(2D, field.getMarginLeft(), 0D);
		Assert.assertEquals(2D, field.getMarginRight(), 0D);
		Assert.assertEquals(2D, field.getMarginBottom(), 0D);
		Assert.assertEquals(-1D, field.getCursorMargin(), 0D);
	}

	@Test
	public void setsItsMarginsByAxis() {
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, 400D, 200D).marginHorizontal(3D).marginVertical(5D);
		Assert.assertEquals(3D, field.getMarginLeft(), 0D);
		Assert.assertEquals(3D, field.getMarginRight(), 0D);
		Assert.assertEquals(5D, field.getMarginTop(), 0D);
		Assert.assertEquals(5D, field.getMarginBottom(), 0D);
	}

	@Test
	public void turnsItsDefaultCursorMarginIntoTwoLinesOnTheFirstDraw() {
		final MultilineTextFieldNode given = MultilineTextFieldNode.create(0D, 300D, 400D, 200D).info(TextInfo.create(this.font, 10F)).cursorMargin(5D);
		this.bridges.open(new NodeUI(given));
		Assert.assertEquals(40D, this.field("").getCursorMargin(), 0D);
		Assert.assertEquals(5D, given.getCursorMargin(), 0D);
	}

	@Test
	public void setsEveryMarginAtOnce() {
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, 400D, 200D);
		Assert.assertSame(field, field.margin(3D));
		Assert.assertEquals(3D, field.getMarginTop(), 0D);
		Assert.assertEquals(3D, field.getMarginLeft(), 0D);
		Assert.assertEquals(3D, field.getMarginRight(), 0D);
		Assert.assertEquals(3D, field.getMarginBottom(), 0D);
		Assert.assertEquals(-1D, field.getCursorMargin(), 0D);
		Assert.assertSame(field, field.margin(4D, 6D));
		Assert.assertEquals(4D, field.getMarginTop(), 0D);
		Assert.assertEquals(4D, field.getMarginLeft(), 0D);
		Assert.assertEquals(4D, field.getMarginRight(), 0D);
		Assert.assertEquals(4D, field.getMarginBottom(), 0D);
		Assert.assertEquals(6D, field.getCursorMargin(), 0D);
	}

	@Test
	public void setsEachMarginOnItsOwn() {
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, 400D, 200D).marginTop(1D).marginLeft(2D).marginRight(3D).marginBottom(4D).cursorMargin(5D);
		Assert.assertEquals(1D, field.getMarginTop(), 0D);
		Assert.assertEquals(2D, field.getMarginLeft(), 0D);
		Assert.assertEquals(3D, field.getMarginRight(), 0D);
		Assert.assertEquals(4D, field.getMarginBottom(), 0D);
		Assert.assertEquals(5D, field.getCursorMargin(), 0D);
	}

	@Test
	public void takesItsPlaceholderAndItsInfo() {
		final TextInfo info = TextInfo.create(this.font, 10F);
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, 400D, 200D);
		Assert.assertSame(field, field.placeholder("Start typing"));
		Assert.assertSame(field, field.info(info));
		Assert.assertEquals("Start typing", field.getPlaceholder());
		Assert.assertSame(info, field.getInfo());
	}

	@Test
	public void keepsTheCursorPositionInsideTheText() {
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, 400D, 200D).text("ab\nc");
		Assert.assertEquals(2, field.cursorPosition(2).getCursorPos());
		Assert.assertEquals(0, field.cursorPosition(-3).getCursorPos());
		Assert.assertEquals(4, field.cursorPosition(9).getCursorPos());
	}

	@Test
	public void reportsEachChangeOfItsText() {
		final List<Object> changes = new ArrayList<>();
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, 400D, 200D).onChange((node, oldText, newText) -> changes.addAll(Arrays.asList(node, oldText, newText)));
		field.text("a\nb").text("a\nb");
		Assert.assertEquals(Arrays.asList(field, "", "a\nb"), changes);
	}

	@Test
	public void passesEveryNewTextThroughItsFilter() {
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, 400D, 200D).filter((oldText, newText) -> oldText + newText.toUpperCase());
		Assert.assertEquals("AB", field.text("ab").getText());
		Assert.assertEquals("ABCD", field.text("cd").getText());
	}

	@Test
	public void cutsATextLongerThanItsMaximumLength() {
		Assert.assertEquals("ab\n", MultilineTextFieldNode.create(0D, 0D, 400D, 200D).maxTextLength(3).text("ab\ncd").getText());
		Assert.assertEquals("", MultilineTextFieldNode.create(0D, 0D, 400D, 200D).maxTextLength(0).text("ab").getText());
		Assert.assertEquals("ab\ncd", MultilineTextFieldNode.create(0D, 0D, 400D, 200D).maxTextLength(-1).text("ab\ncd").getText());
	}

	@Test
	public void reportsOnlyARealChangeOfFocus() {
		final List<Boolean> focuses = new ArrayList<>();
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, 400D, 200D).onFocus(node -> focuses.add(node.isFocused()));
		field.focused(true).focused(true).focused(false);
		Assert.assertEquals(Arrays.asList(true, false), focuses);
	}

	@Test
	public void insertsALineBreakOnEnter() {
		final MultilineTextFieldNode field = this.field("ab").cursorPosition(1);
		this.press(field, Key.ENTER);
		Assert.assertEquals("a\nb", field.getText());
		Assert.assertEquals(2, field.getCursorPos());
		this.press(field, Key.NUMPAD_ENTER);
		Assert.assertEquals("a\n\nb", field.getText());
		Assert.assertTrue(field.isFocused());
	}

	@Test
	public void unfocusesOnEscape() {
		final MultilineTextFieldNode field = this.field("ab");
		this.press(field, Key.ESCAPE);
		Assert.assertFalse(field.isFocused());
		Assert.assertEquals("ab", field.getText());
	}

	@Test
	public void insertsWhatIsTypedAtTheCursor() {
		final MultilineTextFieldNode field = this.field("hllo").cursorPosition(1);
		this.type(field, "e");
		Assert.assertEquals("hello", field.getText());
		Assert.assertEquals(2, field.getCursorPos());
	}

	@Test
	public void turnsATypedCarriageReturnIntoALineBreak() {
		final MultilineTextFieldNode field = this.field("");
		this.type(field, "a\rb\tc\u0007\u007f€");
		Assert.assertEquals("a\nbc", field.getText());
	}

	@Test
	public void replacesTheSelectionWithWhatIsTyped() {
		final MultilineTextFieldNode field = this.field("ab\ncd");
		this.control(field, Key.A);
		this.type(field, "x");
		Assert.assertEquals("x", field.getText());
		Assert.assertEquals(1, field.getCursorPos());
		Assert.assertEquals(-1, field.getSelectionStart());
	}

	@Test
	public void replacesABackwardSelectionWithWhatIsTyped() {
		final MultilineTextFieldNode field = this.selected("ab\ncd", 5, 1);
		this.type(field, "x");
		Assert.assertEquals("ax", field.getText());
		Assert.assertEquals(2, field.getCursorPos());
	}

	@Test
	public void movesItsCursorAcrossLinesWithTheArrows() {
		final MultilineTextFieldNode field = this.field("ab\ncd").cursorPosition(2);
		this.press(field, Key.RIGHT);
		Assert.assertEquals(3, field.getCursorPos());
		for (int i = 0; i < 4; i++) {
			this.press(field, Key.LEFT);
		}
		Assert.assertEquals(0, field.getCursorPos());
		for (int i = 0; i < 9; i++) {
			this.press(field, Key.RIGHT);
		}
		Assert.assertEquals(5, field.getCursorPos());
	}

	@Test
	public void jumpsOverWholeWordsAndLineBreaksWithControl() {
		final MultilineTextFieldNode field = this.field("one\ntwo three");
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.press(field, Key.RIGHT);
		Assert.assertEquals(4, field.getCursorPos());
		this.press(field, Key.RIGHT);
		Assert.assertEquals(8, field.getCursorPos());
		this.press(field, Key.RIGHT);
		Assert.assertEquals(13, field.getCursorPos());
		this.press(field, Key.LEFT);
		Assert.assertEquals(8, field.getCursorPos());
		this.press(field, Key.LEFT);
		Assert.assertEquals(4, field.getCursorPos());
		this.press(field, Key.LEFT);
		Assert.assertEquals(0, field.getCursorPos());
	}

	@Test
	public void selectsWithShiftAndTheArrows() {
		final MultilineTextFieldNode field = this.field("ab\ncd").cursorPosition(1);
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
		final MultilineTextFieldNode field = this.selected("ab\ncd", 1, 2);
		this.press(field, Key.LEFT);
		Assert.assertEquals(-1, field.getSelectionStart());
		Assert.assertEquals(1, field.getCursorPos());
	}

	@Test
	public void movesDownToTheEndOfAShorterLine() {
		final MultilineTextFieldNode field = this.field("abcd\nab").cursorPosition(4);
		this.press(field, Key.DOWN);
		Assert.assertEquals(7, field.getCursorPos());
		this.press(field, Key.DOWN);
		Assert.assertEquals(7, field.getCursorPos());
	}

	@Test
	public void movesUpToTheEndOfAShorterLine() {
		final MultilineTextFieldNode field = this.field("ab\nabcd").cursorPosition(7);
		this.press(field, Key.UP);
		Assert.assertEquals(2, field.getCursorPos());
		this.press(field, Key.UP);
		Assert.assertEquals(2, field.getCursorPos());
	}

	@Test
	public void selectsWithShiftAndTheVerticalArrows() {
		final MultilineTextFieldNode down = this.field("abcd\nab").cursorPosition(4);
		this.bridges.getWindow().getKeys().add(Key.LEFT_SHIFT);
		this.press(down, Key.DOWN);
		Assert.assertEquals(4, down.getSelectionStart());
		Assert.assertEquals(7, down.getCursorPos());
		this.bridges.getWindow().getKeys().remove(Key.LEFT_SHIFT);
		final MultilineTextFieldNode up = this.field("ab\nabcd").cursorPosition(7);
		this.bridges.getWindow().getKeys().add(Key.RIGHT_SHIFT);
		this.press(up, Key.UP);
		Assert.assertEquals(7, up.getSelectionStart());
		Assert.assertEquals(2, up.getCursorPos());
	}

	@Test
	public void dropsItsSelectionOnAVerticalArrowWithoutShift() {
		final MultilineTextFieldNode down = this.selected("ab\nab\nab", 0, 1);
		this.press(down, Key.DOWN);
		Assert.assertEquals(-1, down.getSelectionStart());
		final MultilineTextFieldNode up = this.selected("ab\nab\nab", 7, 6);
		this.press(up, Key.UP);
		Assert.assertEquals(-1, up.getSelectionStart());
	}

	@Test
	public void reachesBothEndsOfTheTextWithHomeAndEnd() {
		final MultilineTextFieldNode field = this.field("ab\ncd").cursorPosition(1);
		this.press(field, Key.END);
		Assert.assertEquals(5, field.getCursorPos());
		this.press(field, Key.HOME);
		Assert.assertEquals(0, field.getCursorPos());
	}

	@Test
	public void erasesTheCharacterBeforeTheCursorWithBackspace() {
		final MultilineTextFieldNode field = this.field("ab\ncd").cursorPosition(3);
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("abcd", field.getText());
		Assert.assertEquals(2, field.getCursorPos());
		this.press(field, Key.HOME);
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("abcd", field.getText());
	}

	@Test
	public void erasesTheWordBeforeTheCursorWithControlBackspace() {
		final MultilineTextFieldNode field = this.field("one\ntwo").cursorPosition(4);
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("two", field.getText());
		Assert.assertEquals(0, field.getCursorPos());
	}

	@Test
	public void erasesTheSelectionWithBackspace() {
		final MultilineTextFieldNode forward = this.selected("ab\ncd", 1, 4);
		this.press(forward, Key.BACKSPACE);
		Assert.assertEquals("ad", forward.getText());
		Assert.assertEquals(1, forward.getCursorPos());
		Assert.assertEquals(-1, forward.getSelectionStart());
		final MultilineTextFieldNode backward = this.selected("ab\ncd", 4, 1);
		this.press(backward, Key.BACKSPACE);
		Assert.assertEquals("ad", backward.getText());
		Assert.assertEquals(1, backward.getCursorPos());
	}

	@Test
	public void erasesTheCharacterAfterTheCursorWithDelete() {
		final MultilineTextFieldNode field = this.field("ab\ncd").cursorPosition(2);
		this.press(field, Key.DELETE);
		Assert.assertEquals("abcd", field.getText());
		Assert.assertEquals(2, field.getCursorPos());
		this.press(field, Key.END);
		this.press(field, Key.DELETE);
		Assert.assertEquals("abcd", field.getText());
	}

	@Test
	public void erasesTheWordAfterTheCursorWithControlDelete() {
		final MultilineTextFieldNode field = this.field("one\ntwo");
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.press(field, Key.DELETE);
		Assert.assertEquals("two", field.getText());
	}

	@Test
	public void erasesTheSelectionWithDelete() {
		final MultilineTextFieldNode forward = this.selected("ab\ncd", 1, 4);
		this.press(forward, Key.DELETE);
		Assert.assertEquals("ad", forward.getText());
		final MultilineTextFieldNode backward = this.selected("ab\ncd", 4, 1);
		this.press(backward, Key.DELETE);
		Assert.assertEquals("ad", backward.getText());
		Assert.assertEquals(1, backward.getCursorPos());
		Assert.assertEquals(-1, backward.getSelectionStart());
	}

	@Test
	public void selectsEverythingWithControlA() {
		final MultilineTextFieldNode field = this.field("ab\ncd").cursorPosition(2);
		this.control(field, Key.A);
		Assert.assertEquals(0, field.getSelectionStart());
		Assert.assertEquals(5, field.getCursorPos());
	}

	@Test
	public void copiesTheSelectionWithControlC() {
		final MultilineTextFieldNode forward = this.selected("ab\ncd", 1, 4);
		this.control(forward, Key.C);
		Assert.assertEquals("b\nc", this.bridges.getWindow().getClipboard());
		Assert.assertEquals("ab\ncd", forward.getText());
		final MultilineTextFieldNode backward = this.selected("ab\ncd", 5, 2);
		this.control(backward, Key.C);
		Assert.assertEquals("\ncd", this.bridges.getWindow().getClipboard());
	}

	@Test
	public void keepsTheClipboardWithoutASelection() {
		final MultilineTextFieldNode field = this.field("ab\ncd");
		this.bridges.getWindow().setClipboard("clip");
		this.control(field, Key.C);
		this.control(field, Key.X);
		Assert.assertEquals("clip", this.bridges.getWindow().getClipboard());
		Assert.assertEquals("ab\ncd", field.getText());
	}

	@Test
	public void cutsTheSelectionWithControlX() {
		final MultilineTextFieldNode forward = this.selected("ab\ncd", 1, 4);
		this.control(forward, Key.X);
		Assert.assertEquals("b\nc", this.bridges.getWindow().getClipboard());
		Assert.assertEquals("ad", forward.getText());
		Assert.assertEquals(1, forward.getCursorPos());
		Assert.assertEquals(-1, forward.getSelectionStart());
		final MultilineTextFieldNode backward = this.selected("ab\ncd", 5, 2);
		this.control(backward, Key.X);
		Assert.assertEquals("\ncd", this.bridges.getWindow().getClipboard());
		Assert.assertEquals("ab", backward.getText());
		Assert.assertEquals(2, backward.getCursorPos());
	}

	@Test
	public void pastesTheClipboardAtTheCursor() {
		final MultilineTextFieldNode field = this.field("ab").cursorPosition(1);
		this.bridges.getWindow().setClipboard("x\ry\tz");
		this.control(field, Key.V);
		Assert.assertEquals("ax\nyzb", field.getText());
		Assert.assertEquals(5, field.getCursorPos());
	}

	@Test
	public void ignoresAnEmptyClipboard() {
		final List<String> changes = new ArrayList<>();
		final MultilineTextFieldNode field = this.field("ab").onChange((node, oldText, newText) -> changes.add(newText));
		this.control(field, Key.V);
		Assert.assertEquals("ab", field.getText());
		Assert.assertTrue(changes.isEmpty());
	}

	@Test
	public void receivesTheKeysTypedInItsUi() {
		final MultilineTextFieldNode field = this.field("ab").cursorPosition(2);
		this.bridges.getUi().keyTyped('c', Key.C);
		Assert.assertEquals("abc", field.getText());
	}

	@Test
	public void ignoresTheKeyboardWhileUnfocused() {
		final MultilineTextFieldNode field = this.field("ab").focused(false);
		final InternalContext context = InternalContext.create();
		field.keyPressed('x', Key.X, context);
		Assert.assertEquals("ab", field.getText());
		Assert.assertFalse(context.isCancelled());
	}

	@Test
	public void consumesTheKeysItReceives() {
		final InternalContext context = InternalContext.create();
		this.field("ab").keyPressed('x', Key.X, context);
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void leavesAKeyAlreadyTakenElsewhere() {
		final MultilineTextFieldNode field = this.field("ab");
		field.keyPressed('x', Key.X, InternalContext.create(true));
		Assert.assertEquals("ab", field.getText());
	}

	@Test
	public void focusesOnAClickAndPutsTheCursorUnderTheMouse() {
		final List<Boolean> focuses = new ArrayList<>();
		final MultilineTextFieldNode field = this.field("ab\ncd").focused(false).onFocus(node -> focuses.add(node.isFocused()));
		this.click(14D, 30D);
		Assert.assertTrue(field.isFocused());
		Assert.assertEquals(4, field.getCursorPos());
		Assert.assertEquals(Collections.singletonList(true), focuses);
	}

	@Test
	public void putsTheCursorAtTheEndOfTheLineOnAClickPastIt() {
		final MultilineTextFieldNode field = this.field("ab\ncd");
		this.click(100D, 10D);
		Assert.assertEquals(2, field.getCursorPos());
	}

	@Test
	public void putsTheCursorOnTheNearestLineOnAClickAboveOrBelowTheText() {
		final MultilineTextFieldNode field = this.field("ab\ncd");
		this.click(100D, 150D);
		Assert.assertEquals(5, field.getCursorPos());
		this.click(5D, 1D);
		Assert.assertEquals(0, field.getCursorPos());
	}

	@Test
	public void putsTheCursorUnderAClickOnAWrappedLine() {
		final MultilineTextFieldNode field = this.field(54D, "hello world");
		this.click(14D, 30D);
		Assert.assertEquals(7, field.getCursorPos());
	}

	@Test
	public void putsTheCursorUnderAClickAfterACarriageReturn() {
		final MultilineTextFieldNode field = this.field("ab\rcd");
		this.click(14D, 30D);
		Assert.assertEquals(4, field.getCursorPos());
	}

	@Test
	public void focusesAnEmptyFieldOnAClick() {
		final MultilineTextFieldNode field = this.field("").focused(false);
		this.click(100D, 100D);
		Assert.assertTrue(field.isFocused());
		Assert.assertEquals(0, field.getCursorPos());
	}

	@Test
	public void extendsItsSelectionOnAShiftClick() {
		final MultilineTextFieldNode field = this.field("ab\ncd");
		this.click(14D, 10D);
		this.bridges.getWindow().getKeys().add(Key.LEFT_SHIFT);
		this.click(14D, 30D);
		Assert.assertEquals(1, field.getSelectionStart());
		Assert.assertEquals(4, field.getCursorPos());
		this.bridges.getWindow().getKeys().remove(Key.LEFT_SHIFT);
		this.click(14D, 30D);
		Assert.assertEquals(-1, field.getSelectionStart());
	}

	@Test
	public void unfocusesAndDropsItsSelectionOnAClickBesideIt() {
		final MultilineTextFieldNode field = this.field("ab\ncd");
		this.control(field, Key.A);
		this.click(600D, 600D);
		Assert.assertFalse(field.isFocused());
		Assert.assertEquals(-1, field.getSelectionStart());
	}

	@Test
	public void unfocusesWhenTheClickWasAlreadyTaken() {
		final MultilineTextFieldNode field = this.field("ab\ncd");
		field.mousePressed(14D, 30D, ClickType.LEFT, InternalContext.create(true));
		Assert.assertFalse(field.isFocused());
	}

	@Test
	public void scrollsOneLinePerWheelTickWhileHovered() {
		final MultilineTextFieldNode field = this.field("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\na\nb\nc\nd\ne");
		this.bridges.move(10D, 10D).frames(2).scroll(-1);
		Assert.assertEquals(20D, field.getYOffset(), 0D);
		for (int i = 0; i < 5; i++) {
			this.bridges.scroll(-1);
		}
		Assert.assertEquals(104D, field.getYOffset(), 0D);
		this.bridges.scroll(1);
		Assert.assertEquals(84D, field.getYOffset(), 0D);
		for (int i = 0; i < 5; i++) {
			this.bridges.scroll(1);
		}
		Assert.assertEquals(0D, field.getYOffset(), 0D);
	}

	@Test
	public void ignoresTheWheelOutsideItself() {
		final MultilineTextFieldNode field = this.field("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\na\nb\nc\nd\ne");
		this.bridges.move(600D, 100D).frames(2).scroll(-1);
		Assert.assertEquals(0D, field.getYOffset(), 0D);
	}

	@Test
	public void scrollsUnderTheMouseWhileUnfocused() {
		final MultilineTextFieldNode field = this.field("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\na\nb\nc\nd\ne").focused(false);
		this.bridges.move(10D, 10D).frames(2).scroll(-1);
		Assert.assertEquals(20D, field.getYOffset(), 0D);
	}

	@Test
	public void leavesAWheelAlreadyTakenElsewhere() {
		final MultilineTextFieldNode field = this.field("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\na\nb\nc\nd\ne");
		field.mouseScroll(10D, 10D, -1, InternalContext.create(true));
		Assert.assertEquals(0D, field.getYOffset(), 0D);
	}

	@Test
	public void ignoresAWheelThatDoesNotTurn() {
		final MultilineTextFieldNode field = this.field("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\na\nb\nc\nd\ne");
		this.bridges.move(10D, 10D).frames(2).scroll(-1);
		field.mouseScroll(10D, 10D, 0, InternalContext.create());
		Assert.assertEquals(20D, field.getYOffset(), 0D);
	}

	@Test
	public void staysAtTheTopOfATextShorterThanItself() {
		final MultilineTextFieldNode field = this.field("ab");
		this.bridges.scroll(-1).frame();
		Assert.assertEquals(0D, field.getYOffset(), 0D);
	}

	@Test
	public void consumesTheWheelWhileItScrolls() {
		final MultilineTextFieldNode field = this.field("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\na\nb\nc\nd\ne");
		final InternalContext down = InternalContext.create();
		field.mouseScroll(10D, 10D, -1, down);
		Assert.assertTrue(down.isCancelled());
		Assert.assertEquals(20D, field.getYOffset(), 0D);
		final InternalContext up = InternalContext.create();
		field.mouseScroll(10D, 10D, 1, up);
		Assert.assertTrue(up.isCancelled());
		Assert.assertEquals(0D, field.getYOffset(), 0D);
	}

	@Test
	public void leavesTheWheelToItsParentAtItsTop() {
		final MultilineTextFieldNode field = this.field("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\na\nb\nc\nd\ne");
		final InternalContext context = InternalContext.create();
		field.mouseScroll(10D, 10D, 1, context);
		Assert.assertFalse(context.isCancelled());
		Assert.assertEquals(0D, field.getYOffset(), 0D);
	}

	@Test
	public void leavesTheWheelToItsParentAtItsBottom() {
		final MultilineTextFieldNode field = this.field("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\na\nb\nc\nd\ne");
		for (int i = 0; i < 6; i++) {
			field.mouseScroll(10D, 10D, -1, InternalContext.create());
		}
		Assert.assertEquals(104D, field.getYOffset(), 0D);
		final InternalContext down = InternalContext.create();
		field.mouseScroll(10D, 10D, -1, down);
		Assert.assertFalse(down.isCancelled());
		Assert.assertEquals(104D, field.getYOffset(), 0D);
		final InternalContext up = InternalContext.create();
		field.mouseScroll(10D, 10D, 1, up);
		Assert.assertTrue(up.isCancelled());
		Assert.assertEquals(84D, field.getYOffset(), 0D);
	}

	@Test
	public void leavesTheWheelToItsParentWithNothingToScroll() {
		final MultilineTextFieldNode field = this.field("ab");
		final InternalContext down = InternalContext.create();
		final InternalContext up = InternalContext.create();
		field.mouseScroll(10D, 10D, -1, down);
		field.mouseScroll(10D, 10D, 1, up);
		Assert.assertFalse(down.isCancelled());
		Assert.assertFalse(up.isCancelled());
	}

	@Test
	public void letsItsScrollingParentTakeTheWheelOnceAtItsLimit() {
		final ContainerNode box = ContainerNode.create(0D, 0D, 400D, 300D).overflow(OverflowProperty.SCROLL);
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, 400D, 200D).info(TextInfo.create(this.font, 10F, MultilineTextFieldNodeTest.INK)).text("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\na\nb").attach(box);
		RectNode.create(0D, 200D, 100D, 1000D).attach(box);
		this.bridges.open(new NodeUI(box)).frames(30);
		this.bridges.move(10D, 10D).frames(2).scroll(-1).scroll(-1).scroll(-1);
		Assert.assertEquals(44D, field.getYOffset(), 0D);
		Assert.assertEquals(0D, box.getTargetScrollY(), 0D);
		this.bridges.scroll(-1);
		Assert.assertEquals(44D, field.getYOffset(), 0D);
		Assert.assertTrue(box.getTargetScrollY() < 0D);
	}

	@Test
	public void scrollsToKeepItsCursorInView() {
		final MultilineTextFieldNode field = this.field("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\nA");
		this.press(field, Key.END);
		Assert.assertEquals(24D, field.getYOffset(), 0D);
		final List<Drawn> drawn = this.draw();
		Assert.assertEquals("0", drawn.get(0).getText());
		Assert.assertEquals(-22D, drawn.get(0).getY(), 0D);
		Assert.assertEquals(178D, this.cursor().getTop(), 0.001D);
		this.press(field, Key.HOME);
		Assert.assertEquals(0D, field.getYOffset(), 0D);
	}

	@Test
	public void scrollsBackOnceItsCursorReachesItsTopMargin() {
		final MultilineTextFieldNode field = this.field("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\nA");
		this.press(field, Key.END);
		for (int i = 0; i < 13; i++) {
			this.press(field, Key.LEFT);
		}
		Assert.assertEquals(24D, field.getYOffset(), 0D);
		this.press(field, Key.LEFT);
		Assert.assertEquals(20D, field.getYOffset(), 0D);
	}

	@Test
	public void scrollsBackToTheTopOnceItsTextShrinks() {
		final MultilineTextFieldNode field = this.field("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\na\nb\nc\nd\ne");
		this.bridges.scroll(-1).scroll(-1).scroll(-1);
		field.text("ab");
		final List<Drawn> drawn = this.draw();
		Assert.assertEquals(0D, field.getYOffset(), 0D);
		Assert.assertEquals(2D, drawn.get(0).getY(), 0D);
	}

	@Test
	public void bringsItsCursorAndSelectionBackInsideAShorterText() {
		final MultilineTextFieldNode field = this.selected("ab\ncd", 5, 4);
		field.text("ab");
		this.bridges.frame();
		Assert.assertEquals(2, field.getCursorPos());
		Assert.assertEquals(2, field.getSelectionStart());
	}

	@Test
	public void bringsItsCursorBackToTheStartOfAFilteredText() {
		final MultilineTextFieldNode field = this.selected("ab\ncd", 2, 3).filter((oldText, newText) -> newText.length() < oldText.length() ? "" : newText);
		this.press(field, Key.BACKSPACE);
		this.bridges.frame();
		Assert.assertEquals("", field.getText());
		Assert.assertEquals(0, field.getCursorPos());
	}

	@Test
	public void repeatsAHeldArrowAfterHalfASecond() {
		final MultilineTextFieldNode field = this.field("ab\ncd");
		this.bridges.getWindow().getKeys().add(Key.RIGHT);
		this.press(field, Key.RIGHT);
		Assert.assertTrue(field.isInputting());
		Assert.assertSame(Key.RIGHT, field.getInputType());
		this.elapse(484L);
		Assert.assertEquals(1, field.getCursorPos());
		this.elapse(16L);
		Assert.assertEquals(2, field.getCursorPos());
		this.elapse(100L);
		Assert.assertEquals(3, field.getCursorPos());
	}

	@Test
	public void repeatsAHeldLeftArrow() {
		final MultilineTextFieldNode field = this.field("ab\ncd").cursorPosition(5);
		this.bridges.getWindow().getKeys().add(Key.LEFT);
		this.press(field, Key.LEFT);
		this.elapse(500L);
		Assert.assertEquals(3, field.getCursorPos());
	}

	@Test
	public void stopsRepeatingOnceTheKeyIsReleased() {
		final MultilineTextFieldNode field = this.field("ab\ncd");
		this.press(field, Key.RIGHT);
		this.elapse(600L);
		Assert.assertFalse(field.isInputting());
		Assert.assertEquals(1, field.getCursorPos());
	}

	@Test
	public void repeatsAHeldBackspaceUntilTheTextIsEmpty() {
		final MultilineTextFieldNode field = this.field("a\nb").cursorPosition(3);
		this.bridges.getWindow().getKeys().add(Key.BACKSPACE);
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("a\n", field.getText());
		this.elapse(500L);
		Assert.assertEquals("a", field.getText());
		this.elapse(100L);
		Assert.assertEquals("", field.getText());
		this.elapse(100L);
		Assert.assertFalse(field.isInputting());
	}

	@Test
	public void repeatsAHeldDeleteUntilTheTextIsEmpty() {
		final MultilineTextFieldNode field = this.field("a\nb");
		this.bridges.getWindow().getKeys().add(Key.DELETE);
		this.press(field, Key.DELETE);
		Assert.assertEquals("\nb", field.getText());
		this.elapse(500L);
		Assert.assertEquals("b", field.getText());
		this.elapse(100L);
		Assert.assertEquals("", field.getText());
		this.elapse(100L);
		Assert.assertFalse(field.isInputting());
	}

	@Test
	public void repeatsWholeWordsWhileControlIsHeld() {
		final MultilineTextFieldNode field = this.field("one\ntwo three").cursorPosition(13);
		this.bridges.getWindow().getKeys().addAll(Arrays.asList(Key.BACKSPACE, Key.LEFT_CONTROL));
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("one\ntwo ", field.getText());
		this.elapse(500L);
		Assert.assertEquals("one\n", field.getText());
		this.bridges.getWindow().getKeys().remove(Key.BACKSPACE);
		this.elapse(100L);
		field.text("one\ntwo three").cursorPosition(0);
		this.bridges.getWindow().getKeys().add(Key.DELETE);
		this.press(field, Key.DELETE);
		this.elapse(500L);
		Assert.assertEquals("three", field.getText());
	}

	@Test
	public void repeatsWholeWordMovesWhileControlIsHeld() {
		final MultilineTextFieldNode field = this.field("one\ntwo three");
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
	public void wrapsItsTextAtTheLastSpaceThatFits() {
		this.field(54D, "hello world");
		final List<Drawn> drawn = this.draw();
		Assert.assertEquals(2, drawn.size());
		Assert.assertEquals("hello", drawn.get(0).getText());
		Assert.assertEquals(2D, drawn.get(0).getX(), 0D);
		Assert.assertEquals(2D, drawn.get(0).getY(), 0D);
		Assert.assertEquals("world", drawn.get(1).getText());
		Assert.assertEquals(2D, drawn.get(1).getX(), 0D);
		Assert.assertEquals(22D, drawn.get(1).getY(), 0D);
	}

	@Test
	public void drawsEachLineOfItsTextBelowThePrevious() {
		this.field("ab\ncd");
		final List<Drawn> drawn = this.draw();
		final Drawn last = drawn.get(drawn.size() - 1);
		Assert.assertEquals("ab", drawn.get(0).getText());
		Assert.assertEquals("cd", last.getText());
		Assert.assertEquals(22D, last.getY(), 0D);
		Assert.assertEquals(1F, last.getAlpha(), 0F);
	}

	@Test
	public void drawsItsPlaceholderAtHalfOpacityWhileEmpty() {
		final MultilineTextFieldNode field = this.field("").placeholder("Start typing").focused(false);
		final List<Drawn> drawn = this.draw();
		Assert.assertEquals("Start typing", drawn.get(0).getText());
		Assert.assertEquals(0.5F, drawn.get(0).getAlpha(), 0F);
		field.focused(true);
		Assert.assertTrue(this.draw().isEmpty());
	}

	@Test
	public void clipsItsTextInsideItsMargins() {
		this.field("ab");
		final Draw mask = this.bridges.getRender().getDraws(1F, 0F, 0F).get(0);
		Assert.assertEquals(2D, mask.getLeft(), 0.001D);
		Assert.assertEquals(398D, mask.getRight(), 0.001D);
		Assert.assertEquals(2D, mask.getTop(), 0.001D);
		Assert.assertEquals(198D, mask.getBottom(), 0.001D);
	}

	@Test
	public void drawsTheCursorOnItsLine() {
		this.field("ab\ncd").cursorPosition(4);
		this.bridges.frame();
		final Draw cursor = this.cursor();
		Assert.assertEquals(12D, cursor.getLeft(), 0.001D);
		Assert.assertEquals(14D, cursor.getRight(), 0.001D);
		Assert.assertEquals(22D, cursor.getTop(), 0.001D);
		Assert.assertEquals(42D, cursor.getBottom(), 0.001D);
	}

	@Test
	public void drawsTheCursorOfAnEmptyTextAtItsStart() {
		this.field("");
		Assert.assertEquals(2D, this.cursor().getLeft(), 0.001D);
		Assert.assertEquals(2D, this.cursor().getTop(), 0.001D);
	}

	@Test
	public void drawsTheCursorAtTheStartOfAWrappedLine() {
		this.field(54D, "hello world").cursorPosition(6);
		this.bridges.frame();
		Assert.assertEquals(2D, this.cursor().getLeft(), 0.001D);
		Assert.assertEquals(22D, this.cursor().getTop(), 0.001D);
	}

	@Test
	public void pulsesTheOpacityOfItsCursor() {
		this.field("ab");
		this.bridges.getClock().setTime(234L);
		this.bridges.frame();
		Assert.assertEquals(1F, this.cursor().getAlpha(), 0.0001F);
		this.bridges.getClock().setTime(734L);
		this.bridges.frame();
		Assert.assertEquals(0F, this.cursor().getAlpha(), 0.0001F);
	}

	@Test
	public void hidesItsCursorWhileUnfocused() {
		this.field("ab").focused(false);
		this.bridges.frame();
		Assert.assertTrue(this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F).isEmpty());
	}

	@Test
	public void highlightsASelectionOnOneLine() {
		this.selected("abcdef", 1, 4);
		this.bridges.frame();
		final List<Draw> selections = this.selections();
		Assert.assertEquals(1, selections.size());
		Assert.assertEquals(12D, selections.get(0).getLeft(), 0.001D);
		Assert.assertEquals(42D, selections.get(0).getRight(), 0.001D);
		Assert.assertEquals(2D, selections.get(0).getTop(), 0.001D);
		Assert.assertEquals(22D, selections.get(0).getBottom(), 0.001D);
	}

	@Test
	public void highlightsASelectionAcrossLines() {
		this.selected("ab\ncd\nef", 7, 1);
		this.bridges.frame();
		final List<Draw> selections = this.selections();
		Assert.assertEquals(3, selections.size());
		Assert.assertEquals(12D, selections.get(0).getLeft(), 0.001D);
		Assert.assertEquals(22D, selections.get(0).getRight(), 0.001D);
		Assert.assertEquals(2D, selections.get(0).getTop(), 0.001D);
		Assert.assertEquals(2D, selections.get(1).getLeft(), 0.001D);
		Assert.assertEquals(22D, selections.get(1).getRight(), 0.001D);
		Assert.assertEquals(22D, selections.get(1).getTop(), 0.001D);
		Assert.assertEquals(2D, selections.get(2).getLeft(), 0.001D);
		Assert.assertEquals(12D, selections.get(2).getRight(), 0.001D);
		Assert.assertEquals(42D, selections.get(2).getTop(), 0.001D);
	}

	@Test
	public void marksTheEmptyLinesOfASelection() {
		this.selected("ab\n\ncd", 2, 4);
		this.bridges.frame();
		final List<Draw> selections = this.selections();
		Assert.assertEquals(3, selections.size());
		Assert.assertEquals(22D, selections.get(0).getLeft(), 0.001D);
		Assert.assertEquals(24D, selections.get(0).getRight(), 0.001D);
		Assert.assertEquals(2D, selections.get(1).getLeft(), 0.001D);
		Assert.assertEquals(4D, selections.get(1).getRight(), 0.001D);
		Assert.assertEquals(22D, selections.get(1).getTop(), 0.001D);
		Assert.assertEquals(2D, selections.get(2).getLeft(), 0.001D);
		Assert.assertEquals(4D, selections.get(2).getRight(), 0.001D);
		Assert.assertEquals(42D, selections.get(2).getTop(), 0.001D);
	}

	@Test
	public void highlightsAWrappedTextUpToItsHangingSpace() {
		final MultilineTextFieldNode field = this.field(44D, "abcd ");
		this.control(field, Key.A);
		this.bridges.frame();
		final List<Draw> selections = this.selections();
		Assert.assertEquals(1, selections.size());
		Assert.assertEquals(2D, selections.get(0).getLeft(), 0.001D);
		Assert.assertEquals(42D, selections.get(0).getRight(), 0.001D);
		Assert.assertEquals(2D, selections.get(0).getTop(), 0.001D);
	}

	@Test
	public void keepsTheColumnWhenMovingBetweenLines() {
		final MultilineTextFieldNode field = this.field("ab\ncd");
		this.press(field, Key.DOWN);
		Assert.assertEquals(3, field.getCursorPos());
		this.press(field, Key.UP);
		Assert.assertEquals(0, field.getCursorPos());
	}

	@Test
	public void keepsDrawingItsUiOnceAnEmptyTextIsSelected() {
		final MultilineTextFieldNode field = this.markedField("");
		this.control(field, Key.A);
		this.bridges.frame();
		Assert.assertEquals(1, this.bridges.getRender().getDraws(0.6F, 0.4F, 0.2F).size());
	}

	@Test
	public void keepsDrawingItsUiOnceAScrolledTextIsCleared() {
		final MultilineTextFieldNode field = this.markedField("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\nA");
		this.press(field, Key.END);
		this.bridges.frame();
		field.text("");
		this.bridges.frame();
		Assert.assertEquals(1, this.bridges.getRender().getDraws(0.6F, 0.4F, 0.2F).size());
		Assert.assertEquals(0D, field.getYOffset(), 0D);
	}

	@Test
	public void selectsEverythingWithTheRightControlKey() {
		final MultilineTextFieldNode field = this.field("ab\ncd").cursorPosition(2);
		this.bridges.getWindow().getKeys().add(Key.RIGHT_CONTROL);
		field.keyPressed('a', Key.A, InternalContext.create());
		Assert.assertEquals("ab\ncd", field.getText());
		Assert.assertEquals(0, field.getSelectionStart());
		Assert.assertEquals(5, field.getCursorPos());
	}

	@Test
	public void erasesTheCharacterBeforeACollapsedSelection() {
		final MultilineTextFieldNode field = this.field("ab\ncd").cursorPosition(2);
		this.bridges.getWindow().getKeys().add(Key.LEFT_SHIFT);
		this.press(field, Key.RIGHT);
		this.press(field, Key.LEFT);
		this.bridges.getWindow().getKeys().remove(Key.LEFT_SHIFT);
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("a\ncd", field.getText());
		Assert.assertEquals(1, field.getCursorPos());
	}

	@Test
	public void reportsASingleChangeWhenTypingOverASelection() {
		final List<String> changes = new ArrayList<>();
		final MultilineTextFieldNode field = this.field("ab").filter((oldText, newText) -> newText.isEmpty() ? oldText : newText).onChange((node, oldText, newText) -> changes.add(oldText + ">" + newText));
		this.control(field, Key.A);
		field.keyPressed('c', Key.C, InternalContext.create());
		Assert.assertEquals("c", field.getText());
		Assert.assertEquals(Collections.singletonList("ab>c"), changes);
	}

	@Test
	public void refusesWhatIsTypedOnceFull() {
		final MultilineTextFieldNode field = this.field("abc").maxTextLength(3).cursorPosition(1);
		field.keyPressed('x', Key.X, InternalContext.create());
		Assert.assertEquals("abc", field.getText());
	}

	@Test
	public void pastesItsClipboardAsItIs() {
		final MultilineTextFieldNode field = this.field("");
		this.bridges.getWindow().setClipboard("${newline}");
		this.control(field, Key.V);
		Assert.assertEquals("${newline}", field.getText());
	}

	@Test
	public void breaksItsLinesOnlyOnRealLineBreaks() {
		this.field("a<br>b\nc");
		final List<Drawn> drawn = this.draw();
		Assert.assertEquals(2, drawn.size());
		Assert.assertEquals("a<br>b", drawn.get(0).getText());
		Assert.assertEquals("c", drawn.get(1).getText());
	}

	@Test
	public void extendsItsSelectionToBothEndsWithShiftHomeAndEnd() {
		final MultilineTextFieldNode field = this.field("ab\ncd").cursorPosition(2);
		this.bridges.getWindow().getKeys().add(Key.LEFT_SHIFT);
		this.press(field, Key.END);
		Assert.assertEquals(2, field.getSelectionStart());
		Assert.assertEquals(5, field.getCursorPos());
		this.press(field, Key.HOME);
		Assert.assertEquals(2, field.getSelectionStart());
		Assert.assertEquals(0, field.getCursorPos());
		this.bridges.getWindow().getKeys().remove(Key.LEFT_SHIFT);
		this.press(field, Key.END);
		Assert.assertEquals(-1, field.getSelectionStart());
	}

	@Test
	public void dropsItsSelectionOnceUnfocused() {
		final MultilineTextFieldNode field = this.field("ab\ncd");
		this.control(field, Key.A);
		this.press(field, Key.ESCAPE);
		Assert.assertEquals(-1, field.getSelectionStart());
	}

	@Test
	public void dropsTheCharactersItCannotShowFromEveryText() {
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, 400D, 200D).text("a\u00a7b\tc\u20ac\nd");
		Assert.assertEquals("abc\nd", field.getText());
		Assert.assertEquals("x\ny", field.filter((oldText, newText) -> "x\r\ny\t").text("z").getText());
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
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, 400D, 200D).info(TextInfo.create(font, 10F).markups(markup)).text("ab");
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
			final MultilineTextFieldNode field = this.field("one two");
			this.control(field, Key.A);
			Assert.assertEquals(-1, field.getSelectionStart());
			this.bridges.getWindow().getKeys().add(Key.RIGHT_SUPER);
			this.press(field, Key.A);
			this.bridges.getWindow().getKeys().remove(Key.RIGHT_SUPER);
			Assert.assertEquals(0, field.getSelectionStart());
			this.press(field, Key.END);
			this.bridges.getWindow().getKeys().add(Key.RIGHT_ALT);
			this.press(field, Key.BACKSPACE);
			this.bridges.getWindow().getKeys().remove(Key.RIGHT_ALT);
			Assert.assertEquals(" one ", field.getText());
		} finally {
			System.setProperty("os.name", system);
		}
	}

	@Test
	public void bindsItsTextToASignalBothWays() {
		final List<String> changes = new ArrayList<>();
		final Signal<String> signal = new Signal<>("a\r\nb");
		final MultilineTextFieldNode field = this.field("").onChange((node, oldText, newText) -> changes.add(newText)).signal(signal);
		Assert.assertEquals("a\nb", field.getText());
		Assert.assertEquals("a\nb", signal.getOrDefault());
		this.press(field, Key.ENTER);
		Assert.assertEquals("\na\nb", signal.getOrDefault());
		signal.set("c");
		Assert.assertEquals("c", field.getText());
		Assert.assertEquals(Arrays.asList("a\nb", "\na\nb", "c"), changes);
	}

	@Test
	public void readsItsTextAsItsValue() {
		final MultilineTextFieldNode field = this.field("a\r\nb");
		Assert.assertEquals("a\nb", field.getValue());
		Assert.assertEquals("", field.text("").getValue());
	}

	private MultilineTextFieldNode field(final String text) {
		return this.field(400D, text);
	}

	private MultilineTextFieldNode field(final double width, final String text) {
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, width, 200D).info(TextInfo.create(this.font, 10F, MultilineTextFieldNodeTest.INK)).text(text).focused(true);
		this.bridges.open(new NodeUI(field));
		return field;
	}

	private MultilineTextFieldNode markedField(final String text) {
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, 400D, 200D).info(TextInfo.create(this.font, 10F, MultilineTextFieldNodeTest.INK)).text(text).focused(true);
		this.bridges.open(new NodeUI(field, RectNode.create(500D, 0D, 50D, 50D).color(new Color(0.6F, 0.4F, 0.2F, 1F))));
		return field;
	}

	private MultilineTextFieldNode selected(final String text, final int start, final int cursor) {
		final MultilineTextFieldNode field = this.field(text).cursorPosition(start);
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

	private void press(final MultilineTextFieldNode field, final Key key) {
		field.keyPressed(' ', key, InternalContext.create());
	}

	private void control(final MultilineTextFieldNode field, final Key key) {
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.press(field, key);
		this.bridges.getWindow().getKeys().remove(Key.LEFT_CONTROL);
	}

	private void type(final MultilineTextFieldNode field, final String text) {
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

	private List<Drawn> draw() {
		this.font.getDrawn().clear();
		this.bridges.frame();
		return this.font.getDrawn();
	}

	private Draw cursor() {
		final List<Draw> draws = this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F);
		Assert.assertEquals(1, draws.size());
		return draws.get(0);
	}

	private List<Draw> selections() {
		return this.bridges.getRender().getDraws(50 / 255F, 152 / 255F, 253 / 255F);
	}

	public static final class FieldUI extends UI {

		private final String text;

		private MultilineTextFieldNode field;

		private FieldUI(final String text) {
			this.text = text;
		}

		@Override
		public void init() {
			this.field = MultilineTextFieldNode.create(0D, 0D, 400D, 200D).info(TextInfo.create(MultilineTextFieldNodeTest.FONT, 10F)).text(this.text).focused(true);
			this.field.attach(this);
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

	public static final class FieldFont implements IFont, IFontProvider {

		@Getter
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