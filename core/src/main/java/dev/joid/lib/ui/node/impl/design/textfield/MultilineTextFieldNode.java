package dev.joid.lib.ui.node.impl.design.textfield;

import java.util.ArrayList;
import java.util.List;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.builder.utils.TextOverflow;
import dev.joid.lib.draw.text.utils.TextMode;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.key.Key;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class MultilineTextFieldNode extends FieldNode<String, MultilineTextFieldNode> {

	private double yOffset;

	protected MultilineTextFieldNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		super.margin(2D);
		super.cursorMargin(-1D);
	}

	public static @NonNull MultilineTextFieldNode create(final double x, final double y, final double width, final double height) {
		return new MultilineTextFieldNode(x, y, width, height);
	}

	@Override
	protected final void drawField() {
		final String text = super.getText();
		final TextInfo info = super.getInfo();
		final double maxWidth = this.getRawWidth();
		final List<String> lines = this.getLines();
		final double lineHeight = this.getLineHeight();
		if (super.getCursorMargin() == -1D) {
			super.cursorMargin(lineHeight * 2);
		}

		if (this.yOffset > lines.size() * lineHeight) {
			super.decreaseCursor(0);
		}

		if (this.yOffset < 0) {
			this.yOffset = 0;
		}

		this.yOffset = super.getUi().lerpByFramerate(this.yOffset, this.yOffset, 0.5D, 0.2D, true);

		final double textX = super.getX() + super.getMarginLeft();
		final double textY = super.getY() + super.getMarginTop() - this.yOffset;

		super.getUi().mask(super.getX() + super.getMarginLeft(), super.getY() + super.getMarginTop(), maxWidth, this.getRawHeight(), () -> {
			if (text.isEmpty() && !super.isFocused()) {
				DrawUtils.TEXT.drawText(textX, textY, maxWidth, super.getHeight(), super.getPlaceholder(), super.getShownInfo().copy().color(new Color(info.getColor().r, info.getColor().g, info.getColor().b, 0.5F)), Align.START, Align.START, TextOverflow.NONE, TextMode.SPLIT);
			}

			for (int i = 0; i < lines.size(); i++) {
				DrawUtils.TEXT.drawText(textX, textY + lineHeight * i, lines.get(i), super.getShownInfo(), Align.START, Align.START);
			}

			if (super.isFocused()) {
				final int[] cursorLineCol = this.getLineAndColumn(super.getCursorPos());
				final int cursorLineIdx = cursorLineCol[0];
				final int cursorCol = cursorLineCol[1];
				final String cursorLine = lines.isEmpty() ? "" : lines.get(cursorLineIdx).replace("\n", "").replace("\r", "");
				final int safeCol = Math.min(cursorCol, cursorLine.length());
				final double cursorX = textX + this.getTextWidth(cursorLine.substring(0, safeCol));
				final double cursorY = textY + lineHeight * cursorLineIdx;
				final Color cursorColor = new Color(info.getColor());
				final float cursorOpacity = (float) ((Math.sin(2 * Math.PI * (BridgeHandler.CLOCK.get().currentTimeMillis() % 2000) / 1000) + 1) / 2F);
				cursorColor.a = cursorOpacity;
				DrawUtils.SHAPE.drawRect(cursorX, cursorY, 2, lineHeight, cursorColor);
			}

			if (super.getSelectionStart() != -1 && !lines.isEmpty()) {
				final Color selectionColor = new Color(50, 152, 253, 100);
				final int start = Math.min(super.getCursorPos(), super.getSelectionStart());
				final int end = Math.max(super.getCursorPos(), super.getSelectionStart());

				final int[] startLineCol = this.getLineAndColumn(start);
				final int[] endLineCol = this.getLineAndColumn(end);
				final int startLine = startLineCol[0];
				final int startChar = startLineCol[1];
				final int endLine = endLineCol[0];
				final int endChar = endLineCol[1];

				if (startLine == endLine) {
					final String line = lines.get(startLine).replace("\n", "").replace("\r", "");
					final int safeStart = Math.min(startChar, line.length());
					final int safeEnd = Math.min(endChar, line.length());
					final double selectionX = textX + this.getTextWidth(line.substring(0, safeStart));
					final double selectionY = textY + lineHeight * startLine;
					final String subLine = line.substring(safeStart, safeEnd);
					final double selectionWidth = subLine.isEmpty() ? 2 : this.getTextWidth(subLine);
					DrawUtils.SHAPE.drawRect(selectionX, selectionY, selectionWidth, lineHeight, selectionColor);
				} else {
					for (int i = startLine; i <= endLine; i++) {
						final String line = lines.get(i).replace("\n", "").replace("\r", "");
						final boolean isStart = i == startLine;
						final boolean isEnd = i == endLine;

						if (isStart) {
							final int safeStart = Math.min(startChar, line.length());
							final double selectionX = textX + this.getTextWidth(line.substring(0, safeStart));
							final double selectionY = textY + lineHeight * i;
							final String subLine = line.substring(safeStart);
							final double selectionWidth = subLine.isEmpty() ? 2 : this.getTextWidth(subLine);
							DrawUtils.SHAPE.drawRect(selectionX, selectionY, selectionWidth, lineHeight, selectionColor);
							continue;
						}

						if (isEnd) {
							final int safeEnd = Math.min(endChar, line.length());
							final double selectionX = textX;
							final double selectionY = textY + lineHeight * i;
							final String subLine = line.substring(0, safeEnd);
							final double selectionWidth = subLine.isEmpty() ? 2 : this.getTextWidth(subLine);
							DrawUtils.SHAPE.drawRect(selectionX, selectionY, selectionWidth, lineHeight, selectionColor);
							continue;
						}

						final double selectionX = textX;
						final double selectionY = textY + lineHeight * i;
						final double selectionWidth = line.isEmpty() ? 2 : this.getTextWidth(line);
						DrawUtils.SHAPE.drawRect(selectionX, selectionY, selectionWidth, lineHeight, selectionColor);
					}
				}
			}
		});
	}

	@Override
	protected final boolean isMultiline() {
		return true;
	}

	@Override
	protected final boolean handleKey(final @NonNull Key key) {
		if (key == Key.UP) {
			final int[] cursorLineCol = this.getLineAndColumn(super.getCursorPos());
			if (cursorLineCol[0] <= 0) {
				return true;
			}

			final List<String> lines = this.getLines();
			final String currentLine = lines.get(cursorLineCol[0]).replace("\n", "").replace("\r", "");
			final int currentCol = Math.min(cursorLineCol[1], currentLine.length());
			final double cursorX = super.getAbsoluteX() + super.getMarginLeft() + this.getTextWidth(currentLine.substring(0, currentCol));

			final int newLineIdx = cursorLineCol[0] - 1;
			final String targetLine = lines.get(newLineIdx).replace("\n", "").replace("\r", "");
			int newCol = targetLine.length();
			for (int i = 0; i < targetLine.length(); i++) {
				final double colX = super.getAbsoluteX() + super.getMarginLeft() + this.getTextWidth(targetLine.substring(0, i)) + this.getTextWidth(targetLine.substring(i, i + 1)) / 2D;
				if (cursorX < colX) {
					newCol = i;
					break;
				}
			}

			final int newCursorPos = this.getTextPosition(newLineIdx, newCol);
			super.updateSelection();
			super.decreaseCursor(super.getCursorPos() - newCursorPos);
			return true;
		}

		if (key == Key.DOWN) {
			final List<String> lines = this.getLines();
			final int[] cursorLineCol = this.getLineAndColumn(super.getCursorPos());
			if (cursorLineCol[0] >= lines.size() - 1) {
				return true;
			}

			final String currentLine = lines.get(cursorLineCol[0]).replace("\n", "").replace("\r", "");
			final int currentCol = Math.min(cursorLineCol[1], currentLine.length());
			final double cursorX = super.getAbsoluteX() + super.getMarginLeft() + this.getTextWidth(currentLine.substring(0, currentCol));

			final int newLineIdx = cursorLineCol[0] + 1;
			final String targetLine = lines.get(newLineIdx).replace("\n", "").replace("\r", "");
			int newCol = targetLine.length();
			for (int i = 0; i < targetLine.length(); i++) {
				final double colX = super.getAbsoluteX() + super.getMarginLeft() + this.getTextWidth(targetLine.substring(0, i)) + this.getTextWidth(targetLine.substring(i, i + 1)) / 2D;
				if (cursorX < colX) {
					newCol = i;
					break;
				}
			}

			final int newCursorPos = this.getTextPosition(newLineIdx, newCol);
			super.updateSelection();
			super.increaseCursor(newCursorPos - super.getCursorPos());
			return true;
		}

		if (key == Key.ESCAPE) {
			super.focused(false);
			return true;
		}

		if (key == Key.ENTER || key == Key.NUMPAD_ENTER) {
			super.insert("\n");
			return true;
		}

		return false;
	}

	@Override
	protected final void placeCursor(final double mouseX, final double mouseY) {
		final List<String> lines = this.getLines();
		final double lineHeight = this.getLineHeight();

		int lineIndex = -1;
		for (int i = 0; i < lines.size(); i++) {
			final double startLineY = super.getAbsoluteY() + super.getMarginTop() - this.yOffset + lineHeight * i;
			final double endLineY = startLineY + lineHeight;
			if (mouseY >= startLineY && mouseY <= endLineY) {
				lineIndex = i;
				break;
			}
		}

		if (lineIndex < 0 && !lines.isEmpty()) {
			final double firstLineY = super.getAbsoluteY() + super.getMarginTop() - this.yOffset;
			lineIndex = mouseY < firstLineY ? 0 : lines.size() - 1;
		}

		if (lineIndex >= 0) {
			final String line = lines.get(lineIndex).replace("\n", "").replace("\r", "");
			int col = line.length();
			for (int i = 0; i < line.length(); i++) {
				final String beforeCursor = line.substring(0, i);
				final String cursorChar = line.substring(i, i + 1);
				final double cursorX = super.getAbsoluteX() + super.getMarginLeft() + this.getTextWidth(beforeCursor) + this.getTextWidth(cursorChar) / 2;
				if (mouseX < cursorX) {
					col = i;
					break;
				}
			}
			super.cursorPosition(this.getTextPosition(lineIndex, col));
		}
	}

	@Override
	protected final void followCursorForward() {
		final double lineHeight = this.getLineHeight();
		final String beforeCursor = super.getText().substring(0, super.getCursorPos());
		final List<String> beforeCursorLines = this.getLines(beforeCursor);
		final double cursorY = super.getY() + super.getMarginTop() + lineHeight * (beforeCursorLines.size() - 1);
		if (cursorY + lineHeight - this.yOffset > super.getY() + super.getHeight() - super.getMarginBottom()) {
			this.yOffset = cursorY + lineHeight - super.getY() - super.getHeight() + super.getMarginBottom();
		}
	}

	@Override
	protected final void followCursorBackward() {
		if (super.getCursorPos() == 0) {
			this.yOffset = 0;
			return;
		}

		final double lineHeight = this.getLineHeight();
		final String beforeCursor = super.getText().substring(0, super.getCursorPos());
		final List<String> beforeCursorLines = this.getLines(beforeCursor);
		final double cursorY = super.getY() + super.getMarginTop() + lineHeight * (beforeCursorLines.size() - 1);
		if (cursorY - this.yOffset < super.getY() + super.getMarginTop() + super.getCursorMargin()) {
			this.yOffset = cursorY - super.getY() - super.getMarginTop() - super.getCursorMargin();
		}
	}

	@Override
	public void mouseScroll(final double mouseX, final double mouseY, final int value, final @NonNull InternalContext context) {
		if (context.isCancelled() || !super.isHovered(mouseX, mouseY)) {
			return;
		}

		context.cancel(() -> {
			final double lineHeight = this.getLineHeight();
			if (value > 0) {
				this.yOffset -= lineHeight;
			} else if (value < 0) {
				this.yOffset += lineHeight;
			}

			if (this.yOffset < 0) {
				this.yOffset = 0;
			}

			final List<String> lines = this.getLines();
			if (this.yOffset > lines.size() * lineHeight - super.getHeight() + super.getMarginTop() + super.getMarginBottom()) {
				this.yOffset = lines.size() * lineHeight - super.getHeight() + super.getMarginTop() + super.getMarginBottom();
			}
		});
	}

	@Override
	public final @NonNull String getValue() {
		return super.getText();
	}

	private final double getRawWidth() {
		return super.getWidth() - super.getMarginLeft() - super.getMarginRight();
	}

	private final double getRawHeight() {
		return super.getHeight() - super.getMarginTop() - super.getMarginBottom();
	}

	private final double getLineHeight() {
		return super.getInfo().getHeight();
	}

	private final @NonNull List<String> getLines() {
		return this.getLines(super.getText());
	}

	private final @NonNull List<String> getLines(final @NonNull String text) {
		final List<String> lines = new ArrayList<>();
		if (text.isEmpty()) {
			return lines;
		}

		final TextInfo info = super.getShownInfo();
		final String[] paragraphs = text.split("\n", -1);
		for (int index = 0; index < paragraphs.length; index++) {
			final String paragraph = paragraphs[index];
			boolean wrapped = false;
			int start = 0;
			for (int i = 0; i < paragraph.length(); i++) {
				if (info.getWidth(paragraph.substring(start, i + 1)) <= this.getRawWidth()) {
					continue;
				}

				int split = i;
				for (int j = i; j >= start; j--) {
					if (paragraph.charAt(j) == ' ') {
						split = j;
						break;
					}
				}

				if (split > start) {
					lines.add(paragraph.substring(start, split));
					start = paragraph.charAt(split) == ' ' ? split + 1 : split;
					wrapped = true;
				}
			}

			if (index < paragraphs.length - 1 || start < paragraph.length() || index > 0 && !wrapped) {
				lines.add(paragraph.substring(start));
			}
		}

		return lines;
	}

	private final int[] getLineAndColumn(final int pos) {
		final String text = super.getText();
		final List<String> lines = this.getLines();
		if (lines.isEmpty()) {
			return new int[] {0, 0};
		}

		int textIdx = 0;
		for (int lineIdx = 0; lineIdx < lines.size(); lineIdx++) {
			if (lineIdx > 0 && textIdx < text.length() && (text.charAt(textIdx) == '\n' || text.charAt(textIdx) == '\r')) {
				textIdx += text.startsWith("\r\n", textIdx) ? 2 : 1;
			}

			final String line = lines.get(lineIdx).replace("\n", "").replace("\r", "");
			final int lineEnd = textIdx + line.length();
			if (pos >= textIdx && pos <= lineEnd) {
				return new int[] {lineIdx, pos - textIdx};
			}

			textIdx = lineEnd;
			if (textIdx < text.length() && text.charAt(textIdx) == ' ' && lineIdx < lines.size() - 1) {
				textIdx++;
			}
		}

		final String lastLine = lines.get(lines.size() - 1).replace("\n", "").replace("\r", "");
		return new int[] {lines.size() - 1, lastLine.length()};
	}

	private final double getTextWidth(final @NonNull String text) {
		return super.getShownInfo().getWidth(text.replace("\n", ""));
	}

	private final int getTextPosition(final int lineIdx, final int col) {
		final String text = super.getText();
		final List<String> lines = this.getLines();
		int textIdx = 0;
		for (int i = 0; i < lineIdx; i++) {
			textIdx += lines.get(i).replace("\n", "").replace("\r", "").length();
			if (textIdx < text.length() && text.charAt(textIdx) == ' ') {
				textIdx++;
			}

			if (textIdx < text.length() && (text.charAt(textIdx) == '\n' || text.charAt(textIdx) == '\r')) {
				textIdx += text.startsWith("\r\n", textIdx) ? 2 : 1;
			}
		}

		return textIdx + Math.min(Math.max(0, col), lines.get(lineIdx).replace("\n", "").replace("\r", "").length());
	}

}