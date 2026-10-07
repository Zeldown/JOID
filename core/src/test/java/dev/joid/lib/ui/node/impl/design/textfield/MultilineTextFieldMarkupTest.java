package dev.joid.lib.ui.node.impl.design.textfield;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.IFont;
import dev.joid.lib.font.IFontProvider;
import dev.joid.lib.font.dto.FontBounds;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.font.dto.TextStyle;
import dev.joid.lib.font.dto.markup.ITextMarkup;
import dev.joid.lib.font.dto.markup.TextMarkup;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.key.Key;

import lombok.Getter;

public class MultilineTextFieldMarkupTest {

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
	public void wrapsAtASpaceOutsideATagOnly() {
		Assert.assertEquals(Arrays.asList("aaaa<c red>bbbbbb", "<c red>cc"), this.lines(this.field("aaaa<c red>bbbbbb cc", true)));
	}

	@Test
	public void wrapsALiteralTagLikeAnyTextWithoutMarkup() {
		Assert.assertEquals(Arrays.asList("aaaa<c", "red>bbbbbb", "cc"), this.lines(this.field("aaaa<c red>bbbbbb cc", false)));
	}

	@Test
	public void replaysTheOpenStyleOnEachWrappedLineAndMeasuresItWithIt() {
		Assert.assertEquals(Arrays.asList("<b>aaaa", "<b>bbbbb", "<b>cc</b>"), this.lines(this.field("<b>aaaa bbbbb cc</b>", true)));
	}

	@Test
	public void measuresALiteralTagLikeAnyTextWithoutMarkup() {
		Assert.assertEquals(Arrays.asList("<b>aaaa", "bbbbb", "cc</b>"), this.lines(this.field("<b>aaaa bbbbb cc</b>", false)));
	}

	@Test
	public void replaysTheOpenStyleAfterARealLineBreak() {
		Assert.assertEquals(Arrays.asList("<b>aa", "<b>bb</b>", "<b></b>cc"), this.lines(this.field("<b>aa\nbb</b>\ncc", true)));
	}

	@Test
	public void replaysNothingAfterARealLineBreakWithoutMarkup() {
		Assert.assertEquals(Arrays.asList("<b>aa", "bb</b>", "cc"), this.lines(this.field("<b>aa\nbb</b>\ncc", false)));
	}

	@Test
	public void cutsAStyledWordTooLongForALine() {
		Assert.assertEquals(Arrays.asList("<b>aaaaaa", "<b>a"), this.lines(this.field("<b>aaaaaaa", true)));
	}

	@Test
	public void cutsAWordTooLongForALineBeforeATagNeverInsideIt() {
		Assert.assertEquals(Arrays.asList("aaaaaaaaaa", "<b>bb"), this.lines(this.field("aaaaaaaaaa<b>bb", true)));
	}

	@Test
	public void cutsAWordTooLongForALineRightBeforeALiteralTagWithoutMarkup() {
		Assert.assertEquals(Arrays.asList("aaaaaaaaaa", "<b>bb"), this.lines(this.field("aaaaaaaaaa<b>bb", false)));
	}

	@Test
	public void keepsALineThatFillsTheWidthExactly() {
		Assert.assertEquals(Arrays.asList("aaaaaaaaaa"), this.lines(this.field("aaaaaaaaaa", false)));
	}

	@Test
	public void keepsAStyledLineThatFillsTheWidthExactly() {
		Assert.assertEquals(Arrays.asList("<b>aaaa</b>aaaa"), this.lines(this.field("<b>aaaa</b>aaaa", true)));
	}

	@Test
	public void cutsAStyledLineOneCharacterTooLong() {
		Assert.assertEquals(Arrays.asList("<b>aaaa</b>aaaa", "<b></b>a"), this.lines(this.field("<b>aaaa</b>aaaaa", true)));
	}

	@Test
	public void placesTheCursorAtTheClickedCharacterWithTagsOfZeroWidth() {
		final MultilineTextFieldNode field = this.field("a<b>bc</b>d", true);
		Assert.assertEquals(0, this.click(field, 4D));
		Assert.assertEquals(1, this.click(field, 12D));
		Assert.assertEquals(5, this.click(field, 20D));
		Assert.assertEquals(6, this.click(field, 44D));
		Assert.assertEquals(11, this.click(field, 45D));
		Assert.assertEquals(11, this.click(field, 90D));
	}

	@Test
	public void placesTheCursorRightAfterTheLastCharacterBeforeAClosingTag() {
		final MultilineTextFieldNode field = this.field("<b>ab</b>", true);
		Assert.assertEquals(5, this.click(field, 90D));
	}

	@Test
	public void placesTheCursorOnEachCharacterOfALiteralTagWithoutMarkup() {
		final MultilineTextFieldNode field = this.field("a<b>bc</b>d", false);
		Assert.assertEquals(1, this.click(field, 12D));
		Assert.assertEquals(2, this.click(field, 20D));
		Assert.assertEquals(4, this.click(field, 44D));
	}

	@Test
	public void placesTheCursorOnTheClickedWrappedLine() {
		final MultilineTextFieldNode field = this.field("<b>aaaa bbbbb cc</b>", true);
		this.bridges.move(2D + 20D, 2D + 25D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertEquals(9, field.getCursorPos());
		this.bridges.move(2D + 40D, 2D + 45D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertEquals(16, field.getCursorPos());
	}

	@Test
	public void drawsTheCursorAtZeroWidthAcrossATag() {
		final MultilineTextFieldNode field = this.field("a<b>bc</b>d", true);
		for (int position = 1; position <= 4; position++) {
			field.cursorPosition(position);
			Assert.assertEquals(12D, this.cursor().getLeft(), 1E-5D);
		}
		field.cursorPosition(5);
		Assert.assertEquals(27D, this.cursor().getLeft(), 1E-5D);
		field.cursorPosition(11);
		Assert.assertEquals(52D, this.cursor().getLeft(), 1E-5D);
	}

	@Test
	public void drawsTheCursorAfterEachCharacterOfALiteralTagWithoutMarkup() {
		final MultilineTextFieldNode field = this.field("a<b>bc</b>d", false);
		field.cursorPosition(3);
		Assert.assertEquals(32D, this.cursor().getLeft(), 1E-5D);
	}

	@Test
	public void movesThroughEveryPositionOfATagWithTheArrows() {
		final MultilineTextFieldNode field = this.field("a<b>bc</b>d", true).cursorPosition(1);
		for (int position = 2; position <= 5; position++) {
			this.press(field, Key.RIGHT);
			Assert.assertEquals(position, field.getCursorPos());
		}
		this.press(field, Key.LEFT);
		Assert.assertEquals(4, field.getCursorPos());
	}

	@Test
	public void goesToTheStartAndTheEndOfItsTextWithHomeAndEnd() {
		for (final boolean markup : new boolean[] {true, false}) {
			final MultilineTextFieldNode field = this.field("<b>aaaa bbbbb\ncc</b>", markup).cursorPosition(5);
			this.press(field, Key.END);
			Assert.assertEquals(20, field.getCursorPos());
			this.press(field, Key.HOME);
			Assert.assertEquals(0, field.getCursorPos());
		}
	}

	@Test
	public void keepsTheCursorColumnOnTheVisibleWidthWithUpAndDown() {
		final MultilineTextFieldNode field = this.field("<b>aaaa bbbbb cc</b>", true).cursorPosition(5);
		this.press(field, Key.DOWN);
		Assert.assertEquals(10, field.getCursorPos());
		this.press(field, Key.DOWN);
		Assert.assertEquals(16, field.getCursorPos());
		this.press(field, Key.UP);
		Assert.assertEquals(10, field.getCursorPos());
		this.press(field, Key.UP);
		Assert.assertEquals(5, field.getCursorPos());
	}

	@Test
	public void keepsTheCursorColumnWithUpAndDownWithoutMarkup() {
		final MultilineTextFieldNode field = this.field("ab<b>\ncd<b>ef", false).cursorPosition(4);
		this.press(field, Key.DOWN);
		Assert.assertEquals(10, field.getCursorPos());
		this.press(field, Key.UP);
		Assert.assertEquals(4, field.getCursorPos());
	}

	@Test
	public void selectsAcrossATagWithItsZeroWidth() {
		final MultilineTextFieldNode field = this.field("a<b>bc</b>d", true).cursorPosition(0);
		this.bridges.getWindow().getKeys().add(Key.LEFT_SHIFT);
		for (int i = 0; i < 6; i++) {
			this.press(field, Key.RIGHT);
		}
		this.bridges.getWindow().getKeys().remove(Key.LEFT_SHIFT);
		this.bridges.frame();
		final List<Draw> selections = this.selections();
		Assert.assertEquals(1, selections.size());
		Assert.assertEquals(2D, selections.get(0).getLeft(), 1E-5D);
		Assert.assertEquals(42D, selections.get(0).getRight(), 1E-5D);
	}

	@Test
	public void selectsAcrossWrappedLinesOfAStyledText() {
		final MultilineTextFieldNode field = this.field("<b>aaaa bbbbb cc</b>", true).cursorPosition(5);
		this.bridges.getWindow().getKeys().add(Key.LEFT_SHIFT);
		this.press(field, Key.DOWN);
		this.bridges.getWindow().getKeys().remove(Key.LEFT_SHIFT);
		this.bridges.frame();
		final List<Draw> selections = this.selections();
		Assert.assertEquals(2, selections.size());
		Assert.assertEquals(32D, selections.get(0).getLeft(), 1E-5D);
		Assert.assertEquals(62D, selections.get(0).getRight(), 1E-5D);
		Assert.assertEquals(2D, selections.get(1).getLeft(), 1E-5D);
		Assert.assertEquals(32D, selections.get(1).getRight(), 1E-5D);
	}

	@Test
	public void typesInsideTheStyleRightAfterATagAndOutsideItRightBefore() {
		final MultilineTextFieldNode field = this.field("a<b>bc</b>d", true).cursorPosition(4);
		this.type(field, "x");
		Assert.assertEquals("a<b>xbc</b>d", field.getText());
		Assert.assertEquals(5, field.getCursorPos());
		field.cursorPosition(1);
		this.type(field, "y");
		Assert.assertEquals("ay<b>xbc</b>d", field.getText());
		Assert.assertEquals(Arrays.asList("ay<b>xbc</b>d"), this.lines(field));
	}

	@Test
	public void typesInsideATagLikeAnyText() {
		final MultilineTextFieldNode field = this.field("a<b>bc</b>d", true).cursorPosition(2);
		this.type(field, "z");
		Assert.assertEquals("a<zb>bc</b>d", field.getText());
		field.cursorPosition(3);
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("a<b>bc</b>d", field.getText());
		Assert.assertEquals(2, field.getCursorPos());
	}

	@Test
	public void showsATagBrokenByADeletionAsText() {
		final MultilineTextFieldNode field = this.field("a<b>bc</b>d", true).cursorPosition(3);
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("a<>bc</b>d", field.getText());
		field.cursorPosition(9);
		Assert.assertEquals(52D, this.cursor().getLeft(), 1E-5D);
	}

	@Test
	public void deletesTheCharacterNextToATag() {
		final MultilineTextFieldNode field = this.field("a<b>bc</b>d", true).cursorPosition(4);
		this.press(field, Key.DELETE);
		Assert.assertEquals("a<b>c</b>d", field.getText());
		this.press(field, Key.BACKSPACE);
		Assert.assertEquals("a<bc</b>d", field.getText());
		Assert.assertEquals(3, field.getCursorPos());
	}

	@Test
	public void keepsTheStyleOfALineBreakTypedInsideIt() {
		final MultilineTextFieldNode field = this.field("<b>abcd</b>", true).cursorPosition(5);
		this.press(field, Key.ENTER);
		Assert.assertEquals("<b>ab\ncd</b>", field.getText());
		Assert.assertEquals(Arrays.asList("<b>ab", "<b>cd</b>"), this.lines(field));
		Assert.assertEquals(6, field.getCursorPos());
		Assert.assertEquals(2D, this.cursor().getLeft(), 1E-5D);
		Assert.assertEquals(22D, this.cursor().getTop(), 1E-5D);
	}

	private MultilineTextFieldNode field(final String text, final boolean markup) {
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, 104D, 200D).info(TextInfo.create(this.font, 10F, MultilineTextFieldMarkupTest.INK).markups(MultilineTextFieldMarkupTest.MARKUP)).markup(markup).text(text).focused(true);
		this.bridges.open(new NodeUI(field));
		return field;
	}

	private List<String> lines(final MultilineTextFieldNode field) {
		this.font.getDrawn().clear();
		this.bridges.frame();
		return new ArrayList<>(this.font.getDrawn());
	}

	private int click(final MultilineTextFieldNode field, final double x) {
		this.bridges.move(2D + x, 5D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		return field.getCursorPos();
	}

	private void press(final MultilineTextFieldNode field, final Key key) {
		field.keyPressed(' ', key, InternalContext.create());
	}

	private void type(final MultilineTextFieldNode field, final String text) {
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

	public static final class MarkupFont implements IFont, IFontProvider {

		@Getter
		private final List<String> drawn = new ArrayList<>();

		@Override
		public IFontProvider getFontProvider() {
			return this;
		}

		@Override
		public FontBounds drawText(final double x, final double y, final String text, final TextInfo info) {
			this.drawn.add(text);
			return new FontBounds(this.getWidth(text, info), this.getHeight(text, info));
		}

		@Override
		public double getWidth(final String text, final TextInfo info) {
			final TextStyle style = info.getStyle().derive();
			double width = 0D;
			for (int index = 0; index < text.length();) {
				final int consumed = TextMarkup.parse(info.getMarkups(), text, index, style);
				if (consumed > 0) {
					index += consumed;
					continue;
				}

				width += style.getWeight() == FontWeight.BOLD ? 15D : 10D;
				index++;
			}
			return width;
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

}