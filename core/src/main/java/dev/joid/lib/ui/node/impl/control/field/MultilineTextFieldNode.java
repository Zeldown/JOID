package dev.joid.lib.ui.node.impl.control.field;

import java.util.ArrayList;
import java.util.List;

import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.TextMode;
import dev.joid.lib.draw.text.TextOverflow;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.ui.event.impl.node.NodeMouseScrollEvent;
import dev.joid.lib.utils.align.Align;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public class MultilineTextFieldNode extends FieldNode<String> {

	private double yOffset;

	protected MultilineTextFieldNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		super.margin(2D);
		super.cursorMargin(() -> super.getInfo() == null ? 0D : this.getLineHeight() * 2D);
	}

	public static @NonNull MultilineTextFieldNode create(final double x, final double y, final double width, final double height) {
		return new MultilineTextFieldNode(x, y, width, height);
	}

	@Override
	protected void drawField() {
		final String text = super.getText();
		final double maxWidth = this.getRawWidth();
		final FieldLayout layout = this.getLayout();
		final List<FieldLine> lines = layout.getLines();
		final double lineHeight = this.getLineHeight();
		if (this.yOffset > lines.size() * lineHeight) {
			this.followCursorBackward();
		}

		if (this.yOffset < 0D) {
			this.yOffset = 0D;
		}

		final double textX = super.getX() + super.getMarginLeft();
		final double textY = super.getY() + super.getMarginTop() - this.yOffset;

		super.getUi().mask(super.getX() + super.getMarginLeft(), super.getY() + super.getMarginTop(), maxWidth, this.getRawHeight(), () -> {
			if (text.isEmpty() && !super.isFocused()) {
				DrawUtils.TEXT.drawText(textX, textY, maxWidth, super.getHeight(), super.getPlaceholder(), super.getPlaceholderInfo(), Align.START, Align.START, TextOverflow.NONE, TextMode.SPLIT);
			}

			for (int i = 0; i < lines.size(); i++) {
				final FieldLine line = lines.get(i);
				DrawUtils.TEXT.drawText(textX, textY + lineHeight * i, line.getPrefix() + text.substring(line.getStart(), line.getEnd()), super.getShownInfo(), Align.START, Align.START);
			}

			if (super.isFocused()) {
				final int cursorLine = this.getLineIndex(layout, super.getCursorPosition());
				final double cursorX = textX + (lines.isEmpty() ? 0D : this.getX(layout, lines.get(cursorLine), super.getCursorPosition()));
				DrawUtils.SHAPE.drawRect(cursorX, textY + lineHeight * cursorLine, 2D, lineHeight, super.getShownCursorColor());
			}

			final FieldSelection selection = super.getSelection();
			if (!selection.isEmpty() && !lines.isEmpty()) {
				final int start = selection.getStart();
				final int end = selection.getEnd();
				final int startLine = this.getLineIndex(layout, start);
				final int endLine = this.getLineIndex(layout, end);
				for (int i = startLine; i <= endLine; i++) {
					final FieldLine line = lines.get(i);
					final int from = i == startLine ? Math.min(start, line.getEnd()) : line.getStart();
					final int to = i == endLine ? Math.min(end, line.getEnd()) : line.getEnd();
					final double selectionX = this.getX(layout, line, from);
					final double selectionWidth = this.getX(layout, line, to) - selectionX;
					DrawUtils.SHAPE.drawRect(textX + selectionX, textY + lineHeight * i, selectionWidth <= 0D ? 2D : selectionWidth, lineHeight, super.getSelectionColor());
				}
			}
		});
	}

	@Override
	protected final String parse(final @NonNull String text) {
		return text;
	}

	@Override
	protected final @NonNull String format(final @NonNull String value) {
		return value;
	}

	@Override
	protected final boolean isMultiline() {
		return true;
	}

	@Override
	protected final boolean handleKey(final @NonNull Key key) {
		if (key == Key.UP || key == Key.DOWN) {
			final FieldLayout layout = this.getLayout();
			final List<FieldLine> lines = layout.getLines();
			final int cursorLine = this.getLineIndex(layout, super.getCursorPosition());
			final int newLineIdx = cursorLine + (key == Key.UP ? -1 : 1);
			if (newLineIdx < 0 || newLineIdx >= lines.size()) {
				return true;
			}

			final double cursorX = this.getX(layout, lines.get(cursorLine), super.getCursorPosition());
			final int newCursorPos = this.getPosition(layout, lines.get(newLineIdx), cursorX);
			super.placeCursor(newCursorPos, key == Key.DOWN);
			return true;
		}

		if (key == Key.ENTER || key == Key.NUMPAD_ENTER) {
			super.insert("\n");
			return true;
		}

		return false;
	}

	@Override
	protected final int getPositionAt(final double mouseX, final double mouseY) {
		final FieldLayout layout = this.getLayout();
		final List<FieldLine> lines = layout.getLines();
		final double lineHeight = this.getLineHeight();
		if (lines.isEmpty()) {
			return 0;
		}

		int lineIndex = -1;
		for (int i = 0; i < lines.size(); i++) {
			final double startLineY = super.getAbsoluteY() + super.getMarginTop() - this.yOffset + lineHeight * i;
			final double endLineY = startLineY + lineHeight;
			if (mouseY >= startLineY && mouseY <= endLineY) {
				lineIndex = i;
				break;
			}
		}

		if (lineIndex < 0) {
			final double firstLineY = super.getAbsoluteY() + super.getMarginTop() - this.yOffset;
			lineIndex = mouseY < firstLineY ? 0 : lines.size() - 1;
		}

		return this.getPosition(layout, lines.get(lineIndex), mouseX - super.getAbsoluteX() - super.getMarginLeft());
	}

	@Override
	protected final void followCursorForward() {
		final double lineHeight = this.getLineHeight();
		final FieldLayout layout = this.getLayout();
		final int cursorLine = this.getLineIndex(layout, super.getCursorPosition());
		final double cursorY = super.getY() + super.getMarginTop() + lineHeight * cursorLine;
		final double margin = Math.min(super.getCursorMargin(), lineHeight * (layout.getLines().size() - 1 - cursorLine));
		if (cursorY + lineHeight + margin - this.yOffset > super.getY() + super.getHeight() - super.getMarginBottom()) {
			this.yOffset = cursorY + lineHeight + margin - super.getY() - super.getHeight() + super.getMarginBottom();
		}
	}

	@Override
	protected final void followCursorBackward() {
		if (super.getCursorPosition() == 0) {
			this.yOffset = 0D;
			return;
		}

		final double cursorY = super.getY() + super.getMarginTop() + this.getLineHeight() * this.getLineIndex(this.getLayout(), super.getCursorPosition());
		if (cursorY - this.yOffset < super.getY() + super.getMarginTop() + super.getCursorMargin()) {
			this.yOffset = cursorY - super.getY() - super.getMarginTop() - super.getCursorMargin();
		}
	}

	@Override
	public void mouseScroll(final @NonNull NodeMouseScrollEvent<?> event) {
		final double notchesY = event.getNotchesY();
		if (event.isConsumed() || notchesY == 0D || !super.isHovered()) {
			return;
		}

		final double lineHeight = this.getLineHeight();
		final double maxOffset = Math.max(0D, this.getLayout().getLines().size() * lineHeight - this.getRawHeight());
		final double offset = notchesY > 0D ? Math.max(0D, this.yOffset - lineHeight) : Math.min(maxOffset, this.yOffset + lineHeight);
		if (notchesY > 0D ? offset < this.yOffset : offset > this.yOffset) {
			this.yOffset = offset;
			event.consume();
		}
	}

	private double getRawWidth() {
		return super.getWidth() - super.getMarginLeft() - super.getMarginRight();
	}

	private double getRawHeight() {
		return super.getHeight() - super.getMarginTop() - super.getMarginBottom();
	}

	private double getLineHeight() {
		return super.getInfo().getHeight();
	}

	private @NonNull FieldLayout getLayout() {
		final String text = super.getText();
		final TextInfo info = super.getShownInfo();
		final int[] tags = FieldNode.tags(text, info);
		final List<FieldLine> lines = new ArrayList<>();
		final FieldLayout layout = new FieldLayout(text, tags, lines);
		if (text.isEmpty()) {
			return layout;
		}

		int paragraphStart = 0;
		while (true) {
			final int newLine = text.indexOf('\n', paragraphStart);
			final int paragraphEnd = newLine < 0 ? text.length() : newLine;
			boolean wrapped = false;
			int start = paragraphStart;
			String prefix = FieldNode.opened(text, tags, start);
			for (int i = paragraphStart; i < paragraphEnd; i++) {
				if (tags[i] != -1 || info.getWidth(prefix + text.substring(start, i + 1)) <= this.getRawWidth()) {
					continue;
				}

				int split = -1;
				for (int j = i; j > start; j--) {
					if (text.charAt(j) == ' ' && tags[j] == -1) {
						split = j;
						break;
					}
				}

				if (split == -1) {
					split = i;
					while (split > start && tags[split - 1] != -1) {
						split = tags[split - 1];
					}
				}

				if (split > start) {
					lines.add(new FieldLine(start, split, prefix));
					start = text.charAt(split) == ' ' ? split + 1 : split;
					prefix = FieldNode.opened(text, tags, start);
					wrapped = true;
				}
			}

			if (newLine >= 0 || start < paragraphEnd || paragraphStart > 0 && !wrapped) {
				lines.add(new FieldLine(start, paragraphEnd, prefix));
			}

			if (newLine < 0) {
				return layout;
			}

			paragraphStart = newLine + 1;
		}
	}

	private int getLineIndex(final @NonNull FieldLayout layout, final int pos) {
		final List<FieldLine> lines = layout.getLines();
		for (int lineIdx = 0; lineIdx < lines.size(); lineIdx++) {
			final FieldLine line = lines.get(lineIdx);
			if (pos >= line.getStart() && pos <= line.getEnd()) {
				return lineIdx;
			}
		}

		return Math.max(0, lines.size() - 1);
	}

	private double getX(final @NonNull FieldLayout layout, final @NonNull FieldLine line, final int pos) {
		final int clamped = Math.max(line.getStart(), Math.min(pos, line.getEnd()));
		final int end = clamped < layout.getText().length() && layout.getTags()[clamped] != -1 ? layout.getTags()[clamped] : clamped;
		return super.getShownInfo().getWidth(line.getPrefix() + layout.getText().substring(line.getStart(), Math.max(line.getStart(), end)));
	}

	private int getPosition(final @NonNull FieldLayout layout, final @NonNull FieldLine line, final double x) {
		final String text = layout.getText();
		int previous = line.getStart();
		for (int i = line.getStart(); i < line.getEnd(); i++) {
			if (layout.getTags()[i] != -1) {
				continue;
			}

			final double charWidth = super.getShownInfo().getWidth(FieldNode.opened(text, layout.getTags(), i) + text.charAt(i));
			if (x < this.getX(layout, line, i) + charWidth / 2D) {
				return previous;
			}

			previous = i + 1;
		}

		return previous;
	}

	@Getter
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class FieldLayout {

		private final String          text;
		private final int[]           tags;
		private final List<FieldLine> lines;

	}

	@Getter
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class FieldLine {

		private final int    start;
		private final int    end;
		private final String prefix;

	}

}