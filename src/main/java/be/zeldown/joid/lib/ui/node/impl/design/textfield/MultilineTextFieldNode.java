package be.zeldown.joid.lib.ui.node.impl.design.textfield;

import java.util.List;
import java.util.function.BiFunction;

import org.lwjgl.input.Keyboard;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.draw.text.builder.Text;
import be.zeldown.joid.lib.draw.text.utils.TextMode;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import be.zeldown.joid.lib.ui.node.impl.design.textfield.callback.NodeTextFieldChangeCallback;
import be.zeldown.joid.lib.ui.node.impl.design.textfield.callback.NodeTextFieldFocusCallback;
import be.zeldown.joid.lib.utils.clipboard.ClipboardUtils;
import be.zeldown.joid.lib.utils.context.InternalContext;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class MultilineTextFieldNode extends Node {

	private static final int CALLBACK_CHANGE = NodeCallbackRegistry.next(NodeTextFieldChangeCallback.class);
	private static final int CALLBACK_FOCUS  = NodeCallbackRegistry.next(NodeTextFieldFocusCallback.class);

	private String   text;
	private String   placeholder;
	private TextInfo info;

	private boolean focused;
	private BiFunction<String, String, String> filter;
	private int maxTextLength;

	private int selectionStart;
	private int cursorPos;

	private double marginTop;
	private double marginBottom;
	private double marginLeft;
	private double marginRight;
	private double cursorMargin;

	private double yOffset;

	private boolean firstInput;
	private boolean inputting;
	private long    lastInput;
	private int     inputType;

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

		super.getUi().stencil(super.getX() + this.marginLeft, super.getY() + this.marginTop, maxWidth, this.getRawHeight(), () -> {
			final boolean isPlaceholder = this.text.isEmpty() && !this.focused;
			DrawUtils.TEXT.drawText(textX, textY, maxWidth, super.getHeight(), Text.create(isPlaceholder ? this.placeholder : this.text, isPlaceholder ? this.info.copy().color(new Color(this.info.getColor().r, this.info.getColor().g, this.info.getColor().b, 0.5F)) : this.info), TextMode.SPLIT);

			if (this.focused) {
				final String beforeCursor = this.text.substring(0, this.cursorPos);
				final List<String> beforeCursorLines = this.getLines(beforeCursor);
				if (beforeCursorLines.isEmpty()) {
					final double cursorX = textX;
					final double cursorY = textY;
					final Color cursorColor = new Color(this.info.getColor());
					final float cursorOpacity = (float) ((Math.sin(2 * Math.PI * (System.currentTimeMillis() % 2000) / 1000) + 1) / 2F);
					cursorColor.a = cursorOpacity;
					DrawUtils.SHAPE.drawRect(cursorX, cursorY, 2, lineHeight, cursorColor);
				} else {
					final String line = beforeCursorLines.get(beforeCursorLines.size() - 1);
					final double cursorX = textX + this.getTextWidth(line);
					final double cursorY = textY + lineHeight * (beforeCursorLines.size() - 1);
					final Color cursorColor = new Color(this.info.getColor());
					final float cursorOpacity = (float) ((Math.sin(2 * Math.PI * (System.currentTimeMillis() % 2000) / 1000) + 1) / 2F);
					cursorColor.a = cursorOpacity;
					DrawUtils.SHAPE.drawRect(cursorX, cursorY, 2, lineHeight, cursorColor);
				}
			}

			if (this.selectionStart != -1) {
				final Color selectionColor = new Color(50, 152, 253, 100);
				final int start = Math.min(this.cursorPos, this.selectionStart);
				final int end = Math.max(this.cursorPos, this.selectionStart);

				int startLine = 0;
				int startChar = 0;
				int endLine = 0;
				int endChar = 0;

				int charIndex = 0;
				for (int i = 0; i < lines.size(); i++) {
					final String line = lines.get(i);
					if (charIndex + line.length() >= start) {
						startLine = i;
						startChar = start - charIndex;
						break;
					}

					charIndex += line.length();
				}

				charIndex = 0;
				for (int i = 0; i < lines.size(); i++) {
					final String line = lines.get(i);
					if (charIndex + line.length() >= end) {
						endLine = i;
						endChar = end - charIndex;
						break;
					}

					charIndex += line.length();
				}

				if (startLine == endLine) {
					final String line = lines.get(startLine);
					final double selectionX = textX + this.getTextWidth(line.substring(0, startChar));
					final double selectionY = textY + lineHeight * startLine;
					final String subLine = line.substring(startChar, endChar);
					final double selectionWidth = "\n".equals(subLine) ? 2 : this.getTextWidth(subLine);
					DrawUtils.SHAPE.drawRect(selectionX, selectionY, selectionWidth, lineHeight, selectionColor);
				} else {
					for (int i = startLine; i <= endLine; i++) {
						final String line = lines.get(i);
						final boolean isStart = i == startLine;
						final boolean isEnd = i == endLine;

						if (isStart) {
							final double selectionX = textX + this.getTextWidth(line.substring(0, startChar));
							final double selectionY = textY + lineHeight * i;
							final String subLine = line.substring(startChar);
							final double selectionWidth = "\n".equals(subLine) ? 2 : this.getTextWidth(subLine);
							DrawUtils.SHAPE.drawRect(selectionX, selectionY, selectionWidth, lineHeight, selectionColor);
							continue;
						}

						if (isEnd) {
							final double selectionX = textX;
							final double selectionY = textY + lineHeight * i;
							final String subLine = line.substring(0, endChar);
							final double selectionWidth = "\n".equals(subLine) ? 2 : this.getTextWidth(subLine);
							DrawUtils.SHAPE.drawRect(selectionX, selectionY, selectionWidth, lineHeight, selectionColor);
							continue;
						}

						final double selectionX = textX;
						final double selectionY = textY + lineHeight * i;
						final double selectionWidth = "\n".equals(line) ? 2 : this.getTextWidth(line);
						DrawUtils.SHAPE.drawRect(selectionX, selectionY, selectionWidth, lineHeight, selectionColor);
					}
				}
			}
		});

		if (this.inputting && System.currentTimeMillis() - this.lastInput >= (this.firstInput ? 500 : 100)) {
			this.firstInput = false;

			if (!Keyboard.isKeyDown(this.inputType)) {
				this.inputting = false;
				return;
			}

			if (this.inputType == Keyboard.KEY_DELETE) {
				if (this.cursorPos >= this.text.length()) {
					this.inputting = false;
					return;
				}

				this.setText(this.text.substring(0, this.cursorPos) + this.text.substring(this.cursorPos + 1));
			} else if (this.inputType == Keyboard.KEY_BACK) {
				if (this.cursorPos <= 0) {
					this.inputting = false;
					return;
				}

				this.setText(this.text.substring(0, this.cursorPos - 1) + this.text.substring(this.cursorPos));
				this.decreaseCursor(1);
			} else if (this.inputType == Keyboard.KEY_LEFT) {
				this.decreaseCursor(1);
			} else if (this.inputType == Keyboard.KEY_RIGHT) {
				this.increaseCursor(1);
			}

			this.lastInput = System.currentTimeMillis();
		}
	}

	@Override
	public final void keyPressed(final char c, final int keyCode, final @NonNull InternalContext context) {
		if (context.isCancelled() || !this.focused) {
			return;
		}

		context.cancel(() -> {
			if (keyCode == Keyboard.KEY_UP) {
				final List<String> lines = this.getLines();
				final String beforeCursor = this.text.substring(0, this.cursorPos);
				final List<String> beforeCursorLines = this.getLines(beforeCursor);
				final int cursorLineIndex = beforeCursorLines.size() - 1;
				if (cursorLineIndex <= 0) {
					return;
				}

				final String cursorLine = beforeCursorLines.get(cursorLineIndex);
				final double cursorX = super.getAbsoluteX() + this.marginLeft + this.getTextWidth(cursorLine);

				final int newCursorLineIndex = cursorLineIndex - 1;
				final String line = beforeCursorLines.get(newCursorLineIndex);
				int lineCharIndex = 0;
				for (int i = 0; i < newCursorLineIndex; i++) {
					lineCharIndex += lines.get(i).length();
				}

				int newCursorPos = lineCharIndex + line.length();
				for (int i = 0; i < line.length(); i++) {
					final String beforeCursorLine = line.substring(0, i);
					final double cursorLineX = super.getAbsoluteX() + this.marginLeft + this.getTextWidth(beforeCursorLine);
					if (cursorX < cursorLineX) {
						newCursorPos = lineCharIndex + i;
						break;
					}
				}

				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
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

			if (keyCode == Keyboard.KEY_DOWN) {
				final List<String> lines = this.getLines();
				final String beforeCursor = this.text.substring(0, this.cursorPos);
				final List<String> beforeCursorLines = this.getLines(beforeCursor);
				final int cursorLineIndex = beforeCursorLines.size() - 1;
				if (cursorLineIndex >= lines.size() - 1) {
					return;
				}

				final String cursorLine = beforeCursorLines.get(cursorLineIndex);
				final double cursorX = super.getAbsoluteX() + this.marginLeft + this.getTextWidth(cursorLine);

				final int newCursorLineIndex = cursorLineIndex + 1;
				final String line = lines.get(newCursorLineIndex);
				int lineCharIndex = 0;
				for (int i = 0; i < newCursorLineIndex; i++) {
					lineCharIndex += lines.get(i).length();
				}

				int newCursorPos = lineCharIndex + line.length();
				for (int i = 0; i < line.length(); i++) {
					final String beforeCursorLine = line.substring(0, i);
					final double cursorLineX = super.getAbsoluteX() + this.marginLeft + this.getTextWidth(beforeCursorLine);
					if (cursorX < cursorLineX) {
						newCursorPos = lineCharIndex + i;
						break;
					}
				}

				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
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

			if (keyCode == Keyboard.KEY_LEFT) {
				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
					if (this.selectionStart == -1) {
						this.selectionStart = this.cursorPos;
					}
				} else {
					this.selectionStart = -1;
				}

				this.holdInput(keyCode);
				this.decreaseCursor(1);
				return;
			}

			if (keyCode == Keyboard.KEY_RIGHT) {
				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
					if (this.selectionStart == -1) {
						this.selectionStart = this.cursorPos;
					}
				} else {
					this.selectionStart = -1;
				}

				this.holdInput(keyCode);
				this.increaseCursor(1);
				return;
			}

			if (keyCode == Keyboard.KEY_ESCAPE) {
				this.focused = false;
				return;
			}

			if (keyCode == Keyboard.KEY_BACK) {
				if (this.deleteSelection(true) || this.cursorPos <= 0) {
					return;
				}

				this.holdInput(keyCode);
				this.setText(this.text.substring(0, this.cursorPos - 1) + this.text.substring(this.cursorPos));
				this.decreaseCursor(1);
				return;
			}

			if (keyCode == Keyboard.KEY_DELETE) {
				if (this.deleteSelection(true) || this.cursorPos >= this.text.length()) {
					return;
				}

				this.holdInput(keyCode);
				this.setText(this.text.substring(0, this.cursorPos) + this.text.substring(this.cursorPos + 1));
				return;
			}

			if (keyCode == Keyboard.KEY_HOME) {
				this.cursorPos = 0;
				this.decreaseCursor(0);
				return;
			}

			if (keyCode == Keyboard.KEY_END) {
				this.cursorPos = this.text.length();
				this.increaseCursor(0);
				return;
			}

			if (keyCode == Keyboard.KEY_A && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
				this.selectionStart = 0;
				this.cursorPos = this.text.length();
				return;
			}

			if (keyCode == Keyboard.KEY_C && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
				if (this.selectionStart == -1) {
					return;
				}

				if (this.selectionStart < this.cursorPos) {
					ClipboardUtils.setClipboard(this.text.substring(this.selectionStart, this.cursorPos));
				} else {
					ClipboardUtils.setClipboard(this.text.substring(this.cursorPos, this.selectionStart));
				}

				return;
			}

			if (keyCode == Keyboard.KEY_X && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
				if (this.selectionStart == -1) {
					return;
				}

				if (this.selectionStart < this.cursorPos) {
					ClipboardUtils.setClipboard(this.text.substring(this.selectionStart, this.cursorPos));
					this.setText(this.text.substring(0, this.selectionStart) + this.text.substring(this.cursorPos));
					this.cursorPos -= this.cursorPos - this.selectionStart;
				} else {
					ClipboardUtils.setClipboard(this.text.substring(this.cursorPos, this.selectionStart));
					this.setText(this.text.substring(0, this.cursorPos) + this.text.substring(this.selectionStart));
				}

				this.selectionStart = -1;
				return;
			}

			String textToAdd = keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER ? "\n" : Character.toString(c);
			if (keyCode == Keyboard.KEY_V && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
				textToAdd = ClipboardUtils.getClipboard();
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

	private final void setText(String newText) {
		final String oldText = this.text;
		newText = this.filter.apply(oldText, newText);
		if (this.maxTextLength >= 0 && newText.length() > this.maxTextLength) {
			newText = newText.substring(0, this.maxTextLength);
		}

		final String finalNewText = newText;
		this.executeCallback(MultilineTextFieldNode.CALLBACK_CHANGE, InternalContext.create(), () -> {
			this.text = finalNewText;
		}, oldText, finalNewText);
	}

	private final void holdInput(final int keyCode) {
		this.firstInput = true;
		this.inputting  = true;
		this.lastInput  = System.currentTimeMillis();
		this.inputType  = keyCode;
	}

	@Override
	public final void mousePressed(final double mouseX, final double mouseY, final int clickType, final @NonNull InternalContext context) {
		if (context.isCancelled() || !this.isHovered(mouseX, mouseY)) {
			this.focused = false;
			this.selectionStart = -1;
			return;
		}

		context.cancel(() -> {
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
				if (this.selectionStart == -1) {
					this.selectionStart = this.cursorPos;
				}
			} else {
				this.selectionStart = -1;
			}

			final List<String> lines = this.getLines();
			final double lineHeight = this.getLineHeight();

			int lineIndex = -1;
			int lineCharIndex = 0;
			for (int i = 0; i < lines.size(); i++) {
				final double startLineY = super.getAbsoluteY() + this.marginTop - this.yOffset + lineHeight * i;
				final double endLineY = startLineY + lineHeight;
				if (mouseY >= startLineY && mouseY <= endLineY) {
					lineIndex = i;
					break;
				}

				lineCharIndex += lines.get(i).length();
			}

			if (lineIndex >= 0) {
				final String line = lines.get(lineIndex);
				this.cursorPos = lineCharIndex + line.length();
				for (int i = 0; i < line.length(); i++) {
					final String beforeCursor = line.substring(0, i);
					final String cursorChar = line.substring(i, i + 1);
					final double cursorX = super.getAbsoluteX() + this.marginLeft + this.getTextWidth(beforeCursor) + this.getTextWidth(cursorChar) / 2;
					if (mouseX < cursorX) {
						this.cursorPos = lineCharIndex + i;
						break;
					}
				}
			}

			this.focused = true;
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

	private final double getRawWidth() {
		return super.getWidth() - this.marginLeft - this.marginRight;
	}

	private final double getRawHeight() {
		return super.getHeight() - this.marginTop - this.marginBottom;
	}

	private final @NonNull List<String> getLines() {
		return DrawUtils.TEXT.getLines(this.getRawWidth(), Text.create(this.text, this.info));
	}

	private final @NonNull List<String> getLines(final @NonNull String text) {
		return DrawUtils.TEXT.getLines(this.getRawWidth(), Text.create(text, this.info));
	}

	private final @NonNull List<String> getLines(int start, int end) {
		start = Math.max(0, start);
		end = Math.min(this.text.replace("\n", "").length(), end);
		return DrawUtils.TEXT.getLines(this.getRawWidth(), Text.create(this.text.substring(start, end), this.info));
	}

	private final double getLineHeight() {
		return this.info.getHeight();
	}

	private final double getTextWidth(final @NonNull String text) {
		return this.info.getWidth(text.replace("\n", ""));
	}

	public final <T extends MultilineTextFieldNode> @NonNull T text(final @NonNull String text) {
		this.text = text;
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

	public final <T extends MultilineTextFieldNode> @NonNull T marginBottom(final double marginBottom) {
		this.marginBottom = marginBottom;
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

	public final <T extends MultilineTextFieldNode> @NonNull T cursorMargin(final double cursorMargin) {
		this.cursorMargin = cursorMargin;
		return (T) this;
	}

	/* [ Callback Section ] */
	public final <T extends MultilineTextFieldNode> @NonNull T onChange(final @NonNull NodeTextFieldChangeCallback<T> callback) {
		super.registerCallback(MultilineTextFieldNode.CALLBACK_CHANGE, callback);
		return (T) this;
	}

	public final <T extends MultilineTextFieldNode> @NonNull T onFocus(final @NonNull NodeTextFieldFocusCallback<T> callback) {
		super.registerCallback(MultilineTextFieldNode.CALLBACK_FOCUS, callback);
		return (T) this;
	}

}