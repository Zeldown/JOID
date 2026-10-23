package dev.joid.lib.ui.node.impl.control.field;

import java.lang.reflect.Field;
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
import dev.joid.lib.ui.event.impl.node.NodeCharEvent;
import dev.joid.lib.ui.event.impl.node.NodeKeyEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseScrollEvent;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.property.OverflowProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class MultilineTextFieldNodeTest {

	private static final Color INK = new Color(0.2F, 0.4F, 0.6F, 1F);

	private static final IFont FONT = () -> MultilineTextFieldNodeTest.RENDERER;

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

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private final FieldFont font = new FieldFont();

	@Test
	public void pastesAWindowsLineEndingAsOneLineBreak() {
		final FieldUI ui = new FieldUI("");
		this.bridges.open(ui);
		this.bridges.getWindow().setClipboard("ab\r\ncd");
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		ui.field.keyPressed(NodeKeyEvent.create(ui.field, Key.V, Key.V, null));
		Assert.assertEquals("ab\ncd", ui.field.getText());
	}

	@Test
	public void storesAWindowsLineEndingAsOneLineBreak() {
		final FieldUI ui = new FieldUI("ab\r\ncd\ref");
		this.bridges.open(ui);
		Assert.assertEquals("ab\ncd\nef", ui.field.getText());
		ui.field.cursorPosition(1);
		ui.field.keyPressed(NodeKeyEvent.create(ui.field, Key.DOWN, Key.DOWN, null));
		Assert.assertEquals(4, ui.field.getCursorPosition());
	}

	@Test
	public void startsEmptyAndUnfocused() {
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, 400D, 200D);
		Assert.assertEquals("", field.getText());
		Assert.assertEquals("", field.getPlaceholder());
		Assert.assertFalse(field.isFocused());
		Assert.assertEquals(-1, field.getMaxTextLength());
		Assert.assertEquals(0, field.getCursorPosition());
		Assert.assertEquals(-1, TextFieldNodeTest.anchor(field));
		Assert.assertTrue(field.getAccept().test("abc"));
		Assert.assertEquals(0D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 0D);
	}

	@Test
	public void startsWithTheDocumentedMargins() {
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, 400D, 200D);
		Assert.assertEquals(2D, field.getMarginTop(), 0D);
		Assert.assertEquals(2D, field.getMarginLeft(), 0D);
		Assert.assertEquals(2D, field.getMarginRight(), 0D);
		Assert.assertEquals(2D, field.getMarginBottom(), 0D);
		Assert.assertEquals(0D, field.getCursorMargin(), 0D);
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
	public void keepsItsDefaultCursorMarginOnTwoLinesOfItsText() {
		final MultilineTextFieldNode given = MultilineTextFieldNode.create(0D, 300D, 400D, 200D).info(TextInfo.create(this.font, 10F)).cursorMargin(5D);
		this.bridges.open(new NodeUI(given));
		final MultilineTextFieldNode field = this.field("");
		Assert.assertEquals(40D, field.getCursorMargin(), 0D);
		Assert.assertEquals(5D, given.getCursorMargin(), 0D);
		field.info(TextInfo.create(this.font, 20F));
		this.bridges.frame();
		Assert.assertEquals(2D * TextInfo.create(this.font, 20F).getHeight(), field.getCursorMargin(), 0D);
	}

	@Test
	public void setsEveryMarginAtOnce() {
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, 400D, 200D);
		Assert.assertSame(field, field.margin(3D));
		Assert.assertEquals(3D, field.getMarginTop(), 0D);
		Assert.assertEquals(3D, field.getMarginLeft(), 0D);
		Assert.assertEquals(3D, field.getMarginRight(), 0D);
		Assert.assertEquals(3D, field.getMarginBottom(), 0D);
		Assert.assertEquals(0D, field.getCursorMargin(), 0D);
		Assert.assertSame(field, field.margin(4D).cursorMargin(6D));
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
	public void reportsEachChangeOfItsText() {
		final List<Object> changes = new ArrayList<>();
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, 400D, 200D).info(TextInfo.create(this.font, 10F).color(MultilineTextFieldNodeTest.INK)).onChange(event -> changes.addAll(Arrays.asList(event.getNode(), event.getText(), event.getValue(), event.isValid())));
		this.bridges.open(new NodeUI(field));
		field.text("a\nb").text("a\nb");
		Assert.assertEquals(Arrays.asList(field, "a\nb", "a\nb", true), changes);
	}

	@Test
	public void normalizesAGivenTextAtOnce() {
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, 400D, 200D).normalize(String::toUpperCase);
		Assert.assertEquals("AB", field.text("ab").getText());
		Assert.assertEquals("CD\nE", field.text("cd\ne").getText());
	}

	@Test
	public void normalizesAGivenTextWhateverTheOrderOfItsSetters() {
		Assert.assertEquals("ab\ncd", MultilineTextFieldNode.create(0D, 0D, 400D, 200D).<MultilineTextFieldNode>text(" ab\ncd ").normalize(String::trim).getText());
	}

	@Test
	public void breaksOneLineForAnEnterFollowedByItsCarriageReturn() {
		final MultilineTextFieldNode field = this.field("ab").cursorPosition(2);
		this.press(field, Key.ENTER);
		this.type(field, "\r");
		Assert.assertEquals("ab\n", field.getText());
		Assert.assertEquals(3, field.getCursorPosition());
	}

	@Test
	public void refusesWhatItsAcceptRefusesWithoutMovingItsCursor() {
		final MultilineTextFieldNode field = this.field("ab").accept(text -> !text.contains("\n\n")).cursorPosition(2);
		this.press(field, Key.ENTER);
		Assert.assertEquals("ab\n", field.getText());
		this.press(field, Key.ENTER);
		Assert.assertEquals("ab\n", field.getText());
		Assert.assertEquals(3, field.getCursorPosition());
	}

	@Test
	public void keepsItsTypedTextOnEscape() {
		final MultilineTextFieldNode field = this.field("ab").cursorPosition(2);
		this.type(field, "cd");
		this.press(field, Key.ESCAPE);
		Assert.assertEquals("abcd", field.getText());
		Assert.assertFalse(field.isFocused());
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
		final MultilineTextFieldNode field = this.field("").focused(false).onFocus(event -> focuses.add(event.getNode().isFocused())).onBlur(event -> focuses.add(event.getNode().isFocused()));
		field.focused(true).focused(true).focused(false);
		Assert.assertEquals(Arrays.asList(true, false), focuses);
	}

	@Test
	public void insertsALineBreakOnEnter() {
		final MultilineTextFieldNode field = this.field("ab").cursorPosition(1);
		this.press(field, Key.ENTER);
		Assert.assertEquals("a\nb", field.getText());
		Assert.assertEquals(2, field.getCursorPosition());
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
	public void dropsATypedCarriageReturnLikeTheOtherControlCharacters() {
		final MultilineTextFieldNode field = this.field("");
		this.type(field, "a\rb\tc\u0007\u007f€");
		Assert.assertEquals("abc€", field.getText());
	}

	@Test
	public void replacesABackwardSelectionWithWhatIsTyped() {
		final MultilineTextFieldNode field = this.selected("ab\ncd", 5, 1);
		this.type(field, "x");
		Assert.assertEquals("ax", field.getText());
		Assert.assertEquals(2, field.getCursorPosition());
	}

	@Test
	public void movesItsCursorAcrossLinesWithTheArrows() {
		final MultilineTextFieldNode field = this.field("ab\ncd").cursorPosition(2);
		this.press(field, Key.RIGHT);
		Assert.assertEquals(3, field.getCursorPosition());
		for (int i = 0; i < 4; i++) {
			this.press(field, Key.LEFT);
		}
		Assert.assertEquals(0, field.getCursorPosition());
		for (int i = 0; i < 9; i++) {
			this.press(field, Key.RIGHT);
		}
		Assert.assertEquals(5, field.getCursorPosition());
	}

	@Test
	public void keepsItsCursorMarginBelowTheCursorUntilTheEndOfItsText() {
		final MultilineTextFieldNode field = this.field("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\na\nb\nc\nd\ne").cursorPosition(0);
		for (int i = 0; i < 8; i++) {
			this.press(field, Key.DOWN);
		}
		Assert.assertEquals(24D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 1E-9D);
		for (int i = 0; i < 6; i++) {
			this.press(field, Key.DOWN);
		}
		Assert.assertEquals(104D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 1E-9D);
	}

	@Test
	public void followsTheCursorDownIntoAWrappedWord() {
		final MultilineTextFieldNode field = this.field(94D, "0\n1\n2\n3\n4\n5\n6\n7\nhello world\nx\ny\nz").cursorPosition(19);
		this.press(field, Key.DOWN);
		Assert.assertEquals(25, field.getCursorPosition());
		Assert.assertEquals(44D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 1E-9D);
	}

	@Test
	public void jumpsOverWholeWordsAndLineBreaksWithControl() {
		final MultilineTextFieldNode field = this.field("one\ntwo three");
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.press(field, Key.RIGHT);
		Assert.assertEquals(4, field.getCursorPosition());
		this.press(field, Key.RIGHT);
		Assert.assertEquals(8, field.getCursorPosition());
		this.press(field, Key.RIGHT);
		Assert.assertEquals(13, field.getCursorPosition());
		this.press(field, Key.LEFT);
		Assert.assertEquals(8, field.getCursorPosition());
		this.press(field, Key.LEFT);
		Assert.assertEquals(4, field.getCursorPosition());
		this.press(field, Key.LEFT);
		Assert.assertEquals(0, field.getCursorPosition());
	}

	@Test
	public void dropsItsSelectionOnALeftArrowWithoutShift() {
		final MultilineTextFieldNode field = this.selected("ab\ncd", 1, 2);
		this.press(field, Key.LEFT);
		Assert.assertEquals(-1, TextFieldNodeTest.anchor(field));
		Assert.assertEquals(1, field.getCursorPosition());
	}

	@Test
	public void movesDownToTheEndOfAShorterLine() {
		final MultilineTextFieldNode field = this.field("abcd\nab").cursorPosition(4);
		this.press(field, Key.DOWN);
		Assert.assertEquals(7, field.getCursorPosition());
		this.press(field, Key.DOWN);
		Assert.assertEquals(7, field.getCursorPosition());
	}

	@Test
	public void movesUpToTheEndOfAShorterLine() {
		final MultilineTextFieldNode field = this.field("ab\nabcd").cursorPosition(7);
		this.press(field, Key.UP);
		Assert.assertEquals(2, field.getCursorPosition());
		this.press(field, Key.UP);
		Assert.assertEquals(2, field.getCursorPosition());
	}

	@Test
	public void selectsWithShiftAndTheVerticalArrows() {
		final MultilineTextFieldNode down = this.field("abcd\nab").cursorPosition(4);
		this.bridges.getWindow().getKeys().add(Key.LEFT_SHIFT);
		this.press(down, Key.DOWN);
		Assert.assertEquals(4, TextFieldNodeTest.anchor(down));
		Assert.assertEquals(7, down.getCursorPosition());
		this.bridges.getWindow().getKeys().remove(Key.LEFT_SHIFT);
		final MultilineTextFieldNode up = this.field("ab\nabcd").cursorPosition(7);
		this.bridges.getWindow().getKeys().add(Key.RIGHT_SHIFT);
		this.press(up, Key.UP);
		Assert.assertEquals(7, TextFieldNodeTest.anchor(up));
		Assert.assertEquals(2, up.getCursorPosition());
	}

	@Test
	public void dropsItsSelectionOnAVerticalArrowWithoutShift() {
		final MultilineTextFieldNode down = this.selected("ab\nab\nab", 0, 1);
		this.press(down, Key.DOWN);
		Assert.assertEquals(-1, TextFieldNodeTest.anchor(down));
		final MultilineTextFieldNode up = this.selected("ab\nab\nab", 7, 6);
		this.press(up, Key.UP);
		Assert.assertEquals(-1, TextFieldNodeTest.anchor(up));
	}

	@Test
	public void reachesBothEndsOfTheTextWithHomeAndEnd() {
		final MultilineTextFieldNode field = this.field("ab\ncd").cursorPosition(1);
		this.press(field, Key.END);
		Assert.assertEquals(5, field.getCursorPosition());
		this.press(field, Key.HOME);
		Assert.assertEquals(0, field.getCursorPosition());
	}

	@Test
	public void erasesTheCharacterBeforeTheCursorWithBackspace() {
		final MultilineTextFieldNode field = this.field("ab\ncd").cursorPosition(3);
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("abcd", field.getText());
		Assert.assertEquals(2, field.getCursorPosition());
		this.press(field, Key.HOME);
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("abcd", field.getText());
	}

	@Test
	public void erasesTheSelectionWithDelete() {
		final MultilineTextFieldNode forward = this.selected("ab\ncd", 1, 4);
		this.press(forward, Key.DELETE);
		Assert.assertEquals("ad", forward.getText());
		final MultilineTextFieldNode backward = this.selected("ab\ncd", 4, 1);
		this.press(backward, Key.DELETE);
		Assert.assertEquals("ad", backward.getText());
		Assert.assertEquals(1, backward.getCursorPosition());
		Assert.assertEquals(-1, TextFieldNodeTest.anchor(backward));
	}

	@Test
	public void ignoresTheKeyboardWhileUnfocused() {
		final MultilineTextFieldNode field = this.field("ab").focused(false);
		final NodeCharEvent<?> context = NodeCharEvent.create(field, 'x', null);
		field.charTyped(context);
		Assert.assertEquals("ab", field.getText());
		Assert.assertFalse(context.isConsumed());
	}

	@Test
	public void focusesOnAClickAndPutsTheCursorUnderTheMouse() {
		final List<Boolean> focuses = new ArrayList<>();
		final MultilineTextFieldNode field = this.field("ab\ncd").focused(false).onFocus(event -> focuses.add(event.getNode().isFocused()));
		this.click(14D, 30D);
		Assert.assertTrue(field.isFocused());
		Assert.assertEquals(4, field.getCursorPosition());
		Assert.assertEquals(Collections.singletonList(true), focuses);
	}

	@Test
	public void putsTheCursorAtTheEndOfTheLineOnAClickPastIt() {
		final MultilineTextFieldNode field = this.field("ab\ncd");
		this.click(100D, 10D);
		Assert.assertEquals(2, field.getCursorPosition());
	}

	@Test
	public void putsTheCursorOnTheNearestLineOnAClickAboveOrBelowTheText() {
		final MultilineTextFieldNode field = this.field("ab\ncd");
		this.click(100D, 150D);
		Assert.assertEquals(5, field.getCursorPosition());
		this.click(5D, 1D);
		Assert.assertEquals(0, field.getCursorPosition());
	}

	@Test
	public void putsTheCursorUnderAClickOnAWrappedLine() {
		final MultilineTextFieldNode field = this.field(54D, "hello world");
		this.click(14D, 30D);
		Assert.assertEquals(7, field.getCursorPosition());
	}

	@Test
	public void putsThePositionWhereAWordWrapsAtTheStartOfTheNextLine() {
		final MultilineTextFieldNode field = this.field(54D, "aaaaaaaaaaaa");
		this.click(2D, 30D);
		Assert.assertEquals(5, field.getCursorPosition());
		this.press(field, Key.UP);
		Assert.assertEquals(0, field.getCursorPosition());
		this.press(field, Key.DOWN);
		this.press(field, Key.DOWN);
		Assert.assertEquals(10, field.getCursorPosition());
	}

	@Test
	public void putsTheCursorUnderAClickAfterACarriageReturn() {
		final MultilineTextFieldNode field = this.field("ab\rcd");
		this.click(14D, 30D);
		Assert.assertEquals(4, field.getCursorPosition());
	}

	@Test
	public void focusesAnEmptyFieldOnAClick() {
		final MultilineTextFieldNode field = this.field("").focused(false);
		this.click(100D, 100D);
		Assert.assertTrue(field.isFocused());
		Assert.assertEquals(0, field.getCursorPosition());
	}

	@Test
	public void extendsItsSelectionOnAShiftClick() {
		final MultilineTextFieldNode field = this.field("ab\ncd");
		this.click(14D, 10D);
		this.bridges.getWindow().getKeys().add(Key.LEFT_SHIFT);
		this.click(14D, 30D);
		Assert.assertEquals(1, TextFieldNodeTest.anchor(field));
		Assert.assertEquals(4, field.getCursorPosition());
		this.bridges.getWindow().getKeys().remove(Key.LEFT_SHIFT);
		this.bridges.getClock().advance(600L);
		this.click(14D, 30D);
		Assert.assertEquals(-1, TextFieldNodeTest.anchor(field));
	}

	@Test
	public void unfocusesAndDropsItsSelectionOnAClickBesideIt() {
		final MultilineTextFieldNode field = this.field("ab\ncd");
		this.control(field, Key.A);
		this.click(600D, 600D);
		Assert.assertFalse(field.isFocused());
		Assert.assertEquals(-1, TextFieldNodeTest.anchor(field));
	}

	@Test
	public void unfocusesWhenTheClickWasAlreadyTaken() {
		final MultilineTextFieldNode field = this.field("ab\ncd");
		final NodeMouseEvent<?> consumedPress = NodeMouseEvent.create(field, 14D, 30D, MouseButton.LEFT, 0L, null);
		consumedPress.consume();
		field.mousePressed(consumedPress);
		Assert.assertFalse(field.isFocused());
	}

	@Test
	public void scrollsOneLinePerWheelTickWhileHovered() {
		final MultilineTextFieldNode field = this.field("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\na\nb\nc\nd\ne");
		this.bridges.move(10D, 10D).frames(2).scroll(-1D);
		Assert.assertEquals(20D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 0D);
		for (int i = 0; i < 5; i++) {
			this.bridges.scroll(-1D);
		}
		Assert.assertEquals(104D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 0D);
		this.bridges.scroll(1D);
		Assert.assertEquals(84D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 0D);
		for (int i = 0; i < 5; i++) {
			this.bridges.scroll(1D);
		}
		Assert.assertEquals(0D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 0D);
	}

	@Test
	public void ignoresTheWheelOutsideItself() {
		final MultilineTextFieldNode field = this.field("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\na\nb\nc\nd\ne");
		this.bridges.move(600D, 100D).frames(2).scroll(-1D);
		Assert.assertEquals(0D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 0D);
	}

	@Test
	public void scrollsUnderTheMouseWhileUnfocused() {
		final MultilineTextFieldNode field = this.field("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\na\nb\nc\nd\ne").focused(false);
		this.bridges.move(10D, 10D).frames(2).scroll(-1D);
		Assert.assertEquals(20D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 0D);
	}

	@Test
	public void leavesAWheelAlreadyTakenElsewhere() {
		final MultilineTextFieldNode field = this.field("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\na\nb\nc\nd\ne");
		final NodeMouseScrollEvent<?> consumedScroll = NodeMouseScrollEvent.create(field, 10D, 10D, 0D, -1D, null);
		consumedScroll.consume();
		field.mouseScroll(consumedScroll);
		Assert.assertEquals(0D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 0D);
	}

	@Test
	public void ignoresAWheelThatDoesNotTurn() {
		final MultilineTextFieldNode field = this.field("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\na\nb\nc\nd\ne");
		this.bridges.move(10D, 10D).frames(2).scroll(-1D);
		field.mouseScroll(NodeMouseScrollEvent.create(field, 10D, 10D, 0D, 0D, null));
		Assert.assertEquals(20D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 0D);
	}

	@Test
	public void staysAtTheTopOfATextShorterThanItself() {
		final MultilineTextFieldNode field = this.field("ab");
		this.bridges.scroll(-1D).frame();
		Assert.assertEquals(0D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 0D);
	}

	@Test
	public void consumesTheWheelWhileItScrolls() {
		final MultilineTextFieldNode field = this.field("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\na\nb\nc\nd\ne");
		this.bridges.move(10D, 10D).frames(2);
		final NodeMouseScrollEvent<?> down = NodeMouseScrollEvent.create(field, 10D, 10D, 0D, -1D, null);
		field.mouseScroll(down);
		Assert.assertTrue(down.isConsumed());
		Assert.assertEquals(20D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 0D);
		final NodeMouseScrollEvent<?> up = NodeMouseScrollEvent.create(field, 10D, 10D, 0D, 1D, null);
		field.mouseScroll(up);
		Assert.assertTrue(up.isConsumed());
		Assert.assertEquals(0D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 0D);
	}

	@Test
	public void leavesTheWheelToItsParentAtItsTop() {
		final MultilineTextFieldNode field = this.field("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\na\nb\nc\nd\ne");
		this.bridges.move(10D, 10D).frames(2);
		final NodeMouseScrollEvent<?> context = NodeMouseScrollEvent.create(field, 10D, 10D, 0D, 1D, null);
		field.mouseScroll(context);
		Assert.assertFalse(context.isConsumed());
		Assert.assertEquals(0D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 0D);
	}

	@Test
	public void leavesTheWheelToItsParentAtItsBottom() {
		final MultilineTextFieldNode field = this.field("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\na\nb\nc\nd\ne");
		this.bridges.move(10D, 10D).frames(2);
		for (int i = 0; i < 6; i++) {
			field.mouseScroll(NodeMouseScrollEvent.create(field, 10D, 10D, 0D, -1D, null));
		}
		Assert.assertEquals(104D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 0D);
		final NodeMouseScrollEvent<?> down = NodeMouseScrollEvent.create(field, 10D, 10D, 0D, -1D, null);
		field.mouseScroll(down);
		Assert.assertFalse(down.isConsumed());
		Assert.assertEquals(104D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 0D);
		final NodeMouseScrollEvent<?> up = NodeMouseScrollEvent.create(field, 10D, 10D, 0D, 1D, null);
		field.mouseScroll(up);
		Assert.assertTrue(up.isConsumed());
		Assert.assertEquals(84D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 0D);
	}

	@Test
	public void leavesTheWheelToItsParentWithNothingToScroll() {
		final MultilineTextFieldNode field = this.field("ab");
		this.bridges.move(10D, 10D).frames(2);
		final NodeMouseScrollEvent<?> down = NodeMouseScrollEvent.create(field, 10D, 10D, 0D, -1D, null);
		final NodeMouseScrollEvent<?> up = NodeMouseScrollEvent.create(field, 10D, 10D, 0D, 1D, null);
		field.mouseScroll(down);
		field.mouseScroll(up);
		Assert.assertFalse(down.isConsumed());
		Assert.assertFalse(up.isConsumed());
	}

	@Test
	public void letsItsScrollingParentTakeTheWheelOnceAtItsLimit() {
		final ContainerNode box = ContainerNode.create(0D, 0D, 400D, 300D).overflow(OverflowProperty.SCROLL);
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, 400D, 200D).info(TextInfo.create(this.font, 10F).color(MultilineTextFieldNodeTest.INK)).text("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\na\nb").attach(box);
		RectNode.create(0D, 200D, 100D, 1000D).attach(box);
		this.bridges.open(new NodeUI(box)).frames(30);
		this.bridges.move(10D, 10D).frames(2).scroll(-1D).scroll(-1D).scroll(-1D);
		Assert.assertEquals(44D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 0D);
		Assert.assertEquals(0D, box.getTargetScrollY(), 0D);
		this.bridges.scroll(-1D);
		Assert.assertEquals(44D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 0D);
		Assert.assertTrue(box.getTargetScrollY() < 0D);
	}

	@Test
	public void scrollsToKeepItsCursorInView() {
		final MultilineTextFieldNode field = this.field("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\nA");
		this.press(field, Key.END);
		Assert.assertEquals(24D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 0D);
		final List<Drawn> drawn = this.draw();
		Assert.assertEquals("0", drawn.get(0).getText());
		Assert.assertEquals(-22D, drawn.get(0).getY(), 0D);
		Assert.assertEquals(178D, this.cursor().getTop(), 0.001D);
		this.press(field, Key.HOME);
		Assert.assertEquals(0D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 0D);
	}

	@Test
	public void scrollsBackOnceItsCursorReachesItsTopMargin() {
		final MultilineTextFieldNode field = this.field("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\nA");
		this.press(field, Key.END);
		for (int i = 0; i < 13; i++) {
			this.press(field, Key.LEFT);
		}
		Assert.assertEquals(24D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 0D);
		this.press(field, Key.LEFT);
		Assert.assertEquals(20D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 0D);
	}

	@Test
	public void scrollsBackToTheTopOnceItsTextShrinks() {
		final MultilineTextFieldNode field = this.field("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\na\nb\nc\nd\ne");
		this.bridges.scroll(-1D).scroll(-1D).scroll(-1D);
		field.text("ab");
		final List<Drawn> drawn = this.draw();
		Assert.assertEquals(0D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 0D);
		Assert.assertEquals(2D, drawn.get(0).getY(), 0D);
	}

	@Test
	public void repeatsAHeldArrowAfterHalfASecond() {
		final MultilineTextFieldNode field = this.field("ab\ncd");
		this.bridges.getWindow().getKeys().add(Key.RIGHT);
		this.press(field, Key.RIGHT);
		Assert.assertTrue(MultilineTextFieldNodeTest.<Boolean>read(field, FieldNode.class, "inputting"));
		Assert.assertSame(Key.RIGHT, MultilineTextFieldNodeTest.<Key>read(field, FieldNode.class, "inputType"));
		this.elapse(484L);
		Assert.assertEquals(1, field.getCursorPosition());
		this.elapse(16L);
		Assert.assertEquals(2, field.getCursorPosition());
		this.elapse(100L);
		Assert.assertEquals(3, field.getCursorPosition());
	}

	@Test
	public void repeatsAHeldLeftArrow() {
		final MultilineTextFieldNode field = this.field("ab\ncd").cursorPosition(5);
		this.bridges.getWindow().getKeys().add(Key.LEFT);
		this.press(field, Key.LEFT);
		this.elapse(500L);
		Assert.assertEquals(3, field.getCursorPosition());
	}

	@Test
	public void stopsRepeatingOnceDisabled() {
		final MultilineTextFieldNode field = this.field("abc").cursorPosition(3);
		this.bridges.getWindow().getKeys().add(Key.BACKSPACE);
		this.press(field, Key.BACKSPACE);
		field.enabled(false);
		this.elapse(500L);
		Assert.assertEquals("ab", field.getText());
		Assert.assertFalse(MultilineTextFieldNodeTest.<Boolean>read(field, FieldNode.class, "inputting"));
	}

	@Test
	public void repeatsAHeldEnter() {
		final MultilineTextFieldNode field = this.field("ab").cursorPosition(2);
		this.bridges.getWindow().getKeys().add(Key.ENTER);
		this.press(field, Key.ENTER);
		Assert.assertEquals("ab\n", field.getText());
		this.elapse(500L);
		Assert.assertEquals("ab\n\n", field.getText());
		this.elapse(100L);
		Assert.assertEquals("ab\n\n\n", field.getText());
	}

	@Test
	public void repeatsAHeldDownArrow() {
		final MultilineTextFieldNode field = this.field("a\nb\nc");
		this.bridges.getWindow().getKeys().add(Key.DOWN);
		this.press(field, Key.DOWN);
		Assert.assertEquals(2, field.getCursorPosition());
		this.elapse(500L);
		Assert.assertEquals(4, field.getCursorPosition());
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
	public void cutsAWordThatFollowsTheSpaceStartingItsLine() {
		this.field(54D, " abcdefgh");
		final List<Drawn> drawn = this.draw();
		Assert.assertEquals(2, drawn.size());
		Assert.assertEquals(" abcd", drawn.get(0).getText());
		Assert.assertEquals("efgh", drawn.get(1).getText());
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
	public void drawsTheCursorAfterAHangingSpaceOnTheNextLine() {
		this.field(44D, "abcd ").cursorPosition(5);
		this.bridges.frame();
		Assert.assertEquals(2D, this.cursor().getLeft(), 0.001D);
		Assert.assertEquals(22D, this.cursor().getTop(), 0.001D);
	}

	@Test
	public void pulsesTheOpacityOfItsCursor() {
		this.field("ab");
		this.bridges.getClock().time(234L);
		this.bridges.frame();
		Assert.assertEquals(1F, this.cursor().getAlpha(), 0.0001F);
		this.bridges.getClock().time(734L);
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
	public void highlightsAWrappedTextAndTheLineAfterItsHangingSpace() {
		final MultilineTextFieldNode field = this.field(44D, "abcd ");
		this.control(field, Key.A);
		this.bridges.frame();
		final List<Draw> selections = this.selections();
		Assert.assertEquals(2, selections.size());
		Assert.assertEquals(2D, selections.get(0).getLeft(), 0.001D);
		Assert.assertEquals(42D, selections.get(0).getRight(), 0.001D);
		Assert.assertEquals(2D, selections.get(0).getTop(), 0.001D);
		Assert.assertEquals(2D, selections.get(1).getLeft(), 0.001D);
		Assert.assertEquals(4D, selections.get(1).getRight(), 0.001D);
		Assert.assertEquals(22D, selections.get(1).getTop(), 0.001D);
	}

	@Test
	public void keepsTheColumnWhenMovingBetweenLines() {
		final MultilineTextFieldNode field = this.field("ab\ncd");
		this.press(field, Key.DOWN);
		Assert.assertEquals(3, field.getCursorPosition());
		this.press(field, Key.UP);
		Assert.assertEquals(0, field.getCursorPosition());
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
		Assert.assertEquals(0D, MultilineTextFieldNodeTest.<Double>read(field, MultilineTextFieldNode.class, "yOffset"), 0D);
	}

	@Test
	public void reportsASingleChangeWhenTypingOverASelection() {
		final List<String> changes = new ArrayList<>();
		final MultilineTextFieldNode field = this.field("ab").<MultilineTextFieldNode>accept(text -> !text.isEmpty()).onChange(event -> changes.add(event.getText()));
		this.control(field, Key.A);
		field.charTyped(NodeCharEvent.create(field, 'c', null));
		Assert.assertEquals("c", field.getText());
		Assert.assertEquals(Collections.singletonList("c"), changes);
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
		Assert.assertEquals(2, TextFieldNodeTest.anchor(field));
		Assert.assertEquals(5, field.getCursorPosition());
		this.press(field, Key.HOME);
		Assert.assertEquals(2, TextFieldNodeTest.anchor(field));
		Assert.assertEquals(0, field.getCursorPosition());
		this.bridges.getWindow().getKeys().remove(Key.LEFT_SHIFT);
		this.press(field, Key.END);
		Assert.assertEquals(-1, TextFieldNodeTest.anchor(field));
	}

	@Test
	public void dropsItsSelectionOnceUnfocused() {
		final MultilineTextFieldNode field = this.field("ab\ncd");
		this.control(field, Key.A);
		this.press(field, Key.ESCAPE);
		Assert.assertEquals(-1, TextFieldNodeTest.anchor(field));
	}

	@Test
	public void dropsTheControlCharactersOfEveryText() {
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, 400D, 200D).text("a\u00a7b\tc\u20ac\nd");
		Assert.assertEquals("a\u00a7bc\u20ac\nd", field.getText());
		Assert.assertEquals("x\ny", field.normalize(text -> "x\r\ny\t").text("z").getText());
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
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, 400D, 200D).info(TextInfo.create(font, 10F).markups(markup)).text("ab");
		this.bridges.open(new NodeUI(field));
		Assert.assertFalse(field.isMarkup());
		Assert.assertTrue(infos.get(infos.size() - 1).getMarkups().isEmpty());
		field.markup(true);
		this.bridges.frame();
		Assert.assertEquals(Collections.singletonList(markup), infos.get(infos.size() - 1).getMarkups());
	}

	@Test
	public void bindsItsTextToASignalBothWays() {
		final List<String> changes = new ArrayList<>();
		final Signal<String> signal = new Signal<>("a\r\nb");
		final MultilineTextFieldNode field = this.field("").onChange(event -> changes.add(event.getText())).text(signal);
		Assert.assertEquals("a\nb", field.getText());
		Assert.assertEquals("a\nb", signal.get());
		this.press(field, Key.ENTER);
		Assert.assertEquals("a\nb\n", signal.get());
		signal.set("c");
		Assert.assertEquals("c", field.getText());
		Assert.assertEquals(Arrays.asList("a\nb", "a\nb\n", "c"), changes);
	}

	private MultilineTextFieldNode field(final String text) {
		return this.field(400D, text);
	}

	private MultilineTextFieldNode field(final double width, final String text) {
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, width, 200D).info(TextInfo.create(this.font, 10F).color(MultilineTextFieldNodeTest.INK)).text(text).focused(true);
		this.bridges.open(new NodeUI(field));
		return field;
	}

	private MultilineTextFieldNode markedField(final String text) {
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, 400D, 200D).info(TextInfo.create(this.font, 10F).color(MultilineTextFieldNodeTest.INK)).text(text).focused(true);
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
		field.keyPressed(NodeKeyEvent.create(field, key, key, null));
	}

	private void control(final MultilineTextFieldNode field, final Key key) {
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.press(field, key);
		this.bridges.getWindow().getKeys().remove(Key.LEFT_CONTROL);
	}

	private void type(final MultilineTextFieldNode field, final String text) {
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

	private static <T> T read(final Object field, final Class<?> owner, final String name) {
		try {
			final Field declared = owner.getDeclaredField(name);
			declared.setAccessible(true);
			return (T) declared.get(field);
		} catch (final ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static final class FieldUI extends UI {

		private final String text;

		private MultilineTextFieldNode field;

		@Override
		public void init() {
			this.field = MultilineTextFieldNode.create(0D, 0D, 400D, 200D).info(TextInfo.create(MultilineTextFieldNodeTest.FONT, 10F)).text(this.text).focused(true);
			this.field.attach(this);
		}

	}

	public static final class FieldFont implements IFont, ITextRenderer {

		@Getter
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