package dev.joid.lib.ui.node.impl.design.textfield;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.TextMode;
import dev.joid.lib.draw.text.builder.TextOverflow;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.utils.align.Align;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@SuppressWarnings("unchecked")
public class MultilineTextFieldNode extends FieldNode<String> {

	private double                yOffset;
	private UnaryOperator<String> formatter;

	protected MultilineTextFieldNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		super.margin(2D);
		super.cursorMargin(-1D);
		this.formatter = UnaryOperator.identity();
	}

	public static @NonNull MultilineTextFieldNode create(final double x, final double y, final double width, final double height) {
		return new MultilineTextFieldNode(x, y, width, height);
	}

	@Override
	protected void drawField() {
		final String text = super.getText();
		final TextInfo info = super.getInfo();
		final double maxWidth = this.getRawWidth();
		final FieldLayout layout = this.getLayout();
		final List<FieldLine> lines = layout.getLines();
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
				final FieldLine line = lines.get(i);
				DrawUtils.TEXT.drawText(textX, textY + lineHeight * i, line.getPrefix() + text.substring(line.getStart(), line.getEnd()), super.getShownInfo(), Align.START, Align.START);
			}

			if (super.isFocused()) {
				final int[] cursorLineCol = this.getLineAndColumn(layout, super.getCursorPos());
				final double cursorX = textX + (lines.isEmpty() ? 0D : this.getX(layout, lines.get(cursorLineCol[0]), super.getCursorPos()));
				final double cursorY = textY + lineHeight * cursorLineCol[0];
				final float cursorOpacity = (float) ((Math.sin(2 * Math.PI * (BridgeHandler.CLOCK.get().currentTimeMillis() % 2000) / 1000) + 1) / 2F);
				final Color cursorColor = new Color(info.getColor().r, info.getColor().g, info.getColor().b, cursorOpacity);
				DrawUtils.SHAPE.drawRect(cursorX, cursorY, 2, lineHeight, cursorColor);
			}

			if (super.getSelectionStart() != -1 && !lines.isEmpty()) {
				final Color selectionColor = new Color(50, 152, 253, 100);
				final int start = Math.min(super.getCursorPos(), super.getSelectionStart());
				final int end = Math.max(super.getCursorPos(), super.getSelectionStart());
				final int startLine = this.getLineAndColumn(layout, start)[0];
				final int endLine = this.getLineAndColumn(layout, end)[0];
				for (int i = startLine; i <= endLine; i++) {
					final FieldLine line = lines.get(i);
					final int from = i == startLine ? Math.min(start, line.getEnd()) : line.getStart();
					final int to = i == endLine ? Math.min(end, line.getEnd()) : line.getEnd();
					final double selectionX = this.getX(layout, line, from);
					final double selectionWidth = this.getX(layout, line, to) - selectionX;
					DrawUtils.SHAPE.drawRect(textX + selectionX, textY + lineHeight * i, selectionWidth <= 0D ? 2 : selectionWidth, lineHeight, selectionColor);
				}
			}
		});
	}

	@Override
	protected final String parse(final @NonNull String text) {
		return this.formatter.apply(text);
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
			final int[] cursorLineCol = this.getLineAndColumn(layout, super.getCursorPos());
			final int newLineIdx = cursorLineCol[0] + (key == Key.UP ? -1 : 1);
			if (newLineIdx < 0 || newLineIdx >= lines.size()) {
				return true;
			}

			final double cursorX = this.getX(layout, lines.get(cursorLineCol[0]), super.getCursorPos());
			final int newCursorPos = this.getPosition(layout, lines.get(newLineIdx), cursorX);
			super.updateSelection();
			if (key == Key.UP) {
				super.decreaseCursor(super.getCursorPos() - newCursorPos);
			} else {
				super.increaseCursor(newCursorPos - super.getCursorPos());
			}
			return true;
		}

		if (key == Key.ESCAPE) {
			super.restore();
			super.focus(false);
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
		final String beforeCursor = super.getText().substring(0, super.getCursorPos());
		final double cursorY = super.getY() + super.getMarginTop() + lineHeight * (this.getLayout(beforeCursor).getLines().size() - 1);
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
		final double cursorY = super.getY() + super.getMarginTop() + lineHeight * (this.getLayout(beforeCursor).getLines().size() - 1);
		if (cursorY - this.yOffset < super.getY() + super.getMarginTop() + super.getCursorMargin()) {
			this.yOffset = cursorY - super.getY() - super.getMarginTop() - super.getCursorMargin();
		}
	}

	@Override
	public void mouseScroll(final double mouseX, final double mouseY, final double notchesX, final double notchesY, final @NonNull DispatchContext context) {
		if (context.isCancelled() || notchesY == 0D || !super.isHovered(mouseX, mouseY)) {
			return;
		}

		final double lineHeight = this.getLineHeight();
		final double maxOffset = Math.max(0D, this.getLayout().getLines().size() * lineHeight - this.getRawHeight());
		final double offset = notchesY > 0D ? Math.max(0D, this.yOffset - lineHeight) : Math.min(maxOffset, this.yOffset + lineHeight);
		if (notchesY > 0D ? offset < this.yOffset : offset > this.yOffset) {
			context.cancel(() -> {
				this.yOffset = offset;
			});
		}
	}

	public final <T extends MultilineTextFieldNode> @NonNull T format(final @NonNull UnaryOperator<@NonNull String> formatter) {
		this.formatter = formatter;
		return (T) this;
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

	private final @NonNull FieldLayout getLayout() {
		return this.getLayout(super.getText());
	}

	private final @NonNull FieldLayout getLayout(final @NonNull String text) {
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
				for (int j = i; j >= start; j--) {
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

	private final int[] getLineAndColumn(final @NonNull FieldLayout layout, final int pos) {
		final List<FieldLine> lines = layout.getLines();
		if (lines.isEmpty()) {
			return new int[] {0, 0};
		}

		for (int lineIdx = 0; lineIdx < lines.size(); lineIdx++) {
			final FieldLine line = lines.get(lineIdx);
			if (pos >= line.getStart() && pos <= line.getEnd()) {
				return new int[] {lineIdx, pos - line.getStart()};
			}
		}

		final FieldLine lastLine = lines.get(lines.size() - 1);
		return new int[] {lines.size() - 1, lastLine.getEnd() - lastLine.getStart()};
	}

	private final double getX(final @NonNull FieldLayout layout, final @NonNull FieldLine line, final int pos) {
		final int clamped = Math.max(line.getStart(), Math.min(pos, line.getEnd()));
		final int end = clamped < layout.getText().length() && layout.getTags()[clamped] != -1 ? layout.getTags()[clamped] : clamped;
		return super.getShownInfo().getWidth(line.getPrefix() + layout.getText().substring(line.getStart(), Math.max(line.getStart(), end)));
	}

	private final int getPosition(final @NonNull FieldLayout layout, final @NonNull FieldLine line, final double x) {
		final String text = layout.getText();
		int previous = line.getStart();
		for (int i = line.getStart(); i < line.getEnd(); i++) {
			if (layout.getTags()[i] != -1) {
				continue;
			}

			final double charWidth = super.getShownInfo().getWidth(FieldNode.opened(text, layout.getTags(), i) + text.charAt(i));
			if (x < this.getX(layout, line, i) + charWidth / 2) {
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