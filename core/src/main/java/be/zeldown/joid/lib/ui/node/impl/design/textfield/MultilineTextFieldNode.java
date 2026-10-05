package be.zeldown.joid.lib.ui.node.impl.design.textfield;

import java.util.List;
import java.util.function.BiFunction;

import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.draw.text.builder.utils.TextOverflow;
import be.zeldown.joid.lib.draw.text.utils.TextMode;
import be.zeldown.joid.lib.font.dto.TextInfo;
import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import be.zeldown.joid.lib.ui.node.impl.design.textfield.callback.NodeTextFieldChangeCallback;
import be.zeldown.joid.lib.ui.node.impl.design.textfield.callback.NodeTextFieldFocusCallback;
import be.zeldown.joid.lib.utils.align.Align;
import be.zeldown.joid.lib.utils.click.ClickType;
import be.zeldown.joid.lib.utils.context.InternalContext;
import be.zeldown.joid.lib.utils.key.Key;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class MultilineTextFieldNode extends Node {

	private static final int CALLBACK_FOCUS  = NodeCallbackRegistry.next(NodeTextFieldFocusCallback.class);
	private static final int CALLBACK_CHANGE = NodeCallbackRegistry.next(NodeTextFieldChangeCallback.class);

	private String   text;
	private TextInfo info;
	private String   placeholder;

	private boolean focused;
	private int maxTextLength;
	private BiFunction<String, String, String> filter;

	private int cursorPos;
	private int selectionStart;

	private double marginTop;
	private double marginLeft;
	private double marginRight;
	private double marginBottom;
	private double cursorMargin;

	private double yOffset;

	private Key     inputType;
	private long    lastInput;
	private boolean inputting;
	private boolean firstInput;

	protected MultilineTextFieldNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.text          = "";
		this.placeholder   = "";
		this.focused       = false;
		this.filter        = (oldText, newText) -> newText;
		this.maxTextLength = -1;

		this.selectionStart = -1;

		this.margin(2D);
		this.cursorMargin(-1D);
	}

	public static @NonNull MultilineTextFieldNode create(final double x, final double y, final double width, final double height) {
		return new MultilineTextFieldNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		final double maxWidth = this.getRawWidth();
		final List<String> lines = this.getLines();
		final double lineHeight = this.getLineHeight();
		if (this.cursorMargin == -1D) {
			this.cursorMargin = lineHeight * 2;
		}

		if (this.yOffset > lines.size() * lineHeight) {
			this.decreaseCursor(0);
		}

		if (this.cursorPos < 0) {
			this.cursorPos = 0;
		}

		if (this.cursorPos > this.text.length()) {
			this.cursorPos = this.text.length();
		}

		if (this.selectionStart > this.text.length()) {
			this.selectionStart = this.text.length();
		}

		if (this.yOffset < 0) {
			this.yOffset = 0;
		}

		this.yOffset = super.getUi().lerpByFramerate(this.yOffset, this.yOffset, 0.5D, 0.2D, true);

		final double textX = super.getX() + this.marginLeft;
		final double textY = super.getY() + this.marginTop - this.yOffset;

		super.getUi().mask(super.getX() + this.marginLeft, super.getY() + this.marginTop, maxWidth, this.getRawHeight(), () -> {
			final boolean isPlaceholder = this.text.isEmpty() && !this.focused;
			DrawUtils.TEXT.drawText(textX, textY, maxWidth, super.getHeight(), isPlaceholder ? this.placeholder : this.text, isPlaceholder ? this.info.copy().color(new Color(this.info.getColor().r, this.info.getColor().g, this.info.getColor().b, 0.5F)) : this.info, Align.START, Align.START, TextOverflow.NONE, TextMode.SPLIT);

			if (this.focused) {
				final int[] cursorLineCol = this.getLineAndColumn(this.cursorPos);
				final int cursorLineIdx = cursorLineCol[0];
				final int cursorCol = cursorLineCol[1];
				final String cursorLine = lines.isEmpty() ? "" : lines.get(cursorLineIdx).replace("\n", "").replace("\r", "");
				final int safeCol = Math.min(cursorCol, cursorLine.length());
				final double cursorX = textX + this.getTextWidth(cursorLine.substring(0, safeCol));
				final double cursorY = textY + lineHeight * cursorLineIdx;
				final Color cursorColor = new Color(this.info.getColor());
				final float cursorOpacity = (float) ((Math.sin(2 * Math.PI * (BridgeHandler.CLOCK.get().currentTimeMillis() % 2000) / 1000) + 1) / 2F);
				cursorColor.a = cursorOpacity;
				DrawUtils.SHAPE.drawRect(cursorX, cursorY, 2, lineHeight, cursorColor);
			}

			if (this.selectionStart != -1) {
				final Color selectionColor = new Color(50, 152, 253, 100);
				final int start = Math.min(this.cursorPos, this.selectionStart);
				final int end = Math.max(this.cursorPos, this.selectionStart);

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

		if (this.inputting && BridgeHandler.CLOCK.get().currentTimeMillis() - this.lastInput >= (this.firstInput ? 500 : 100)) {
			this.firstInput = false;

			if (!this.inputType.isDown()) {
				this.inputting = false;
				return;
			}

			if (this.inputType == Key.DELETE) {
				if (this.cursorPos >= this.text.length()) {
					this.inputting = false;
					return;
				}

				this.setText(this.text.substring(0, this.cursorPos) + this.text.substring(Key.LEFT_CONTROL.isDown() ? this.nextWordIndex() : this.cursorPos + 1));
			} else if (this.inputType == Key.BACKSPACE) {
				if (this.cursorPos <= 0) {
					this.inputting = false;
					return;
				}

				final int backStart = Key.LEFT_CONTROL.isDown() ? this.previousWordIndex() : this.cursorPos - 1;
				this.setText(this.text.substring(0, backStart) + this.text.substring(this.cursorPos));
				this.decreaseCursor(this.cursorPos - backStart);
			} else if (this.inputType == Key.LEFT) {
				this.decreaseCursor(Key.LEFT_CONTROL.isDown() ? this.cursorPos - this.previousWordIndex() : 1);
			} else if (this.inputType == Key.RIGHT) {
				this.increaseCursor(Key.LEFT_CONTROL.isDown() ? this.nextWordIndex() - this.cursorPos : 1);
			}

			this.lastInput = BridgeHandler.CLOCK.get().currentTimeMillis();
		}
	}

	@Override
	public final void keyPressed(final char c, final @NonNull Key key, final @NonNull InternalContext context) {
		if (context.isCancelled() || !this.focused) {
			return;
		}

		context.cancel(() -> {
			if (key == Key.UP) {
				final int[] cursorLineCol = this.getLineAndColumn(this.cursorPos);
				if (cursorLineCol[0] <= 0) {
					return;
				}

				final List<String> lines = this.getLines();
				final String currentLine = lines.get(cursorLineCol[0]).replace("\n", "").replace("\r", "");
				final int currentCol = Math.min(cursorLineCol[1], currentLine.length());
				final double cursorX = super.getAbsoluteX() + this.marginLeft + this.getTextWidth(currentLine.substring(0, currentCol));

				final int newLineIdx = cursorLineCol[0] - 1;
				final String targetLine = lines.get(newLineIdx).replace("\n", "").replace("\r", "");
				int newCol = targetLine.length();
				for (int i = 0; i < targetLine.length(); i++) {
					final double colX = super.getAbsoluteX() + this.marginLeft + this.getTextWidth(targetLine.substring(0, i));
					if (cursorX < colX) {
						newCol = i;
						break;
					}
				}

				final int newCursorPos = this.getTextPosition(newLineIdx, newCol);
				if (Key.LEFT_SHIFT.isDown() || Key.RIGHT_SHIFT.isDown()) {
					if (this.selectionStart == -1) {
						this.selectionStart = this.cursorPos;
					}
				} else {
					this.selectionStart = -1;
				}

				if (newCursorPos < this.cursorPos) {
					this.decreaseCursor(this.cursorPos - newCursorPos);
				} else {
					this.increaseCursor(newCursorPos - this.cursorPos);
				}

				return;
			}

			if (key == Key.DOWN) {
				final List<String> lines = this.getLines();
				final int[] cursorLineCol = this.getLineAndColumn(this.cursorPos);
				if (cursorLineCol[0] >= lines.size() - 1) {
					return;
				}

				final String currentLine = lines.get(cursorLineCol[0]).replace("\n", "").replace("\r", "");
				final int currentCol = Math.min(cursorLineCol[1], currentLine.length());
				final double cursorX = super.getAbsoluteX() + this.marginLeft + this.getTextWidth(currentLine.substring(0, currentCol));

				final int newLineIdx = cursorLineCol[0] + 1;
				final String targetLine = lines.get(newLineIdx).replace("\n", "").replace("\r", "");
				int newCol = targetLine.length();
				for (int i = 0; i < targetLine.length(); i++) {
					final double colX = super.getAbsoluteX() + this.marginLeft + this.getTextWidth(targetLine.substring(0, i));
					if (cursorX < colX) {
						newCol = i;
						break;
					}
				}

				final int newCursorPos = this.getTextPosition(newLineIdx, newCol);
				if (Key.LEFT_SHIFT.isDown() || Key.RIGHT_SHIFT.isDown()) {
					if (this.selectionStart == -1) {
						this.selectionStart = this.cursorPos;
					}
				} else {
					this.selectionStart = -1;
				}

				if (newCursorPos < this.cursorPos) {
					this.decreaseCursor(this.cursorPos - newCursorPos);
				} else {
					this.increaseCursor(newCursorPos - this.cursorPos);
				}

				return;
			}

			if (key == Key.LEFT) {
				if (Key.LEFT_SHIFT.isDown() || Key.RIGHT_SHIFT.isDown()) {
					if (this.selectionStart == -1) {
						this.selectionStart = this.cursorPos;
					}
				} else {
					this.selectionStart = -1;
				}

				this.holdInput(key);
				this.decreaseCursor(Key.LEFT_CONTROL.isDown() ? this.cursorPos - this.previousWordIndex() : 1);
				return;
			}

			if (key == Key.RIGHT) {
				if (Key.LEFT_SHIFT.isDown() || Key.RIGHT_SHIFT.isDown()) {
					if (this.selectionStart == -1) {
						this.selectionStart = this.cursorPos;
					}
				} else {
					this.selectionStart = -1;
				}

				this.holdInput(key);
				this.increaseCursor(Key.LEFT_CONTROL.isDown() ? this.nextWordIndex() - this.cursorPos : 1);
				return;
			}

			if (key == Key.ESCAPE) {
				this.focused(false);
				return;
			}

			if (key == Key.BACKSPACE) {
				if (this.deleteSelection(true) || this.cursorPos <= 0) {
					return;
				}

				this.holdInput(key);
				final int backStart = Key.LEFT_CONTROL.isDown() ? this.previousWordIndex() : this.cursorPos - 1;
				this.setText(this.text.substring(0, backStart) + this.text.substring(this.cursorPos));
				this.decreaseCursor(this.cursorPos - backStart);
				return;
			}

			if (key == Key.DELETE) {
				if (this.deleteSelection(true) || this.cursorPos >= this.text.length()) {
					return;
				}

				this.holdInput(key);
				final int deleteEnd = Key.LEFT_CONTROL.isDown() ? this.nextWordIndex() : this.cursorPos + 1;
				this.setText(this.text.substring(0, this.cursorPos) + this.text.substring(deleteEnd));
				return;
			}

			if (key == Key.HOME) {
				this.cursorPos = 0;
				this.decreaseCursor(0);
				return;
			}

			if (key == Key.END) {
				this.cursorPos = this.text.length();
				this.increaseCursor(0);
				return;
			}

			if (key == Key.A && Key.LEFT_CONTROL.isDown()) {
				this.selectionStart = 0;
				this.cursorPos = this.text.length();
				return;
			}

			if (key == Key.C && Key.LEFT_CONTROL.isDown()) {
				if (this.selectionStart == -1) {
					return;
				}

				if (this.selectionStart < this.cursorPos) {
					BridgeHandler.WINDOW.get().setClipboard(this.text.substring(this.selectionStart, this.cursorPos));
				} else {
					BridgeHandler.WINDOW.get().setClipboard(this.text.substring(this.cursorPos, this.selectionStart));
				}

				return;
			}

			if (key == Key.X && Key.LEFT_CONTROL.isDown()) {
				if (this.selectionStart == -1) {
					return;
				}

				if (this.selectionStart < this.cursorPos) {
					BridgeHandler.WINDOW.get().setClipboard(this.text.substring(this.selectionStart, this.cursorPos));
					this.setText(this.text.substring(0, this.selectionStart) + this.text.substring(this.cursorPos));
					this.cursorPos -= this.cursorPos - this.selectionStart;
				} else {
					BridgeHandler.WINDOW.get().setClipboard(this.text.substring(this.cursorPos, this.selectionStart));
					this.setText(this.text.substring(0, this.cursorPos) + this.text.substring(this.selectionStart));
				}

				this.selectionStart = -1;
				return;
			}

			String textToAdd = key == Key.ENTER || key == Key.NUMPAD_ENTER ? "\n" : Character.toString(c);
			if (key == Key.V && Key.LEFT_CONTROL.isDown()) {
				textToAdd = BridgeHandler.WINDOW.get().getClipboard();
			}

			textToAdd = textToAdd.replace("\r", "\n").replace(System.lineSeparator(), "\n");
			textToAdd = textToAdd.replace("\n", "${newline}");

			final char[] achar = textToAdd.toCharArray();
			final int i = achar.length;

			final StringBuilder stringbuilder = new StringBuilder();
			for (int j = 0; j < i; ++j) {
				final char c0 = achar[j];
				if (c0 != 167 && c0 >= 32 && c0 != 127 && c0 <= 563) {
					stringbuilder.append(c0);
				}
			}
			textToAdd = stringbuilder.toString();

			textToAdd = textToAdd.replace("${newline}", "\n");
			if (textToAdd.isEmpty()) {
				return;
			}

			this.deleteSelection(false);
			this.setText(this.text.substring(0, this.cursorPos) + textToAdd + this.text.substring(this.cursorPos));
			this.increaseCursor(textToAdd.length());
		});
	}

	@Override
	public final void mousePressed(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
		if (context.isCancelled() || !this.isHovered(mouseX, mouseY)) {
			this.focused(false);
			this.selectionStart = -1;
			return;
		}

		context.cancel(() -> {
			if (Key.LEFT_SHIFT.isDown() || Key.RIGHT_SHIFT.isDown()) {
				if (this.selectionStart == -1) {
					this.selectionStart = this.cursorPos;
				}
			} else {
				this.selectionStart = -1;
			}

			final List<String> lines = this.getLines();
			final double lineHeight = this.getLineHeight();

			int lineIndex = -1;
			for (int i = 0; i < lines.size(); i++) {
				final double startLineY = super.getAbsoluteY() + this.marginTop - this.yOffset + lineHeight * i;
				final double endLineY = startLineY + lineHeight;
				if (mouseY >= startLineY && mouseY <= endLineY) {
					lineIndex = i;
					break;
				}
			}

			if (lineIndex < 0 && !lines.isEmpty()) {
				final double firstLineY = super.getAbsoluteY() + this.marginTop - this.yOffset;
				lineIndex = mouseY < firstLineY ? 0 : lines.size() - 1;
			}

			if (lineIndex >= 0) {
				final String line = lines.get(lineIndex).replace("\n", "").replace("\r", "");
				int col = line.length();
				for (int i = 0; i < line.length(); i++) {
					final String beforeCursor = line.substring(0, i);
					final String cursorChar = line.substring(i, i + 1);
					final double cursorX = super.getAbsoluteX() + this.marginLeft + this.getTextWidth(beforeCursor) + this.getTextWidth(cursorChar) / 2;
					if (mouseX < cursorX) {
						col = i;
						break;
					}
				}
				this.cursorPos = this.getTextPosition(lineIndex, col);
			}

			this.focused(true);
		});
	}

	@Override
	public void mouseScroll(final double mouseX, final double mouseY, final int value, final @NonNull InternalContext context) {
		if (context.isCancelled() || !this.focused) {
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
			if (this.yOffset > lines.size() * lineHeight - super.getHeight() + this.marginTop + this.marginBottom) {
				this.yOffset = lines.size() * lineHeight - super.getHeight() + this.marginTop + this.marginBottom;
			}
		});
	}

	public final <T extends MultilineTextFieldNode> @NonNull T text(final @NonNull String text) {
		this.setText(text);
		return (T) this;
	}

	public final <T extends MultilineTextFieldNode> @NonNull T placeholder(final @NonNull String placeholder) {
		this.placeholder = placeholder;
		return (T) this;
	}

	public final <T extends MultilineTextFieldNode> @NonNull T info(final @NonNull TextInfo textInfo) {
		this.info = textInfo;
		return (T) this;
	}

	public final <T extends MultilineTextFieldNode> @NonNull T focused(final boolean focused) {
		if (this.focused == focused) {
			return (T) this;
		}

		super.executeCallback(MultilineTextFieldNode.CALLBACK_FOCUS, InternalContext.create(), () -> {
			this.focused = focused;
		});
		return (T) this;
	}

	public final <T extends MultilineTextFieldNode> @NonNull T filter(final @NonNull BiFunction<String, String, String> filter) {
		this.filter = filter;
		return (T) this;
	}

	public final <T extends MultilineTextFieldNode> @NonNull T maxTextLength(final int maxTextLength) {
		this.maxTextLength = maxTextLength;
		return (T) this;
	}

	public final <T extends MultilineTextFieldNode> @NonNull T margin(final double margin) {
		this.marginTop    = margin;
		this.marginBottom = margin;
		this.marginLeft   = margin;
		this.marginRight  = margin;
		return (T) this;
	}

	public final <T extends MultilineTextFieldNode> @NonNull T margin(final double margin, final double cursorMargin) {
		this.marginTop    = margin;
		this.marginBottom = margin;
		this.marginLeft   = margin;
		this.marginRight  = margin;
		this.cursorMargin = cursorMargin;
		return (T) this;
	}

	public final <T extends MultilineTextFieldNode> @NonNull T marginTop(final double marginTop) {
		this.marginTop = marginTop;
		return (T) this;
	}

	public final <T extends MultilineTextFieldNode> @NonNull T marginLeft(final double marginLeft) {
		this.marginLeft = marginLeft;
		return (T) this;
	}

	public final <T extends MultilineTextFieldNode> @NonNull T marginRight(final double marginRight) {
		this.marginRight = marginRight;
		return (T) this;
	}

	public final <T extends MultilineTextFieldNode> @NonNull T marginBottom(final double marginBottom) {
		this.marginBottom = marginBottom;
		return (T) this;
	}

	public final <T extends MultilineTextFieldNode> @NonNull T cursorMargin(final double cursorMargin) {
		this.cursorMargin = cursorMargin;
		return (T) this;
	}

	public final <T extends MultilineTextFieldNode> @NonNull T cursorPosition(final int cursorPos) {
		this.cursorPos = Math.min(Math.max(0, cursorPos), this.text.length());
		return (T) this;
	}

	public final <T extends MultilineTextFieldNode> @NonNull T onChange(final @NonNull NodeTextFieldChangeCallback<T> callback) {
		super.registerCallback(MultilineTextFieldNode.CALLBACK_CHANGE, callback);
		return (T) this;
	}

	public final <T extends MultilineTextFieldNode> @NonNull T onFocus(final @NonNull NodeTextFieldFocusCallback<T> callback) {
		super.registerCallback(MultilineTextFieldNode.CALLBACK_FOCUS, callback);
		return (T) this;
	}

	private final void setText(final String newText) {
		final String oldText = this.text == null ? "" : this.text;
		final String filtered = this.filter.apply(oldText, newText == null ? "" : newText);
		final String accepted = this.maxTextLength >= 0 && filtered.length() > this.maxTextLength ? filtered.substring(0, this.maxTextLength) : filtered;
		if (!accepted.equals(oldText)) {
			this.executeCallback(MultilineTextFieldNode.CALLBACK_CHANGE, InternalContext.create(), () -> {
				this.text = accepted;
			}, oldText, accepted);
		} else {
			this.text = accepted;
		}
	}

	private final void holdInput(final @NonNull Key key) {
		this.firstInput = true;
		this.inputting  = true;
		this.lastInput  = BridgeHandler.CLOCK.get().currentTimeMillis();
		this.inputType  = key;
	}

	private final void decreaseCursor(final int value) {
		this.cursorPos -= value;
		if (this.cursorPos < 0) {
			this.cursorPos = 0;
		}

		if (this.cursorPos == 0) {
			this.yOffset = 0;
			return;
		}

		final double lineHeight = this.getLineHeight();
		final String beforeCursor = this.text.substring(0, this.cursorPos);
		final List<String> beforeCursorLines = this.getLines(beforeCursor);
		final double cursorY = super.getY() + this.marginTop + lineHeight * (beforeCursorLines.size() - 1);
		if (cursorY - this.yOffset < super.getY() + this.marginTop + this.cursorMargin) {
			this.yOffset = cursorY - super.getY() - this.marginTop - this.cursorMargin;
		}
	}

	private final void increaseCursor(final int value) {
		this.cursorPos += value;
		if (this.cursorPos > this.text.length()) {
			this.cursorPos = this.text.length();
		}

		final double lineHeight = this.getLineHeight();
		final String beforeCursor = this.text.substring(0, this.cursorPos);
		final List<String> beforeCursorLines = this.getLines(beforeCursor);
		final double cursorY = super.getY() + this.marginTop + lineHeight * (beforeCursorLines.size() - 1);
		if (cursorY + lineHeight - this.yOffset > super.getY() + super.getHeight() - this.marginBottom) {
			this.yOffset = cursorY + lineHeight - super.getY() - super.getHeight() + this.marginBottom;
		}
	}

	private final double getRawWidth() {
		return super.getWidth() - this.marginLeft - this.marginRight;
	}

	private final double getRawHeight() {
		return super.getHeight() - this.marginTop - this.marginBottom;
	}

	private final double getLineHeight() {
		return this.info.getHeight();
	}

	private final @NonNull List<String> getLines() {
		return DrawUtils.TEXT.getLines(this.getRawWidth(), this.text, this.info);
	}

	private final @NonNull List<String> getLines(final @NonNull String text) {
		return DrawUtils.TEXT.getLines(this.getRawWidth(), text, this.info);
	}

	private final @NonNull List<String> getLines(final int start, final int end) {
		final int from = Math.max(0, start);
		final int to = Math.min(this.text.replace("\n", "").length(), end);
		return DrawUtils.TEXT.getLines(this.getRawWidth(), this.text.substring(from, to), this.info);
	}

	private final int[] getLineAndColumn(final int pos) {
		final List<String> lines = this.getLines();
		if (lines.isEmpty()) {
			return new int[] {0, 0};
		}

		int textIdx = 0;
		for (int lineIdx = 0; lineIdx < lines.size(); lineIdx++) {
			if (lineIdx > 0 && textIdx < this.text.length() && (this.text.charAt(textIdx) == '\n' || this.text.charAt(textIdx) == '\r')) {
				textIdx++;
			}

			final String line = lines.get(lineIdx).replace("\n", "").replace("\r", "");
			final int lineEnd = textIdx + line.length();
			if (pos >= textIdx && pos <= lineEnd) {
				return new int[] {lineIdx, pos - textIdx};
			}

			textIdx = lineEnd;
			if (textIdx < this.text.length() && this.text.charAt(textIdx) == ' ' && lineIdx < lines.size() - 1) {
				textIdx++;
			}
		}

		final String lastLine = lines.get(lines.size() - 1).replace("\n", "").replace("\r", "");
		return new int[] {lines.size() - 1, lastLine.length()};
	}

	private final double getTextWidth(final @NonNull String text) {
		return this.info.getWidth(text.replace("\n", ""));
	}

	private final int getTextPosition(final int lineIdx, final int col) {
		final List<String> lines = this.getLines();
		if (lines.isEmpty()) {
			return 0;
		}

		int textIdx = 0;
		final int clampedLineIdx = Math.min(Math.max(0, lineIdx), lines.size() - 1);
		for (int i = 0; i <= clampedLineIdx; i++) {
			if (i > 0 && textIdx < this.text.length() && (this.text.charAt(textIdx) == '\n' || this.text.charAt(textIdx) == '\r')) {
				textIdx++;
			}

			final String line = lines.get(i).replace("\n", "").replace("\r", "");
			if (i == clampedLineIdx) {
				return textIdx + Math.min(Math.max(0, col), line.length());
			}

			textIdx += line.length();
			if (textIdx < this.text.length() && this.text.charAt(textIdx) == ' ' && i < lines.size() - 1) {
				textIdx++;
			}
		}

		return textIdx;
	}

	private final int nextWordIndex() {
		int index = this.cursorPos;
		while (index < this.text.length() && !this.isSeparator(this.text.charAt(index))) {
			index++;
		}

		while (index < this.text.length() && this.isSeparator(this.text.charAt(index))) {
			index++;
		}

		return index;
	}

	private final int previousWordIndex() {
		int index = this.cursorPos;
		while (index > 0 && this.isSeparator(this.text.charAt(index - 1))) {
			index--;
		}

		while (index > 0 && !this.isSeparator(this.text.charAt(index - 1))) {
			index--;
		}

		return index;
	}

	private final boolean isSeparator(final char c) {
		return c == ' ' || c == '\n' || c == '\r';
	}

	private final boolean deleteSelection(final boolean filter) {
		if (this.selectionStart == -1) {
			return false;
		}

		final String oldText = this.text;
		if (this.selectionStart < this.cursorPos) {
			if (filter) {
				final String newText = this.filter.apply(this.text, this.text.substring(0, this.selectionStart) + this.text.substring(this.cursorPos));
				this.executeCallback(MultilineTextFieldNode.CALLBACK_CHANGE, InternalContext.create(), () -> {
					this.text = newText;
				}, oldText, newText);
			} else {
				final String newText = this.text.substring(0, this.selectionStart) + this.text.substring(this.cursorPos);
				this.executeCallback(MultilineTextFieldNode.CALLBACK_CHANGE, InternalContext.create(), () -> {
					this.text = newText;
				}, oldText, newText);
			}
			this.cursorPos -= oldText.length() - this.text.length();
		} else if (filter) {
			final String newText = this.filter.apply(this.text, this.text.substring(0, this.cursorPos) + this.text.substring(this.selectionStart));
			this.executeCallback(MultilineTextFieldNode.CALLBACK_CHANGE, InternalContext.create(), () -> {
				this.text = newText;
			}, oldText, newText);
		} else {
			final String newText = this.text.substring(0, this.cursorPos) + this.text.substring(this.selectionStart);
			this.executeCallback(MultilineTextFieldNode.CALLBACK_CHANGE, InternalContext.create(), () -> {
				this.text = newText;
			}, oldText, newText);
		}

		this.selectionStart = -1;
		return true;
	}

}