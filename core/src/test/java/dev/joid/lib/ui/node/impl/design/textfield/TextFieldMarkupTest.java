package dev.joid.lib.ui.node.impl.design.textfield;

import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.font.dto.markup.ITextMarkup;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.textfield.MultilineTextFieldMarkupTest.MarkupFont;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.key.Key;

public class TextFieldMarkupTest {

	private static final Color INK = new Color(0.2F, 0.4F, 0.6F, 1F);

	private static final ITextMarkup MARKUP = (text, index, style) -> {
		if (text.startsWith("<b>", index)) {
			style.weight(FontWeight.BOLD);
			return 3;
		}

		if (text.startsWith("</b>", index)) {
			style.weight(style.getBase().getWeight());
			return 4;
		}

		if (text.startsWith("<c red>", index)) {
			style.color(Color.RED);
			return 7;
		}

		if (text.startsWith("</c>", index)) {
			style.color(style.getBase().getColor());
			return 4;
		}

		return 0;
	};

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private final MarkupFont font = new MarkupFont();

	@Test
	public void placesTheCursorAtTheClickedCharacterWithTagsOfZeroWidth() {
		final TextFieldNode field = this.field("a<b>bc</b>d", true);
		Assert.assertEquals(0, this.click(field, 4D));
		Assert.assertEquals(1, this.click(field, 12D));
		Assert.assertEquals(5, this.click(field, 20D));
		Assert.assertEquals(6, this.click(field, 44D));
		Assert.assertEquals(11, this.click(field, 45D));
		Assert.assertEquals(11, this.click(field, 90D));
	}

	@Test
	public void placesTheCursorRightAfterTheLastCharacterBeforeAClosingTag() {
		final TextFieldNode field = this.field("<b>ab</b>", true);
		Assert.assertEquals(5, this.click(field, 90D));
		Assert.assertEquals(0, this.click(field, 1D));
	}

	@Test
	public void placesTheCursorOnEachCharacterOfALiteralTagWithoutMarkup() {
		final TextFieldNode field = this.field("a<b>bc</b>d", false);
		Assert.assertEquals(1, this.click(field, 12D));
		Assert.assertEquals(2, this.click(field, 20D));
		Assert.assertEquals(4, this.click(field, 44D));
		Assert.assertEquals(11, this.click(field, 200D));
	}

	@Test
	public void placesTheCursorAtTheStartOfAnEmptyText() {
		final TextFieldNode field = this.field("", true);
		Assert.assertEquals(0, this.click(field, 50D));
	}

	@Test
	public void drawsTheCursorAtZeroWidthAcrossATag() {
		final TextFieldNode field = this.field("a<b>bc</b>d", true);
		for (int position = 1; position <= 4; position++) {
			field.cursorPosition(position);
			Assert.assertEquals(12D, this.cursor().getLeft(), 1E-5D);
		}
		field.cursorPosition(5);
		Assert.assertEquals(27D, this.cursor().getLeft(), 1E-5D);
		for (int position = 6; position <= 10; position++) {
			field.cursorPosition(position);
			Assert.assertEquals(42D, this.cursor().getLeft(), 1E-5D);
		}
		field.cursorPosition(11);
		Assert.assertEquals(52D, this.cursor().getLeft(), 1E-5D);
	}

	@Test
	public void drawsTheCursorAfterEachCharacterOfALiteralTagWithoutMarkup() {
		final TextFieldNode field = this.field("a<b>bc</b>d", false);
		field.cursorPosition(3);
		Assert.assertEquals(32D, this.cursor().getLeft(), 1E-5D);
		field.cursorPosition(11);
		Assert.assertEquals(112D, this.cursor().getLeft(), 1E-5D);
	}

	@Test
	public void movesThroughEveryPositionOfATagWithTheArrows() {
		final TextFieldNode field = this.field("a<b>bc</b>d", true).cursorPosition(1);
		for (int position = 2; position <= 5; position++) {
			this.press(field, Key.RIGHT);
			Assert.assertEquals(position, field.getCursorPos());
		}
		this.press(field, Key.LEFT);
		Assert.assertEquals(4, field.getCursorPos());
		Assert.assertEquals(12D, this.cursor().getLeft(), 1E-5D);
	}

	@Test
	public void goesToTheStartAndTheEndOfItsTextWithHomeAndEnd() {
		for (final boolean markup : new boolean[] {true, false}) {
			final TextFieldNode field = this.field("<b>aa bb</b>", markup).cursorPosition(5);
			this.press(field, Key.END);
			Assert.assertEquals(12, field.getCursorPos());
			this.press(field, Key.HOME);
			Assert.assertEquals(0, field.getCursorPos());
		}
	}

	@Test
	public void jumpsOverATagWithASpaceAsOneWordWithControl() {
		final TextFieldNode field = this.field("a <c red>bb</c> c", true).cursorPosition(2);
		this.control(field, Key.RIGHT);
		Assert.assertEquals(16, field.getCursorPos());
		this.control(field, Key.LEFT);
		Assert.assertEquals(2, field.getCursorPos());
	}

	@Test
	public void stopsOnTheSpaceOfALiteralTagWithControlWithoutMarkup() {
		final TextFieldNode field = this.field("a <c red>bb</c> c", false).cursorPosition(2);
		this.control(field, Key.RIGHT);
		Assert.assertEquals(5, field.getCursorPos());
	}

	@Test
	public void selectsAcrossATagWithItsZeroWidth() {
		final TextFieldNode field = this.field("a<b>bc</b>d", true).cursorPosition(0);
		this.bridges.getWindow().getKeys().add(Key.LEFT_SHIFT);
		for (int i = 0; i < 6; i++) {
			this.press(field, Key.RIGHT);
		}
		this.bridges.getWindow().getKeys().remove(Key.LEFT_SHIFT);
		final Draw selection = this.selection();
		Assert.assertEquals(2D, selection.getLeft(), 1E-5D);
		Assert.assertEquals(42D, selection.getRight(), 1E-5D);
	}

	@Test
	public void selectsEachCharacterOfALiteralTagWithoutMarkup() {
		final TextFieldNode field = this.field("a<b>bc</b>d", false).cursorPosition(0);
		this.bridges.getWindow().getKeys().add(Key.LEFT_SHIFT);
		for (int i = 0; i < 6; i++) {
			this.press(field, Key.RIGHT);
		}
		this.bridges.getWindow().getKeys().remove(Key.LEFT_SHIFT);
		final Draw selection = this.selection();
		Assert.assertEquals(2D, selection.getLeft(), 1E-5D);
		Assert.assertEquals(62D, selection.getRight(), 1E-5D);
	}

	@Test
	public void selectsFromAClickToAShiftClickAcrossATag() {
		final TextFieldNode field = this.field("a<b>bc</b>d", true);
		this.click(field, 4D);
		this.bridges.getWindow().getKeys().add(Key.LEFT_SHIFT);
		this.click(field, 30D);
		this.bridges.getWindow().getKeys().remove(Key.LEFT_SHIFT);
		Assert.assertEquals(0, field.getSelectionStart());
		Assert.assertEquals(5, field.getCursorPos());
		final Draw selection = this.selection();
		Assert.assertEquals(2D, selection.getLeft(), 1E-5D);
		Assert.assertEquals(27D, selection.getRight(), 1E-5D);
	}

	@Test
	public void typesInsideTheStyleRightAfterATagAndOutsideItRightBefore() {
		final TextFieldNode field = this.field("a<b>bc</b>d", true).cursorPosition(4);
		this.type(field, "x");
		Assert.assertEquals("a<b>xbc</b>d", field.getText());
		Assert.assertEquals(5, field.getCursorPos());
		Assert.assertEquals(27D, this.cursor().getLeft(), 1E-5D);
		field.cursorPosition(1);
		this.type(field, "y");
		Assert.assertEquals("ay<b>xbc</b>d", field.getText());
		Assert.assertEquals(22D, this.cursor().getLeft(), 1E-5D);
	}

	@Test
	public void typesBeforeAClosingTagInsideTheStyle() {
		final TextFieldNode field = this.field("<b>ab</b>c", true).cursorPosition(5);
		this.type(field, "x");
		Assert.assertEquals("<b>abx</b>c", field.getText());
		Assert.assertEquals(47D, this.cursor().getLeft(), 1E-5D);
	}

	@Test
	public void typesInsideATagLikeAnyText() {
		final TextFieldNode field = this.field("a<b>bc</b>d", true).cursorPosition(2);
		this.type(field, "z");
		Assert.assertEquals("a<zb>bc</b>d", field.getText());
		Assert.assertEquals(32D, this.cursor().getLeft(), 1E-5D);
		field.cursorPosition(3);
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("a<b>bc</b>d", field.getText());
		Assert.assertEquals(2, field.getCursorPos());
		Assert.assertEquals(12D, this.cursor().getLeft(), 1E-5D);
	}

	@Test
	public void showsATagBrokenByADeletionAsText() {
		final TextFieldNode field = this.field("a<b>bc</b>d", true).cursorPosition(3);
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("a<>bc</b>d", field.getText());
		field.cursorPosition(9);
		Assert.assertEquals(52D, this.cursor().getLeft(), 1E-5D);
	}

	@Test
	public void deletesTheCharacterNextToATag() {
		final TextFieldNode field = this.field("a<b>bc</b>d", true).cursorPosition(4);
		this.press(field, Key.DELETE);
		Assert.assertEquals("a<b>c</b>d", field.getText());
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("a<bc</b>d", field.getText());
		Assert.assertEquals(3, field.getCursorPos());
	}

	@Test
	public void deletesASelectionAcrossATag() {
		final TextFieldNode field = this.field("a<b>bc</b>d", true).cursorPosition(1);
		this.bridges.getWindow().getKeys().add(Key.LEFT_SHIFT);
		for (int i = 0; i < 4; i++) {
			this.press(field, Key.RIGHT);
		}
		this.bridges.getWindow().getKeys().remove(Key.LEFT_SHIFT);
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("ac</b>d", field.getText());
		Assert.assertEquals(1, field.getCursorPos());
		Assert.assertEquals(12D, this.cursor().getLeft(), 1E-5D);
	}

	@Test
	public void selectsTheWordOfADoubleClickWithItsTags() {
		for (final boolean markup : new boolean[] {true, false}) {
			final TextFieldNode field = this.field("aa <b>bold</b> cc", markup);
			this.clicks(field, markup ? 40D : 70D, 2);
			Assert.assertEquals(3, field.getSelectionStart());
			Assert.assertEquals(14, field.getCursorPos());
		}
	}

	@Test
	public void selectsAWordWithASpacedTagAsOneWordWithMarkup() {
		final TextFieldNode field = this.field("a <c red>bb</c> c", true);
		this.clicks(field, 25D, 2);
		Assert.assertEquals(2, field.getSelectionStart());
		Assert.assertEquals(15, field.getCursorPos());
	}

	@Test
	public void selectsEverythingOnATripleClick() {
		final TextFieldNode field = this.field("aa <b>bold</b> cc", true);
		this.clicks(field, 40D, 3);
		Assert.assertEquals(0, field.getSelectionStart());
		Assert.assertEquals(17, field.getCursorPos());
	}

	@Test
	public void extendsItsSelectionByWordsWhenDraggedAfterADoubleClick() {
		final TextFieldNode field = this.field("aa <b>bold</b> cc", true);
		this.clicks(field, 40D, 2);
		this.drag(field, 105D);
		Assert.assertEquals(3, field.getSelectionStart());
		Assert.assertEquals(17, field.getCursorPos());
		this.drag(field, 5D);
		Assert.assertEquals(14, field.getSelectionStart());
		Assert.assertEquals(0, field.getCursorPos());
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		this.drag(field, 92D);
		Assert.assertEquals(0, field.getCursorPos());
	}

	@Test
	public void selectsTheCharactersDraggedOverAfterAClick() {
		final TextFieldNode field = this.field("a<b>bc</b>d", true);
		this.click(field, 4D);
		this.drag(field, 30D);
		Assert.assertEquals(0, field.getSelectionStart());
		Assert.assertEquals(5, field.getCursorPos());
		final Draw selection = this.selection();
		Assert.assertEquals(2D, selection.getLeft(), 1E-5D);
		Assert.assertEquals(27D, selection.getRight(), 1E-5D);
		this.drag(field, 4D);
		Assert.assertEquals(-1, field.getSelectionStart());
		Assert.assertEquals(0, field.getCursorPos());
	}

	@Test
	public void countsNoDoubleClickBetweenTwoDistantOrSlowClicks() {
		final TextFieldNode field = this.field("aa bb", false);
		this.clicks(field, 5D, 1);
		this.clicks(field, 35D, 1);
		Assert.assertEquals(-1, field.getSelectionStart());
		this.bridges.getClock().advance(600L);
		this.clicks(field, 35D, 1);
		Assert.assertEquals(-1, field.getSelectionStart());
		Assert.assertEquals(1, field.getPressCount());
	}

	private TextFieldNode field(final String text, final boolean markup) {
		final TextFieldNode field = TextFieldNode.create(0D, 0D, 400D, 40D).info(TextInfo.create(this.font, 10F, TextFieldMarkupTest.INK).markups(TextFieldMarkupTest.MARKUP)).markup(markup).text(text).focused(true);
		this.bridges.open(new NodeUI(field));
		this.bridges.getClock().advance(1000L);
		return field;
	}

	private int click(final TextFieldNode field, final double x) {
		this.bridges.getClock().advance(600L);
		this.clicks(field, x, 1);
		return field.getCursorPos();
	}

	private void clicks(final TextFieldNode field, final double x, final int count) {
		this.bridges.move(2D + x, 20D).frames(2);
		for (int i = 0; i < count; i++) {
			this.bridges.getUi().mouseReleased(ClickType.LEFT);
			this.bridges.getUi().mousePressed(ClickType.LEFT);
		}
	}

	private void drag(final TextFieldNode field, final double x) {
		this.bridges.move(2D + x, 20D).frames(1);
		this.bridges.getUi().mouseDragged(ClickType.LEFT, 40L);
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

	private Draw cursor() {
		this.bridges.frame();
		final List<Draw> draws = this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F);
		Assert.assertEquals(1, draws.size());
		return draws.get(0);
	}

	private Draw selection() {
		this.bridges.frame();
		final List<Draw> draws = this.bridges.getRender().getDraws(50 / 255F, 152 / 255F, 253 / 255F);
		Assert.assertEquals(1, draws.size());
		return draws.get(0);
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