package dev.joid.lib.ui.node.impl.design.textfield;

import java.util.Locale;
import java.util.function.BiFunction;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import dev.joid.lib.ui.node.impl.design.textfield.callback.NodeTextFieldChangeCallback;
import dev.joid.lib.ui.node.impl.design.textfield.callback.NodeTextFieldEnterCallback;
import dev.joid.lib.ui.node.impl.design.textfield.callback.NodeTextFieldFocusCallback;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.key.Key;
import dev.joid.lib.utils.signal.Signal;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class TextFieldNode extends Node {

	private static final int CALLBACK_FOCUS  = NodeCallbackRegistry.next(NodeTextFieldFocusCallback.class);
	private static final int CALLBACK_ENTER  = NodeCallbackRegistry.next(NodeTextFieldEnterCallback.class);
	private static final int CALLBACK_CHANGE = NodeCallbackRegistry.next(NodeTextFieldChangeCallback.class);

	private String   text;
	private TextInfo info;
	private String   placeholder;
	private Align    verticalAlignment;
	private Align    horizontalAlignment;

	private boolean focused;
	private int maxTextLength;
	private BiFunction<String, String, String> filter;

	private boolean        markup;
	private Signal<String> signal;

	private int cursorPos;
	private int selectionStart;

	private double marginTop;
	private double marginLeft;
	private double marginRight;
	private double marginBottom;
	private double cursorMargin;

	private double xOffset;

	private Key     inputType;
	private long    lastInput;
	private boolean inputting;
	private boolean firstInput;

	protected TextFieldNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.text          = "";
		this.placeholder   = "";
		this.focused       = false;
		this.filter        = (oldText, newText) -> newText;
		this.maxTextLength = -1;

		this.horizontalAlignment = Align.START;
		this.verticalAlignment   = Align.CENTER;

		this.selectionStart = -1;

		this.marginHorizontal(2D);
		this.marginVertical(10D);
		this.cursorMargin(15D);
	}

	public static @NonNull TextFieldNode create(final double x, final double y, final double width) {
		return new TextFieldNode(x, y, width, 0D);
	}

	public static @NonNull TextFieldNode create(final double x, final double y, final double width, final double height) {
		return new TextFieldNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (super.getHeight() == 0 && this.info != null) {
			super.height(this.info.ah(this.marginTop + this.marginBottom));
		}

		if (this.cursorPos > this.text.length()) {
			this.cursorPos = this.text.length();
		}

		if (this.selectionStart > this.text.length()) {
			this.selectionStart = this.text.length();
		}

		if (this.xOffset > this.getShownInfo().getWidth(this.text)) {
			this.decreaseCursor(0);
		}

		if (this.xOffset < 0D) {
			this.xOffset = 0D;
		}

		this.xOffset = super.getUi().lerpByFramerate(this.xOffset, this.xOffset, 0.5D, 0.2D, true);

		final boolean isPlaceholder = this.text.isEmpty() && !this.focused;
		double tmpTextY = super.getY() + this.marginTop;

		if (this.verticalAlignment.isCenter()) {
			tmpTextY = super.getY() + (super.getHeight() - this.info.getHeight()) / 2D;
		} else if (this.verticalAlignment.isEnd()) {
			tmpTextY = super.getY() + super.getHeight() - this.info.getHeight() - this.marginBottom;
		}

		final double textX = this.getTextX(super.getX(), isPlaceholder ? this.placeholder : this.text);
		final double textY = tmpTextY;
		super.getUi().mask(super.getX() + this.marginLeft, super.getY(), super.getWidth() - this.marginLeft - this.marginRight, super.getHeight(), () -> {
			DrawUtils.TEXT.drawText(textX, textY, isPlaceholder ? this.placeholder : this.text, isPlaceholder ? this.getShownInfo().copy().color(new Color(this.info.getColor().r, this.info.getColor().g, this.info.getColor().b, 0.5F)) : this.getShownInfo(), Align.START, Align.START);

			if (this.focused) {
				final String beforeCursor = this.text.substring(0, this.cursorPos);
				final double cursorX = textX + this.getShownInfo().getWidth(beforeCursor);
				final Color cursorColor = new Color(this.info.getColor());
				final float cursorOpacity = (float) ((Math.sin(2D * Math.PI * (BridgeHandler.CLOCK.get().currentTimeMillis() % 2000) / 1000) + 1D) / 2F);
				cursorColor.a = cursorOpacity;
				DrawUtils.SHAPE.drawRect(cursorX, textY, 2D, this.info.getHeight(), cursorColor);
			}

			if (this.selectionStart != -1) {
				final String beforeCursor = this.text.substring(0, this.cursorPos);
				final double cursorX = textX + this.getShownInfo().getWidth(beforeCursor);

				final String beforeSelection = this.text.substring(0, this.selectionStart);
				final double selectionX = textX + this.getShownInfo().getWidth(beforeSelection);

				if (selectionX > cursorX) {
					DrawUtils.SHAPE.drawRect(cursorX, textY, selectionX - cursorX, this.info.getHeight(), new Color(50, 152, 253, 100));
				} else {
					DrawUtils.SHAPE.drawRect(selectionX, textY, cursorX - selectionX, this.info.getHeight(), new Color(50, 152, 253, 100));
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

				this.setText(this.text.substring(0, this.cursorPos) + this.text.substring(this.isWordKeyDown() ? this.nextWordIndex() : this.cursorPos + 1));
			} else if (this.inputType == Key.BACKSPACE) {
				if (this.cursorPos <= 0) {
					this.inputting = false;
					return;
				}

				final int backStart = this.isWordKeyDown() ? this.previousWordIndex() : this.cursorPos - 1;
				this.setText(this.text.substring(0, backStart) + this.text.substring(this.cursorPos));
				this.decreaseCursor(this.cursorPos - backStart);
			} else if (this.inputType == Key.LEFT) {
				this.decreaseCursor(this.isWordKeyDown() ? this.cursorPos - this.previousWordIndex() : 1);
			} else if (this.inputType == Key.RIGHT) {
				this.increaseCursor(this.isWordKeyDown() ? this.nextWordIndex() - this.cursorPos : 1);
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
			if (key == Key.LEFT) {
				if (Key.LEFT_SHIFT.isDown() || Key.RIGHT_SHIFT.isDown()) {
					if (this.selectionStart == -1) {
						this.selectionStart = this.cursorPos;
					}
				} else {
					this.selectionStart = -1;
				}

				this.holdInput(key);
				this.decreaseCursor(this.isWordKeyDown() ? this.cursorPos - this.previousWordIndex() : 1);
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
				this.increaseCursor(this.isWordKeyDown() ? this.nextWordIndex() - this.cursorPos : 1);
				return;
			}

			if (key == Key.ENTER || key == Key.NUMPAD_ENTER || key == Key.ESCAPE) {
				this.focused(false);
				this.executeCallback(TextFieldNode.CALLBACK_ENTER, InternalContext.create(), this.text);
				return;
			}

			if (key == Key.BACKSPACE) {
				if (this.deleteSelection() || this.cursorPos <= 0) {
					return;
				}

				this.holdInput(key);
				final int backStart = this.isWordKeyDown() ? this.previousWordIndex() : this.cursorPos - 1;
				this.setText(this.text.substring(0, backStart) + this.text.substring(this.cursorPos));
				this.decreaseCursor(this.cursorPos - backStart);
				return;
			}

			if (key == Key.DELETE) {
				if (this.deleteSelection() || this.cursorPos >= this.text.length()) {
					return;
				}

				this.holdInput(key);
				final int deleteEnd = this.isWordKeyDown() ? this.nextWordIndex() : this.cursorPos + 1;
				this.setText(this.text.substring(0, this.cursorPos) + this.text.substring(deleteEnd));
				return;
			}

			if (key == Key.HOME) {
				if (Key.LEFT_SHIFT.isDown() || Key.RIGHT_SHIFT.isDown()) {
					if (this.selectionStart == -1) {
						this.selectionStart = this.cursorPos;
					}
				} else {
					this.selectionStart = -1;
				}

				this.cursorPos = 0;
				this.decreaseCursor(0);
				return;
			}

			if (key == Key.END) {
				if (Key.LEFT_SHIFT.isDown() || Key.RIGHT_SHIFT.isDown()) {
					if (this.selectionStart == -1) {
						this.selectionStart = this.cursorPos;
					}
				} else {
					this.selectionStart = -1;
				}

				this.cursorPos = this.text.length();
				this.increaseCursor(0);
				return;
			}

			if (key == Key.A && this.isShortcutKeyDown()) {
				this.selectionStart = 0;
				this.cursorPos = this.text.length();
				return;
			}

			if (key == Key.C && this.isShortcutKeyDown()) {
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

			if (key == Key.X && this.isShortcutKeyDown()) {
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

			String textToAdd = Character.toString(c);
			if (key == Key.V && this.isShortcutKeyDown()) {
				textToAdd = BridgeHandler.WINDOW.get().getClipboard();
			}

			textToAdd = this.clean(textToAdd);

			if (textToAdd.isEmpty()) {
				return;
			}

			final int start = this.selectionStart == -1 ? this.cursorPos : Math.min(this.selectionStart, this.cursorPos);
			final int end = this.selectionStart == -1 ? this.cursorPos : Math.max(this.selectionStart, this.cursorPos);
			if (this.maxTextLength >= 0) {
				textToAdd = textToAdd.substring(0, Math.min(textToAdd.length(), Math.max(0, this.maxTextLength - this.text.length() + end - start)));
				if (textToAdd.isEmpty()) {
					return;
				}
			}

			this.selectionStart = -1;
			this.setText(this.text.substring(0, start) + textToAdd + this.text.substring(end));
			this.cursorPos = start;
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

			final double textX = this.getTextX(super.getAbsoluteX(), this.text);
			for (int i = 0; i < this.text.length(); i++) {
				final String beforeCursor = this.text.substring(0, i);
				final String cursorChar = this.text.substring(i, i + 1);
				final double cursorX = textX + this.getShownInfo().getWidth(beforeCursor) + this.getShownInfo().dw(cursorChar, 2);
				if (mouseX < cursorX) {
					this.cursorPos = i;
					break;
				}
				if (i == this.text.length() - 1) {
					this.cursorPos = this.text.length();
				}
			}

			this.focused(true);
		});
	}

	public final <T extends TextFieldNode> @NonNull T text(final @NonNull String text) {
		this.setText(text);
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T placeholder(final @NonNull String placeholder) {
		this.placeholder = placeholder;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T info(final @NonNull TextInfo textInfo) {
		this.info = textInfo;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T align(final @NonNull Align horizontal, final @NonNull Align vertical) {
		this.horizontalAlignment = horizontal;
		this.verticalAlignment   = vertical;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T verticalAlign(final @NonNull Align align) {
		this.verticalAlignment = align;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T horizontalAlign(final @NonNull Align align) {
		this.horizontalAlignment = align;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T focused(final boolean focused) {
		if (this.focused == focused) {
			return (T) this;
		}

		super.executeCallback(TextFieldNode.CALLBACK_FOCUS, InternalContext.create(), () -> {
			this.focused = focused;
			if (!focused) {
				this.selectionStart = -1;
			}
		});
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T filter(final @NonNull BiFunction<String, String, String> filter) {
		this.filter = filter;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T maxTextLength(final int maxTextLength) {
		this.maxTextLength = maxTextLength;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T markup(final boolean markup) {
		this.markup = markup;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T margin(final double margin) {
		this.marginLeft  = margin;
		this.marginRight = margin;
		this.marginTop   = margin;
		this.marginBottom = margin;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T margin(final double margin, final double cursorMargin) {
		this.marginLeft   = margin;
		this.marginRight  = margin;
		this.marginTop    = margin;
		this.marginBottom = margin;
		this.cursorMargin = cursorMargin;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T marginTop(final double marginTop) {
		this.marginTop = marginTop;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T marginLeft(final double marginLeft) {
		this.marginLeft = marginLeft;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T marginVertical(final double margin) {
		this.marginTop = margin;
		this.marginBottom = margin;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T marginHorizontal(final double margin) {
		this.marginLeft = margin;
		this.marginRight = margin;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T marginRight(final double marginRight) {
		this.marginRight = marginRight;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T marginBottom(final double marginBottom) {
		this.marginBottom = marginBottom;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T cursorMargin(final double cursorMargin) {
		this.cursorMargin = cursorMargin;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T cursorPosition(final int cursorPos) {
		this.cursorPos = Math.min(Math.max(0, cursorPos), this.text.length());
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T signal(final @NonNull Signal<String> signal) {
		this.signal = signal;
		super.bind(signal, this::setText);
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T onChange(final @NonNull NodeTextFieldChangeCallback<T> callback) {
		super.registerCallback(TextFieldNode.CALLBACK_CHANGE, callback);
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T onFocus(final @NonNull NodeTextFieldFocusCallback<T> callback) {
		super.registerCallback(TextFieldNode.CALLBACK_FOCUS, callback);
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T onEnter(final @NonNull NodeTextFieldEnterCallback<T> callback) {
		super.registerCallback(TextFieldNode.CALLBACK_ENTER, callback);
		return (T) this;
	}

	private final void setText(final String newText) {
		final String oldText = this.text == null ? "" : this.text;
		final String filtered = this.clean(this.filter.apply(oldText, this.clean(newText == null ? "" : newText)));
		final String accepted = this.maxTextLength >= 0 && filtered.length() > this.maxTextLength ? filtered.substring(0, this.maxTextLength) : filtered;
		if (!accepted.equals(oldText)) {
			this.executeCallback(TextFieldNode.CALLBACK_CHANGE, InternalContext.create(), () -> {
				this.text = accepted;
				super.sync(this.signal, accepted);
			}, oldText, accepted);
		} else {
			this.text = accepted;
		}

		super.sync(this.signal, this.text);
	}

	private final @NonNull String clean(final @NonNull String text) {
		final StringBuilder builder = new StringBuilder();
		for (final char c : text.toCharArray()) {
			if (c != 167 && c >= 32 && c != 127 && c <= 563) {
				builder.append(c);
			}
		}

		return builder.toString();
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
			this.xOffset = 0;
			return;
		}

		if (!this.isScrolling(this.text)) {
			this.xOffset = 0D;
			return;
		}

		final double textX = this.getTextX(super.getX(), this.text);

		final String beforeCursor = this.text.substring(0, this.cursorPos);
		final double cursorX = textX + this.getShownInfo().getWidth(beforeCursor);
		if (cursorX < super.getX() + this.marginLeft + this.cursorMargin) {
			this.xOffset -= super.getX() + this.marginLeft + this.cursorMargin - cursorX;
		}
	}

	private final void increaseCursor(final int value) {
		this.cursorPos += value;
		if (this.cursorPos > this.text.length()) {
			this.cursorPos = this.text.length();
		}

		if (!this.isScrolling(this.text)) {
			this.xOffset = 0D;
			return;
		}

		final double textX = this.getTextX(super.getX(), this.text);

		final String beforeCursor = this.text.substring(0, this.cursorPos);
		final double cursorX = textX + this.getShownInfo().getWidth(beforeCursor);
		if (cursorX > super.getX() + super.getWidth() - this.marginRight - this.cursorMargin) {
			this.xOffset += cursorX - (super.getX() + super.getWidth() - this.marginRight - this.cursorMargin);
		}
	}

	private final int nextWordIndex() {
		int index = this.cursorPos;
		while (index < this.text.length() && this.text.charAt(index) != ' ') {
			index++;
		}

		while (index < this.text.length() && this.text.charAt(index) == ' ') {
			index++;
		}

		return index;
	}

	private final int previousWordIndex() {
		int index = this.cursorPos;
		while (index > 0 && this.text.charAt(index - 1) == ' ') {
			index--;
		}

		while (index > 0 && this.text.charAt(index - 1) != ' ') {
			index--;
		}

		return index;
	}

	private final boolean isMac() {
		return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("mac");
	}

	private final boolean isWordKeyDown() {
		return this.isMac() ? UI.isAltKeyDown() : UI.isCtrlKeyDown();
	}

	private final boolean isShortcutKeyDown() {
		return this.isMac() ? Key.LEFT_SUPER.isDown() || Key.RIGHT_SUPER.isDown() : UI.isCtrlKeyDown();
	}

	private final boolean isScrolling(final @NonNull String shown) {
		return this.horizontalAlignment.isStart() || this.getShownInfo().getWidth(shown) > super.getWidth() - this.marginLeft - this.marginRight - 2D;
	}

	private final double getTextX(final double x, final @NonNull String shown) {
		if (this.isScrolling(shown)) {
			return x + this.marginLeft - this.xOffset;
		}

		if (this.horizontalAlignment.isCenter()) {
			return x + (super.getWidth() - this.getShownInfo().getWidth(shown)) / 2D;
		}

		return x + super.getWidth() - this.getShownInfo().getWidth(shown) - this.marginRight - 2D;
	}

	private final @NonNull TextInfo getShownInfo() {
		return this.markup ? this.info : this.info.copy().markups();
	}

	private final boolean deleteSelection() {
		if (this.selectionStart == -1 || this.selectionStart == this.cursorPos) {
			this.selectionStart = -1;
			return false;
		}

		final int start = Math.min(this.selectionStart, this.cursorPos);
		final int end = Math.max(this.selectionStart, this.cursorPos);
		this.selectionStart = -1;
		this.setText(this.text.substring(0, start) + this.text.substring(end));
		this.cursorPos = start;
		return true;
	}


}