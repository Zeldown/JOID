package dev.joid.lib.ui.node.impl.design.textfield;

import java.util.Locale;
import java.util.function.BiFunction;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import dev.joid.lib.ui.node.impl.design.textfield.callback.NodeTextFieldChangeCallback;
import dev.joid.lib.ui.node.impl.design.textfield.callback.NodeTextFieldFocusCallback;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.key.Key;
import dev.joid.lib.utils.signal.Signal;
import dev.joid.lib.utils.signal.SignalSubscriber;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public abstract class FieldNode<V> extends Node {

	private static final int CALLBACK_FOCUS  = NodeCallbackRegistry.next(NodeTextFieldFocusCallback.class);
	private static final int CALLBACK_CHANGE = NodeCallbackRegistry.next(NodeTextFieldChangeCallback.class);

	private String   text;
	private TextInfo info;
	private String   placeholder;

	private boolean focused;
	private int maxTextLength;
	private BiFunction<String, String, String> filter;

	private boolean             markup;
	private Signal<V>           signal;
	private SignalSubscriber<V> subscription;

	private int cursorPos;
	private int selectionStart;

	private double marginTop;
	private double marginLeft;
	private double marginRight;
	private double marginBottom;
	private double cursorMargin;

	private Key     inputType;
	private long    lastInput;
	private boolean inputting;
	private boolean firstInput;

	protected FieldNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.text          = "";
		this.placeholder   = "";
		this.focused       = false;
		this.filter        = (oldText, newText) -> newText;
		this.maxTextLength = -1;

		this.selectionStart = -1;
	}

	protected abstract void drawField();

	protected abstract boolean isMultiline();

	protected abstract boolean handleKey(final @NonNull Key key);

	protected abstract void placeCursor(final double mouseX, final double mouseY);

	protected abstract void followCursorForward();
	protected abstract void followCursorBackward();

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.cursorPos > this.text.length()) {
			this.cursorPos = this.text.length();
		}

		if (this.selectionStart > this.text.length()) {
			this.selectionStart = this.text.length();
		}

		this.drawField();

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
			if (this.handleKey(key)) {
				return;
			}

			if (key == Key.LEFT) {
				this.updateSelection();
				this.holdInput(key);
				this.decreaseCursor(this.isWordKeyDown() ? this.cursorPos - this.previousWordIndex() : 1);
				return;
			}

			if (key == Key.RIGHT) {
				this.updateSelection();
				this.holdInput(key);
				this.increaseCursor(this.isWordKeyDown() ? this.nextWordIndex() - this.cursorPos : 1);
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
				this.updateSelection();
				this.cursorPos = 0;
				this.decreaseCursor(0);
				return;
			}

			if (key == Key.END) {
				this.updateSelection();
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

			if (key == Key.V && this.isShortcutKeyDown()) {
				this.insert(BridgeHandler.WINDOW.get().getClipboard());
				return;
			}

			this.insert(Character.toString(c));
		});
	}

	@Override
	public final void mousePressed(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
		if (context.isCancelled() || !super.isHovered(mouseX, mouseY)) {
			this.focused(false);
			this.selectionStart = -1;
			return;
		}

		context.cancel(() -> {
			this.updateSelection();
			this.placeCursor(mouseX, mouseY);
			this.focused(true);
		});
	}

	@Override
	public void detach() {
		this.focused(false);
		this.inputting = false;
	}

	public abstract @NonNull V getValue();

	public final <T extends FieldNode<V>> @NonNull T text(final @NonNull String text) {
		this.setText(text);
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T placeholder(final @NonNull String placeholder) {
		this.placeholder = placeholder;
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T info(final @NonNull TextInfo textInfo) {
		this.info = textInfo;
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T focused(final boolean focused) {
		if (this.focused == focused) {
			return (T) this;
		}

		super.executeCallback(FieldNode.CALLBACK_FOCUS, InternalContext.create(), () -> {
			this.focused = focused;
			if (!focused) {
				this.selectionStart = -1;
			}
		});
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T filter(final @NonNull BiFunction<String, String, String> filter) {
		this.filter = filter;
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T maxTextLength(final int maxTextLength) {
		this.maxTextLength = maxTextLength;
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T markup(final boolean markup) {
		this.markup = markup;
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T margin(final double margin) {
		this.marginLeft   = margin;
		this.marginRight  = margin;
		this.marginTop    = margin;
		this.marginBottom = margin;
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T margin(final double margin, final double cursorMargin) {
		this.marginLeft   = margin;
		this.marginRight  = margin;
		this.marginTop    = margin;
		this.marginBottom = margin;
		this.cursorMargin = cursorMargin;
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T marginTop(final double marginTop) {
		this.marginTop = marginTop;
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T marginLeft(final double marginLeft) {
		this.marginLeft = marginLeft;
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T marginVertical(final double margin) {
		this.marginTop = margin;
		this.marginBottom = margin;
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T marginHorizontal(final double margin) {
		this.marginLeft = margin;
		this.marginRight = margin;
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T marginRight(final double marginRight) {
		this.marginRight = marginRight;
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T marginBottom(final double marginBottom) {
		this.marginBottom = marginBottom;
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T cursorMargin(final double cursorMargin) {
		this.cursorMargin = cursorMargin;
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T cursorPosition(final int cursorPos) {
		this.cursorPos = Math.min(Math.max(0, cursorPos), this.text.length());
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T signal(final @NonNull Signal<V> signal) {
		this.signal = signal;
		this.subscription = super.rebind(this.subscription, signal, value -> {
			if (!value.equals(this.getValue())) {
				this.setText(String.valueOf(value));
			}

			super.sync(this.signal, this.getValue());
		});
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T onChange(final @NonNull NodeTextFieldChangeCallback<T> callback) {
		super.registerCallback(FieldNode.CALLBACK_CHANGE, callback);
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T onFocus(final @NonNull NodeTextFieldFocusCallback<T> callback) {
		super.registerCallback(FieldNode.CALLBACK_FOCUS, callback);
		return (T) this;
	}

	protected final void insert(final @NonNull String text) {
		String textToAdd = this.clean(text);
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
	}

	protected final void updateSelection() {
		if (!UI.isShiftKeyDown()) {
			this.selectionStart = -1;
		} else if (this.selectionStart == -1) {
			this.selectionStart = this.cursorPos;
		}
	}

	protected final void decreaseCursor(final int value) {
		this.cursorPos -= value;
		if (this.cursorPos < 0) {
			this.cursorPos = 0;
		}

		this.followCursorBackward();
	}

	protected final void increaseCursor(final int value) {
		this.cursorPos += value;
		if (this.cursorPos > this.text.length()) {
			this.cursorPos = this.text.length();
		}

		this.followCursorForward();
	}

	protected final @NonNull TextInfo getShownInfo() {
		return this.markup ? this.info : this.info.copy().markups();
	}

	private final void setText(final String newText) {
		final String oldText = this.text == null ? "" : this.text;
		final String filtered = this.clean(this.filter.apply(oldText, this.clean(newText == null ? "" : newText)));
		final String accepted = this.maxTextLength >= 0 && filtered.length() > this.maxTextLength ? filtered.substring(0, this.maxTextLength) : filtered;
		if (!accepted.equals(oldText)) {
			super.executeCallback(FieldNode.CALLBACK_CHANGE, InternalContext.create(), () -> {
				this.text = accepted;
				super.sync(this.signal, this.getValue());
			}, oldText, accepted);
		} else {
			this.text = accepted;
		}

		super.sync(this.signal, this.getValue());
	}

	private final @NonNull String clean(final @NonNull String text) {
		final StringBuilder builder = new StringBuilder();
		for (final char c : text.replace("\r\n", "\n").replace("\r", "\n").toCharArray()) {
			if (c == '\n' && this.isMultiline() || c != 167 && c >= 32 && c != 127 && c <= 563) {
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

	private final boolean isMac() {
		return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("mac");
	}

	private final boolean isWordKeyDown() {
		return this.isMac() ? UI.isAltKeyDown() : UI.isCtrlKeyDown();
	}

	private final boolean isShortcutKeyDown() {
		return this.isMac() ? Key.LEFT_SUPER.isDown() || Key.RIGHT_SUPER.isDown() : UI.isCtrlKeyDown();
	}

	private final boolean isSeparator(final char c) {
		return c == ' ' || c == '\n';
	}

}