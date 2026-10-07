package dev.joid.lib.ui.node.impl.design.textfield;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.function.Supplier;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.font.dto.TextStyle;
import dev.joid.lib.font.dto.markup.ITextMarkup;
import dev.joid.lib.font.dto.markup.TextMarkup;
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
	private String  focusedText;
	private int     maxTextLength;

	private Predicate<String> accept;
	private boolean           allowEmpty;
	private V                 fallback;

	private boolean             markup;
	private Signal<V>           signal;
	private SignalSubscriber<V> subscription;

	private int cursorPos;
	private int selectionStart;

	private int    pressCount;
	private long   lastPress;
	private double lastPressX;
	private double lastPressY;

	private int     anchorEnd;
	private int     anchorStart;
	private boolean selecting;

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
		this.accept        = text -> true;
		this.maxTextLength = -1;

		this.selectionStart = -1;
	}

	protected abstract void drawField();

	protected abstract boolean isMultiline();

	protected abstract boolean handleKey(final @NonNull Key key);

	protected abstract int getPositionAt(final double mouseX, final double mouseY);

	protected abstract void followCursorForward();
	protected abstract void followCursorBackward();

	protected abstract V parse(final @NonNull String text);
	protected abstract @NonNull String format(final @NonNull V value);

	protected V correct(final @NonNull V value) {
		return value;
	}

	protected V increment(final @NonNull V value, final int count) {
		return null;
	}

	protected boolean accepts(final @NonNull String text) {
		return true;
	}

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

				this.edit(this.text.substring(0, this.cursorPos) + this.text.substring(this.isWordKeyDown() ? this.nextWordIndex() : this.cursorPos + 1));
			} else if (this.inputType == Key.BACKSPACE) {
				if (this.cursorPos <= 0) {
					this.inputting = false;
					return;
				}

				final int backStart = this.isWordKeyDown() ? this.previousWordIndex() : this.cursorPos - 1;
				if (this.edit(this.text.substring(0, backStart) + this.text.substring(this.cursorPos))) {
					this.decreaseCursor(this.cursorPos - backStart);
				}
			} else if (this.inputType == Key.LEFT) {
				this.decreaseCursor(this.isWordKeyDown() ? this.cursorPos - this.previousWordIndex() : 1);
			} else if (this.inputType == Key.RIGHT) {
				this.increaseCursor(this.isWordKeyDown() ? this.nextWordIndex() - this.cursorPos : 1);
			}

			this.lastInput = BridgeHandler.CLOCK.get().currentTimeMillis();
		}
	}

	@Override
	public void keyPressed(final char c, final @NonNull Key key, final @NonNull InternalContext context) {
		if (context.isCancelled() || !this.focused) {
			return;
		}

		context.cancel(() -> {
			if (this.handleKey(key)) {
				return;
			}

			if (key == Key.UP || key == Key.DOWN) {
				this.stepValue(key == Key.UP ? 1 : -1);
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
				if (this.edit(this.text.substring(0, backStart) + this.text.substring(this.cursorPos))) {
					this.decreaseCursor(this.cursorPos - backStart);
				}
				return;
			}

			if (key == Key.DELETE) {
				if (this.deleteSelection() || this.cursorPos >= this.text.length()) {
					return;
				}

				this.holdInput(key);
				final int deleteEnd = this.isWordKeyDown() ? this.nextWordIndex() : this.cursorPos + 1;
				this.edit(this.text.substring(0, this.cursorPos) + this.text.substring(deleteEnd));
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

				final int start = Math.min(this.selectionStart, this.cursorPos);
				final int end = Math.max(this.selectionStart, this.cursorPos);
				final String cut = this.text.substring(start, end);
				if (this.edit(this.text.substring(0, start) + this.text.substring(end))) {
					BridgeHandler.WINDOW.get().setClipboard(cut);
					this.cursorPos = start;
					this.selectionStart = -1;
				}
				return;
			}

			if (key == Key.V && this.isShortcutKeyDown()) {
				if (this.insert(BridgeHandler.WINDOW.get().getClipboard())) {
					this.commit();
				}
				return;
			}

			this.insert(Character.toString(c));
		});
	}

	@Override
	public void mousePressed(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
		if (context.isCancelled() || !super.isHovered(mouseX, mouseY)) {
			this.focus(false);
			this.selectionStart = -1;
			this.pressCount = 0;
			return;
		}

		context.cancel(() -> {
			this.press(mouseX, mouseY, clickType.isLeft());
			this.focus(true);
		});
	}

	@Override
	public void mouseDragged(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final long deltaTime, final @NonNull InternalContext context) {
		if (this.selecting) {
			this.select(this.getPositionAt(mouseX, mouseY));
		}
	}

	@Override
	public void mouseReleased(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
		this.selecting = false;
	}

	@Override
	public void mouseScroll(final double mouseX, final double mouseY, final int value, final @NonNull InternalContext context) {
		if (context.isCancelled() || value == 0 || !super.isHovered(mouseX, mouseY) || !this.canStep()) {
			return;
		}

		context.cancel(() -> this.stepValue(value > 0 ? 1 : -1));
	}

	@Override
	public void detach() {
		this.focus(false);
		this.inputting = false;
		this.selecting = false;
	}

	public final V getValue() {
		if (this.text.isEmpty() && this.allowEmpty) {
			return null;
		}

		final V parsed = this.parse(this.text);
		return parsed == null ? this.fallback : this.correct(parsed);
	}

	public final boolean isValid() {
		if (this.text.isEmpty() && this.allowEmpty) {
			return true;
		}

		final V parsed = this.isAccepted(this.text) ? this.parse(this.text) : null;
		return parsed != null && parsed.equals(this.correct(parsed));
	}

	public final <T extends FieldNode<V>> @NonNull T text(final @NonNull String text) {
		return this.text(Signal.from(text));
	}

	public final <T extends FieldNode<V>> @NonNull T text(final @NonNull Supplier<@NonNull String> text) {
		return super.follow("text", text, this::replace);
	}

	public final <T extends FieldNode<V>> @NonNull T placeholder(final @NonNull String placeholder) {
		return this.placeholder(Signal.from(placeholder));
	}

	public final <T extends FieldNode<V>> @NonNull T placeholder(final @NonNull Supplier<@NonNull String> placeholder) {
		return super.follow("placeholder", placeholder, value -> this.placeholder = value);
	}

	public final <T extends FieldNode<V>> @NonNull T info(final @NonNull TextInfo info) {
		return this.info(Signal.from(info));
	}

	public final <T extends FieldNode<V>> @NonNull T info(final @NonNull Supplier<@NonNull TextInfo> info) {
		return super.follow("info", info, value -> this.info = value);
	}

	public final <T extends FieldNode<V>> @NonNull T focused(final boolean focused) {
		return this.focused(Signal.from(focused));
	}

	public final <T extends FieldNode<V>> @NonNull T focused(final @NonNull Supplier<Boolean> focused) {
		return super.follow("focused", focused, this::focus);
	}

	public final <T extends FieldNode<V>> @NonNull T accept(final @NonNull Predicate<@NonNull String> accept) {
		this.accept = accept;
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T allowEmpty(final boolean allowEmpty) {
		return this.allowEmpty(Signal.from(allowEmpty));
	}

	public final <T extends FieldNode<V>> @NonNull T allowEmpty(final @NonNull Supplier<Boolean> allowEmpty) {
		return super.follow("allowEmpty", allowEmpty, value -> this.allowEmpty = value);
	}

	public final <T extends FieldNode<V>> @NonNull T maxTextLength(final int maxTextLength) {
		return this.maxTextLength(Signal.from(maxTextLength));
	}

	public final <T extends FieldNode<V>> @NonNull T maxTextLength(final @NonNull Supplier<Integer> maxTextLength) {
		return super.follow("maxTextLength", maxTextLength, value -> this.maxTextLength = value);
	}

	public final <T extends FieldNode<V>> @NonNull T markup(final boolean markup) {
		return this.markup(Signal.from(markup));
	}

	public final <T extends FieldNode<V>> @NonNull T markup(final @NonNull Supplier<Boolean> markup) {
		return super.follow("markup", markup, value -> this.markup = value);
	}

	public final <T extends FieldNode<V>> @NonNull T margin(final double margin) {
		return this.margin(Signal.from(margin));
	}

	public final <T extends FieldNode<V>> @NonNull T margin(final @NonNull Supplier<Double> margin) {
		return super.follow("margin", margin, value -> {
			this.marginLeft   = value;
			this.marginRight  = value;
			this.marginTop    = value;
			this.marginBottom = value;
		});
	}

	public final <T extends FieldNode<V>> @NonNull T marginTop(final double marginTop) {
		return this.marginTop(Signal.from(marginTop));
	}

	public final <T extends FieldNode<V>> @NonNull T marginTop(final @NonNull Supplier<Double> marginTop) {
		return super.follow("marginTop", marginTop, value -> this.marginTop = value);
	}

	public final <T extends FieldNode<V>> @NonNull T marginLeft(final double marginLeft) {
		return this.marginLeft(Signal.from(marginLeft));
	}

	public final <T extends FieldNode<V>> @NonNull T marginLeft(final @NonNull Supplier<Double> marginLeft) {
		return super.follow("marginLeft", marginLeft, value -> this.marginLeft = value);
	}

	public final <T extends FieldNode<V>> @NonNull T marginVertical(final double marginVertical) {
		return this.marginVertical(Signal.from(marginVertical));
	}

	public final <T extends FieldNode<V>> @NonNull T marginVertical(final @NonNull Supplier<Double> marginVertical) {
		return super.follow("marginVertical", marginVertical, value -> {
			this.marginTop    = value;
			this.marginBottom = value;
		});
	}

	public final <T extends FieldNode<V>> @NonNull T marginHorizontal(final double marginHorizontal) {
		return this.marginHorizontal(Signal.from(marginHorizontal));
	}

	public final <T extends FieldNode<V>> @NonNull T marginHorizontal(final @NonNull Supplier<Double> marginHorizontal) {
		return super.follow("marginHorizontal", marginHorizontal, value -> {
			this.marginLeft  = value;
			this.marginRight = value;
		});
	}

	public final <T extends FieldNode<V>> @NonNull T marginRight(final double marginRight) {
		return this.marginRight(Signal.from(marginRight));
	}

	public final <T extends FieldNode<V>> @NonNull T marginRight(final @NonNull Supplier<Double> marginRight) {
		return super.follow("marginRight", marginRight, value -> this.marginRight = value);
	}

	public final <T extends FieldNode<V>> @NonNull T marginBottom(final double marginBottom) {
		return this.marginBottom(Signal.from(marginBottom));
	}

	public final <T extends FieldNode<V>> @NonNull T marginBottom(final @NonNull Supplier<Double> marginBottom) {
		return super.follow("marginBottom", marginBottom, value -> this.marginBottom = value);
	}

	public final <T extends FieldNode<V>> @NonNull T cursorMargin(final double cursorMargin) {
		return this.cursorMargin(Signal.from(cursorMargin));
	}

	public final <T extends FieldNode<V>> @NonNull T cursorMargin(final @NonNull Supplier<Double> cursorMargin) {
		return super.follow("cursorMargin", cursorMargin, value -> this.cursorMargin = value);
	}

	public final <T extends FieldNode<V>> @NonNull T cursorPosition(final int cursorPosition) {
		return this.cursorPosition(Signal.from(cursorPosition));
	}

	public final <T extends FieldNode<V>> @NonNull T cursorPosition(final @NonNull Supplier<Integer> cursorPosition) {
		return super.follow("cursorPosition", cursorPosition, value -> this.cursorPos = Math.min(Math.max(0, value), this.text.length()));
	}

	public final <T extends FieldNode<V>> @NonNull T signal(final @NonNull Signal<V> signal) {
		this.signal = super.writable(signal);
		this.subscription = super.rebind(this.subscription, signal, value -> {
			if (!value.equals(this.getValue())) {
				this.write(value);
			}
		});
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T onChange(final @NonNull NodeTextFieldChangeCallback<T, V> callback) {
		super.registerCallback(FieldNode.CALLBACK_CHANGE, callback);
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T onFocus(final @NonNull NodeTextFieldFocusCallback<T> callback) {
		super.registerCallback(FieldNode.CALLBACK_FOCUS, callback);
		return (T) this;
	}

	protected final void fallback(final V fallback) {
		this.fallback = fallback;
	}

	protected final void write(final @NonNull V value) {
		final String text = this.limit(this.format(this.correct(value)));
		if (!text.equals(this.text)) {
			this.rewrite(text);
		}

		this.commit();
	}

	protected final void revalidate() {
		if (this.fallback != null) {
			this.fallback = this.correct(this.fallback);
		}

		if (!this.focused && !this.text.isEmpty()) {
			this.commit();
		}
	}

	protected final boolean insert(final @NonNull String text) {
		String textToAdd = this.clean(text);
		if (textToAdd.isEmpty()) {
			return false;
		}

		final int start = this.selectionStart == -1 ? this.cursorPos : Math.min(this.selectionStart, this.cursorPos);
		final int end = this.selectionStart == -1 ? this.cursorPos : Math.max(this.selectionStart, this.cursorPos);
		if (this.maxTextLength >= 0) {
			textToAdd = textToAdd.substring(0, Math.min(textToAdd.length(), Math.max(0, this.maxTextLength - this.text.length() + end - start)));
			if (textToAdd.isEmpty()) {
				return false;
			}
		}

		if (!this.edit(this.text.substring(0, start) + textToAdd + this.text.substring(end))) {
			return false;
		}

		this.selectionStart = -1;
		this.cursorPos = start;
		this.increaseCursor(textToAdd.length());
		return true;
	}

	protected final void restore() {
		if (this.focusedText != null && !this.focusedText.equals(this.text)) {
			this.change(this.focusedText);
		}
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

	protected final void focus(final boolean focused) {
		if (this.focused == focused) {
			return;
		}

		super.executeCallback(FieldNode.CALLBACK_FOCUS, InternalContext.create(), () -> {
			if (!focused) {
				this.commit();
			}

			this.focused = focused;
			this.focusedText = focused ? this.text : null;
			if (!focused) {
				this.selectionStart = -1;
			}
		});
	}

	private final void replace(final String newText) {
		final String text = this.limit(newText == null ? "" : newText);
		if (!text.equals(this.text)) {
			this.rewrite(text);
		}

		if (!this.focused) {
			this.commit();
		}
	}

	private final boolean edit(final @NonNull String newText) {
		if (newText.equals(this.text) || !this.isAccepted(newText)) {
			return false;
		}

		return this.change(newText);
	}

	private final boolean change(final @NonNull String newText) {
		final String previous = this.text;
		this.text = newText;
		final V value = this.getValue();
		final boolean valid = this.isValid();
		this.text = previous;

		super.executeCallback(FieldNode.CALLBACK_CHANGE, InternalContext.create(), () -> {
			this.text = newText;
			this.sync();
		}, newText, value, valid);
		return !previous.equals(this.text);
	}

	private final void commit() {
		final V value = this.getValue();
		this.fallback = value;
		final String text = value == null ? "" : this.limit(this.format(value));
		if (!text.equals(this.text)) {
			this.rewrite(text);
		}

		this.sync();
	}

	private final void rewrite(final @NonNull String newText) {
		final int cursorFromEnd = this.text.length() - this.cursorPos;
		final int selectionFromEnd = this.text.length() - this.selectionStart;
		if (!this.change(newText) || !this.focused) {
			return;
		}

		this.cursorPos = Math.max(0, this.text.length() - cursorFromEnd);
		if (this.selectionStart != -1) {
			this.selectionStart = Math.max(0, this.text.length() - selectionFromEnd);
		}
	}

	private final void sync() {
		final V value = this.getValue();
		if (value != null) {
			super.sync(this.signal, value);
		}
	}

	private final boolean canStep() {
		final V value = this.getValue();
		final V from = value != null ? value : this.fallback;
		return from != null && this.increment(from, 0) != null;
	}

	private final void stepValue(final int count) {
		final V value = this.getValue();
		final V from = value != null ? value : this.fallback;
		if (from == null) {
			return;
		}

		final V next = this.increment(from, count);
		if (next != null) {
			this.write(next);
		}
	}

	private final boolean isAccepted(final @NonNull String text) {
		return this.accepts(text) && this.accept.test(text);
	}

	private final @NonNull String limit(final @NonNull String text) {
		final String cleaned = this.clean(text);
		return this.maxTextLength >= 0 && cleaned.length() > this.maxTextLength ? cleaned.substring(0, this.maxTextLength) : cleaned;
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

	private final void press(final double mouseX, final double mouseY, final boolean left) {
		final long now = BridgeHandler.CLOCK.get().currentTimeMillis();
		final boolean repeated = left && this.pressCount > 0 && now - this.lastPress <= 500L && Math.abs(mouseX - this.lastPressX) <= 4D && Math.abs(mouseY - this.lastPressY) <= 4D;
		this.pressCount = repeated ? Math.min(this.pressCount + 1, 3) : 1;
		this.lastPress  = now;
		this.lastPressX = mouseX;
		this.lastPressY = mouseY;
		this.selecting  = left;

		final int position = this.getPositionAt(mouseX, mouseY);
		if (this.pressCount == 1) {
			this.updateSelection();
			this.anchorStart = this.selectionStart == -1 ? position : this.selectionStart;
			this.anchorEnd = this.anchorStart;
			this.cursorPos = position;
			return;
		}

		final int[] range = this.getRange(position);
		this.anchorStart = range[0];
		this.anchorEnd = range[1];
		this.selectionStart = range[0] == range[1] ? -1 : range[0];
		this.cursorPos = range[1];
	}

	private final void select(final int position) {
		final int[] range = this.getRange(position);
		final int previous = this.cursorPos;
		if (range[0] < this.anchorStart) {
			this.selectionStart = this.anchorEnd;
			this.cursorPos = range[0];
		} else {
			this.selectionStart = this.anchorStart;
			this.cursorPos = Math.max(range[1], this.anchorEnd);
		}

		if (this.selectionStart == this.cursorPos) {
			this.selectionStart = -1;
		}

		if (this.cursorPos < previous) {
			this.decreaseCursor(0);
		} else {
			this.increaseCursor(0);
		}
	}

	private final int[] getRange(final int position) {
		if (this.pressCount == 2) {
			return this.getWordRange(position);
		}

		if (this.pressCount == 3) {
			return this.getLineRange(position);
		}

		return new int[] {position, position};
	}

	private final int[] getWordRange(final int position) {
		final int[] tags = FieldNode.tags(this.text, this.getShownInfo());
		final boolean word = position < this.text.length() && !this.isSeparator(tags, position) || position > 0 && !this.isSeparator(tags, position - 1);
		int start = position;
		while (start > 0 && this.isSeparator(tags, start - 1) != word) {
			start--;
		}

		int end = position;
		while (end < this.text.length() && this.isSeparator(tags, end) != word) {
			end++;
		}

		return new int[] {start, end};
	}

	private final int[] getLineRange(final int position) {
		if (!this.isMultiline()) {
			return new int[] {0, this.text.length()};
		}

		final int end = this.text.indexOf('\n', position);
		return new int[] {this.text.lastIndexOf('\n', position - 1) + 1, end < 0 ? this.text.length() : end};
	}

	private final boolean deleteSelection() {
		if (this.selectionStart == -1 || this.selectionStart == this.cursorPos) {
			this.selectionStart = -1;
			return false;
		}

		final int start = Math.min(this.selectionStart, this.cursorPos);
		final int end = Math.max(this.selectionStart, this.cursorPos);
		if (!this.edit(this.text.substring(0, start) + this.text.substring(end))) {
			return true;
		}

		this.selectionStart = -1;
		this.cursorPos = start;
		return true;
	}

	private final int nextWordIndex() {
		final int[] tags = FieldNode.tags(this.text, this.getShownInfo());
		int index = this.cursorPos;
		while (index < this.text.length() && !this.isSeparator(tags, index)) {
			index++;
		}

		while (index < this.text.length() && this.isSeparator(tags, index)) {
			index++;
		}

		return index;
	}

	private final int previousWordIndex() {
		final int[] tags = FieldNode.tags(this.text, this.getShownInfo());
		int index = this.cursorPos;
		while (index > 0 && this.isSeparator(tags, index - 1)) {
			index--;
		}

		while (index > 0 && !this.isSeparator(tags, index - 1)) {
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

	private final boolean isSeparator(final @NonNull int[] tags, final int index) {
		final char c = this.text.charAt(index);
		return tags[index] == -1 && (c == ' ' || c == '\n');
	}

	protected static @NonNull int[] tags(final @NonNull String text, final @NonNull TextInfo info) {
		final int[] tags = new int[text.length()];
		Arrays.fill(tags, -1);

		final List<ITextMarkup> markups = info.getMarkups();
		if (markups.isEmpty()) {
			return tags;
		}

		final TextStyle style = info.getStyle().derive();
		for (int index = 0; index < text.length();) {
			final int consumed = TextMarkup.parse(markups, text, index, style);
			if (consumed > 0) {
				Arrays.fill(tags, index, index + consumed, index);
				index += consumed;
				continue;
			}

			index += Character.charCount(text.codePointAt(index));
		}
		return tags;
	}

	protected static @NonNull String opened(final @NonNull String text, final @NonNull int[] tags, final int end) {
		final StringBuilder opened = new StringBuilder();
		for (int i = 0; i < end; i++) {
			if (tags[i] != -1) {
				opened.append(text.charAt(i));
			}
		}
		return opened.toString();
	}

}