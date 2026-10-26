package dev.joid.lib.ui.node.impl.control.field;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.font.markup.TextMarkup;
import dev.joid.lib.input.cursor.Cursor;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.binding.InteractiveValue;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.impl.node.NodeChangeEvent;
import dev.joid.lib.ui.event.impl.node.NodeCharEvent;
import dev.joid.lib.ui.event.impl.node.NodeFieldChangeEvent;
import dev.joid.lib.ui.event.impl.node.NodeKeyEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseScrollEvent;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.utils.platform.Platform;
import lombok.Getter;
import lombok.NonNull;

public abstract class FieldNode<V> extends Node {

	private static final EventType<NodeFieldChangeEvent.Pre<?, ?>>  BEFORE_CHANGE = EventType.create("beforeChange");
	private static final EventType<NodeFieldChangeEvent.Post<?, ?>> CHANGE        = EventType.create("onChange");

	private static final EventType<NodeChangeEvent.Pre<?, Boolean>>  BEFORE_FOCUS = EventType.create("beforeFocus");
	private static final EventType<NodeChangeEvent.Post<?, Boolean>> FOCUS        = EventType.create("onFocus");
	private static final EventType<NodeChangeEvent.Pre<?, Boolean>>  BEFORE_BLUR  = EventType.create("beforeBlur");
	private static final EventType<NodeChangeEvent.Post<?, Boolean>> BLUR         = EventType.create("onBlur");

	private static final EventType<NodeChangeEvent.Pre<?, Integer>>  BEFORE_CURSOR_MOVE = EventType.create("beforeCursorMove");
	private static final EventType<NodeChangeEvent.Post<?, Integer>> CURSOR_MOVE        = EventType.create("onCursorMove");

	private static final EventType<NodeChangeEvent.Pre<?, FieldSelection>>  BEFORE_SELECT = EventType.create("beforeSelect");
	private static final EventType<NodeChangeEvent.Post<?, FieldSelection>> SELECT        = EventType.create("onSelect");

	private final transient InteractiveValue<String>         textValue;
	private final transient InteractiveValue<Integer>        cursorValue;
	private final transient InteractiveValue<Boolean>        focusedValue;
	private final transient InteractiveValue<FieldSelection> selectionValue;

	@Getter private String   text;
	@Getter private TextInfo info;
	@Getter private String   placeholder;

	@Getter private boolean focused;
	@Getter private int     maxTextLength;

	@Getter private boolean               allowEmpty;
	@Getter private Predicate<String>     accept;
	@Getter private UnaryOperator<String> normalize;

	private V       fallback;
	private V       requested;
	private boolean requesting;
	private String  requestedText;

	@Getter
	private boolean markup;

	@Getter
	private Node background;

	@Getter private Color cursorColor;
	@Getter private Color selectionColor;
	@Getter private Color placeholderColor;

	private int            cursorPosition;
	private FieldSelection selection;

	private int    pressCount;
	private long   lastPress;
	private double lastPressX;
	private double lastPressY;

	private int     anchorStart;
	private int     anchorEnd;
	private boolean selecting;

	@Getter private double marginTop;
	@Getter private double marginBottom;
	@Getter private double marginLeft;
	@Getter private double marginRight;
	@Getter private double cursorMargin;

	private Key     inputType;
	private long    lastInput;
	private boolean inputting;
	private boolean firstInput;

	protected FieldNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.text           = "";
		this.placeholder    = "";
		this.focused        = false;
		this.accept         = text -> true;
		this.normalize      = UnaryOperator.identity();
		this.maxTextLength  = -1;
		this.selection      = FieldSelection.of(0, 0);
		this.selectionColor = new Color(50, 152, 253, 100);

		this.textValue = InteractiveValue.create(super.getBindings(), "text", () -> this.text, (final String text) -> this.text = text)
		.events(FieldNode.BEFORE_CHANGE, (previous, text) -> NodeFieldChangeEvent.Pre.create(this, previous, text, this.valueOf(text), this.isValid(text)), FieldNode.CHANGE, (previous, text) -> NodeFieldChangeEvent.Post.create(this, previous, text, this.valueOf(text), this.isValid(text)));
		this.focusedValue = InteractiveValue.create(super.getBindings(), "focused", () -> this.focused, this::applyFocus)
		.events(true, FieldNode.BEFORE_FOCUS, (previous, focused) -> NodeChangeEvent.Pre.create(this, previous, focused), FieldNode.FOCUS, (previous, focused) -> NodeChangeEvent.Post.create(this, previous, focused))
		.events(false, FieldNode.BEFORE_BLUR, (previous, focused) -> NodeChangeEvent.Pre.create(this, previous, focused), FieldNode.BLUR, (previous, focused) -> NodeChangeEvent.Post.create(this, previous, focused));
		this.cursorValue = InteractiveValue.create(super.getBindings(), "cursorPosition", () -> this.cursorPosition, (final Integer position) -> this.cursorPosition = position)
		.clamp(position -> this.snap(Math.min(Math.max(0, position), this.text.length()), false))
		.events(FieldNode.BEFORE_CURSOR_MOVE, (previous, position) -> NodeChangeEvent.Pre.create(this, previous, position), FieldNode.CURSOR_MOVE, (previous, position) -> NodeChangeEvent.Post.create(this, previous, position));
		this.selectionValue = InteractiveValue.create(super.getBindings(), "selection", () -> this.selection, this::applySelection)
		.clamp(selection -> {
			final int start = this.snap(Math.min(Math.max(0, selection.getStart()), this.text.length()), false);
			final int end = this.snap(Math.min(Math.max(0, selection.getEnd()), this.text.length()), false);
			return FieldSelection.of(start == end ? 0 : start, start == end ? 0 : end);
		})
		.events(FieldNode.BEFORE_SELECT, (previous, selection) -> NodeChangeEvent.Pre.create(this, previous, selection), FieldNode.SELECT, (previous, selection) -> NodeChangeEvent.Post.create(this, previous, selection));

		super.cursor(Cursor.TEXT);
		super.<FieldNode<V>>require("info", "call info(...) before attaching it", node -> node.info != null);
	}

	@Override
	public void layout() {
		if (this.background != null) {
			this.background.layoutPosition(0D, 0D).layoutSize(super.getWidth(), super.getHeight());
		}
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		this.drawField();
		this.repeatInput();
	}

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (event.isConsumed() || !super.isHovered()) {
			this.release();
			return;
		}

		if (!this.focused) {
			for (final UI ui : super.getUi().getBridge().getUiList()) {
				FieldNode.release(ui.getNodeList(), this);
			}
		}

		if (!super.isAttached()) {
			return;
		}

		if (!FieldNode.settle(this.focusedValue, true)) {
			return;
		}

		this.press(event.getMouseX(), event.getMouseY(), event.getButton().isLeft());
		event.consume();
	}

	@Override
	public void mouseReleased(final @NonNull NodeMouseEvent<?> event) {
		this.selecting = false;
	}

	@Override
	public void mouseDragged(final @NonNull NodeMouseEvent<?> event) {
		if (this.selecting) {
			this.drag(this.snap(this.getPositionAt(event.getMouseX(), event.getMouseY()), false));
		}
	}

	@Override
	public void mouseScroll(final @NonNull NodeMouseScrollEvent<?> event) {
		if (event.isConsumed() || event.getNotchesY() == 0D || !this.focused || !super.isHovered() || !this.stepValue(event.getNotchesY() > 0D ? 1 : -1)) {
			return;
		}

		event.consume();
	}

	@Override
	public void charTyped(final @NonNull NodeCharEvent<?> event) {
		if (event.isConsumed() || !this.focused) {
			return;
		}

		if (!Character.isISOControl(event.getCodepoint()) && (!this.isShortcutKeyDown() || UI.isAltKeyDown())) {
			this.insert(new String(Character.toChars(event.getCodepoint())));
		}
		event.consume();
	}

	@Override
	public void keyPressed(final @NonNull NodeKeyEvent<?> event) {
		if (event.isConsumed() || !this.focused) {
			return;
		}

		event.consume();
		if (event.getKey() == Key.ESCAPE) {
			this.focusedValue.set(false);
			return;
		}

		this.pressKey(event.getKey());
	}

	@Override
	public void detach() {
		this.focusedValue.update(false);
	}

	public final V getValue() {
		return this.valueOf(this.text);
	}

	public final boolean isValid() {
		return this.isValid(this.text);
	}

	public final int getCursorPosition() {
		return this.cursorValue.get();
	}

	public final @NonNull FieldSelection getSelection() {
		return this.selectionValue.get();
	}

	public final <T extends FieldNode<V>> @NonNull T background(final @NonNull Node background) {
		this.background = super.part(this.background, background.zindex(-1));
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T text(final @NonNull String text) {
		return super.start("text", text, this::retext);
	}

	public final <T extends FieldNode<V>> @NonNull T text(final @NonNull Signal<String> text) {
		return super.bind("text", text, this::retext);
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

	public final <T extends FieldNode<V>> @NonNull T cursorColor(final @NonNull Color cursorColor) {
		return this.cursorColor(Signal.from(cursorColor));
	}

	public final <T extends FieldNode<V>> @NonNull T cursorColor(final @NonNull Supplier<@NonNull Color> cursorColor) {
		return super.follow("cursorColor", cursorColor, value -> this.cursorColor = value);
	}

	public final <T extends FieldNode<V>> @NonNull T selectionColor(final @NonNull Color selectionColor) {
		return this.selectionColor(Signal.from(selectionColor));
	}

	public final <T extends FieldNode<V>> @NonNull T selectionColor(final @NonNull Supplier<@NonNull Color> selectionColor) {
		return super.follow("selectionColor", selectionColor, value -> this.selectionColor = value);
	}

	public final <T extends FieldNode<V>> @NonNull T placeholderColor(final @NonNull Color placeholderColor) {
		return this.placeholderColor(Signal.from(placeholderColor));
	}

	public final <T extends FieldNode<V>> @NonNull T placeholderColor(final @NonNull Supplier<@NonNull Color> placeholderColor) {
		return super.follow("placeholderColor", placeholderColor, value -> this.placeholderColor = value);
	}

	public final <T extends FieldNode<V>> @NonNull T focused(final boolean focused) {
		return this.focusedValue.start(focused);
	}

	public final <T extends FieldNode<V>> @NonNull T focused(final @NonNull Signal<Boolean> focused) {
		return this.focusedValue.bind(focused);
	}

	public final <T extends FieldNode<V>> @NonNull T accept(final @NonNull Predicate<@NonNull String> accept) {
		this.accept = accept;
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T normalize(final @NonNull UnaryOperator<@NonNull String> normalize) {
		this.normalize = normalize;
		if (this.requestedText != null) {
			this.retext(this.requestedText);
		} else if (!this.focused && !this.text.isEmpty()) {
			this.commit();
		}
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T allowEmpty(final boolean allowEmpty) {
		return this.allowEmpty(Signal.from(allowEmpty));
	}

	public final <T extends FieldNode<V>> @NonNull T allowEmpty(final @NonNull Supplier<Boolean> allowEmpty) {
		return super.follow("allowEmpty", allowEmpty, value -> {
			this.allowEmpty = value;
			this.revalidate();
		});
	}

	public final <T extends FieldNode<V>> @NonNull T maxTextLength(final int maxTextLength) {
		return this.maxTextLength(Signal.from(maxTextLength));
	}

	public final <T extends FieldNode<V>> @NonNull T maxTextLength(final @NonNull Supplier<Integer> maxTextLength) {
		return super.follow("maxTextLength", maxTextLength, value -> {
			this.maxTextLength = value;
			this.revalidate();
		});
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
		this.marginTop(margin);
		this.marginLeft(margin);
		this.marginRight(margin);
		return this.marginBottom(margin);
	}

	public final <T extends FieldNode<V>> @NonNull T marginTop(final double marginTop) {
		return this.marginTop(Signal.from(marginTop));
	}

	public final <T extends FieldNode<V>> @NonNull T marginTop(final @NonNull Supplier<Double> marginTop) {
		return super.follow("marginTop", marginTop, value -> this.marginTop = value);
	}

	public final <T extends FieldNode<V>> @NonNull T marginBottom(final double marginBottom) {
		return this.marginBottom(Signal.from(marginBottom));
	}

	public final <T extends FieldNode<V>> @NonNull T marginBottom(final @NonNull Supplier<Double> marginBottom) {
		return super.follow("marginBottom", marginBottom, value -> this.marginBottom = value);
	}

	public final <T extends FieldNode<V>> @NonNull T marginLeft(final double marginLeft) {
		return this.marginLeft(Signal.from(marginLeft));
	}

	public final <T extends FieldNode<V>> @NonNull T marginLeft(final @NonNull Supplier<Double> marginLeft) {
		return super.follow("marginLeft", marginLeft, value -> this.marginLeft = value);
	}

	public final <T extends FieldNode<V>> @NonNull T marginRight(final double marginRight) {
		return this.marginRight(Signal.from(marginRight));
	}

	public final <T extends FieldNode<V>> @NonNull T marginRight(final @NonNull Supplier<Double> marginRight) {
		return super.follow("marginRight", marginRight, value -> this.marginRight = value);
	}

	public final <T extends FieldNode<V>> @NonNull T marginHorizontal(final double marginHorizontal) {
		return this.marginHorizontal(Signal.from(marginHorizontal));
	}

	public final <T extends FieldNode<V>> @NonNull T marginHorizontal(final @NonNull Supplier<Double> marginHorizontal) {
		this.marginLeft(marginHorizontal);
		return this.marginRight(marginHorizontal);
	}

	public final <T extends FieldNode<V>> @NonNull T marginVertical(final double marginVertical) {
		return this.marginVertical(Signal.from(marginVertical));
	}

	public final <T extends FieldNode<V>> @NonNull T marginVertical(final @NonNull Supplier<Double> marginVertical) {
		this.marginTop(marginVertical);
		return this.marginBottom(marginVertical);
	}

	public final <T extends FieldNode<V>> @NonNull T cursorMargin(final double cursorMargin) {
		return this.cursorMargin(Signal.from(cursorMargin));
	}

	public final <T extends FieldNode<V>> @NonNull T cursorMargin(final @NonNull Supplier<Double> cursorMargin) {
		return super.follow("cursorMargin", cursorMargin, value -> this.cursorMargin = value);
	}

	public final <T extends FieldNode<V>> @NonNull T cursorPosition(final int cursorPosition) {
		return super.start("cursorPosition", cursorPosition, (final Integer position) -> {
			this.cursorValue.update(position);
			this.alignCursor();
		});
	}

	public final <T extends FieldNode<V>> @NonNull T cursorPosition(final @NonNull Signal<Integer> cursorPosition) {
		return this.cursorValue.bind(cursorPosition);
	}

	public final <T extends FieldNode<V>> @NonNull T selection(final int start, final int end) {
		return this.selectionValue.start(FieldSelection.of(start, end));
	}

	public final <T extends FieldNode<V>> @NonNull T selection(final @NonNull Signal<FieldSelection> selection) {
		return this.selectionValue.bind(selection);
	}

	public final <T extends FieldNode<V>> @NonNull T change(final @NonNull String text) {
		super.getBindings().batch(() -> this.replace(text, true));
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T beforeChange(final @NonNull EventListener<NodeFieldChangeEvent.Pre<T, V>> listener) {
		return this.textValue.before(listener);
	}

	public final <T extends FieldNode<V>> @NonNull T onChange(final @NonNull EventListener<NodeFieldChangeEvent.Post<T, V>> listener) {
		return this.textValue.on(listener);
	}

	public final <T extends FieldNode<V>> @NonNull T focus() {
		this.focusedValue.change(true);
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T blur() {
		this.focusedValue.change(false);
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T beforeFocus(final @NonNull EventListener<NodeChangeEvent.Pre<T, Boolean>> listener) {
		return this.focusedValue.before(true, listener);
	}

	public final <T extends FieldNode<V>> @NonNull T onFocus(final @NonNull EventListener<NodeChangeEvent.Post<T, Boolean>> listener) {
		return this.focusedValue.on(true, listener);
	}

	public final <T extends FieldNode<V>> @NonNull T beforeBlur(final @NonNull EventListener<NodeChangeEvent.Pre<T, Boolean>> listener) {
		return this.focusedValue.before(false, listener);
	}

	public final <T extends FieldNode<V>> @NonNull T onBlur(final @NonNull EventListener<NodeChangeEvent.Post<T, Boolean>> listener) {
		return this.focusedValue.on(false, listener);
	}

	public final <T extends FieldNode<V>> @NonNull T moveCursor(final int position) {
		this.cursorValue.change(position);
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T beforeCursorMove(final @NonNull EventListener<NodeChangeEvent.Pre<T, Integer>> listener) {
		return this.cursorValue.before(listener);
	}

	public final <T extends FieldNode<V>> @NonNull T onCursorMove(final @NonNull EventListener<NodeChangeEvent.Post<T, Integer>> listener) {
		return this.cursorValue.on(listener);
	}

	public final <T extends FieldNode<V>> @NonNull T select(final int start, final int end) {
		this.selectionValue.change(FieldSelection.of(start, end));
		return (T) this;
	}

	public final <T extends FieldNode<V>> @NonNull T beforeSelect(final @NonNull EventListener<NodeChangeEvent.Pre<T, FieldSelection>> listener) {
		return this.selectionValue.before(listener);
	}

	public final <T extends FieldNode<V>> @NonNull T onSelect(final @NonNull EventListener<NodeChangeEvent.Post<T, FieldSelection>> listener) {
		return this.selectionValue.on(listener);
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

	protected boolean accepts(final @NonNull String text) {
		return true;
	}

	protected V increment(final @NonNull V value, final int count) {
		return null;
	}

	protected final void fallback(final V fallback) {
		this.fallback = fallback;
	}

	protected final <T extends FieldNode<V>> @NonNull T startValue(final V value) {
		return super.start("text", value, current -> this.write(current, false));
	}

	protected final <T extends FieldNode<V>> @NonNull T bindValue(final @NonNull Signal<V> value) {
		return super.bindNullable("text", value, current -> this.write(current, false), (final String text) -> this.getValue());
	}

	protected final void write(final V value, final boolean refusable) {
		super.getBindings().batch(() -> {
			final boolean shown = Objects.equals(value, this.getValue());
			if (shown && refusable) {
				super.getBindings().see("text");
			}

			if (shown || value == null && !this.allowEmpty || this.replace(value == null ? "" : this.format(this.correct(value)), refusable)) {
				this.request(value, true);
			}

			this.commit();
		});
	}

	protected final void revalidate() {
		if (this.fallback != null) {
			this.fallback = this.correct(this.fallback);
		}

		if (this.requesting) {
			this.write(this.requested, false);
		} else if (!this.focused && !this.text.isEmpty()) {
			this.commit();
		}
	}

	protected final void blurNow() {
		FieldNode.settle(this.focusedValue, false);
	}

	protected final boolean insert(final @NonNull String text) {
		String textToAdd = this.clean(text);
		if (textToAdd.isEmpty()) {
			return false;
		}

		final FieldSelection selection = this.getSelection();
		final int start = selection.isEmpty() ? this.getCursorPosition() : selection.getStart();
		final int end = selection.isEmpty() ? this.getCursorPosition() : selection.getEnd();
		if (this.maxTextLength >= 0) {
			textToAdd = FieldNode.cut(textToAdd, Math.max(0, this.maxTextLength - this.text.codePointCount(0, this.text.length()) + this.text.codePointCount(start, end)));
			if (textToAdd.isEmpty()) {
				return false;
			}
		}

		final int added = textToAdd.length();
		return this.edit(this.text.substring(0, start) + textToAdd + this.text.substring(end), () -> {
			this.selectionValue.set(FieldSelection.of(0, 0));
			this.cursorValue.set(start);
			this.increaseCursor(added);
		});
	}

	protected final void placeCursor(final int position, final boolean forward) {
		final int anchor = this.getAnchor();
		final int cursor = this.snap(position, forward);
		this.cursorValue.set(cursor);
		this.selectionValue.set(UI.isShiftKeyDown() ? FieldSelection.of(anchor, cursor) : FieldSelection.of(0, 0));
		if (forward) {
			this.followCursorForward();
		} else {
			this.followCursorBackward();
		}
	}

	protected final void increaseCursor(final int value) {
		this.cursorValue.set(this.snap(this.getCursorPosition() + value, true));
		this.followCursorForward();
	}

	protected final void decreaseCursor(final int value) {
		this.cursorValue.set(this.snap(this.getCursorPosition() - value, false));
		this.followCursorBackward();
	}

	protected final @NonNull TextInfo getShownInfo() {
		return this.markup ? this.info : this.info.copy().markups();
	}

	protected final @NonNull TextInfo getPlaceholderInfo() {
		if (this.placeholderColor != null) {
			return this.getShownInfo().withColor(this.placeholderColor);
		}

		final Color color = this.info.getColor();
		return this.getShownInfo().withColor(color.withAlpha(color.getAlphaF() * 0.5F));
	}

	protected final @NonNull Color getShownCursorColor() {
		final Color color = this.cursorColor != null ? this.cursorColor : this.info.getColor();
		return color.withAlpha(color.getAlphaF() * (float) ((Math.sin(2D * Math.PI * (BridgeHandler.CLOCK.get().currentTimeMillis() % 2000) / 1000) + 1D) / 2F));
	}

	protected final double getLineX(final @NonNull String text, final @NonNull int[] tags, final int start, final int end, final @NonNull String prefix, final int position) {
		final int clamped = Math.max(start, Math.min(position, end));
		final int shown = clamped < text.length() && tags[clamped] != -1 ? tags[clamped] : clamped;
		return this.getShownInfo().getWidth(prefix + text.substring(start, Math.max(start, shown)));
	}

	protected final int getLinePosition(final @NonNull String text, final @NonNull int[] tags, final int start, final int end, final @NonNull String prefix, final double x) {
		int previous = start;
		for (int i = start; i < end; i++) {
			if (tags[i] != -1) {
				continue;
			}

			final double charWidth = this.getShownInfo().getWidth(TextMarkup.opened(text, tags, i) + text.charAt(i));
			if (x < this.getLineX(text, tags, start, end, prefix, i) + charWidth / 2D) {
				return previous;
			}

			previous = i + 1;
		}

		return previous;
	}

	private void applyFocus(final boolean focused) {
		if (!focused) {
			if (this.focused && !this.requesting && this.requestedText == null) {
				final V typed = this.parse(this.normalize.apply(this.text));
				this.request(typed, typed != null || this.text.isEmpty());
			}

			this.commit();
			this.selectionValue.update(FieldSelection.of(0, 0));
			this.inputting = false;
			this.selecting = false;
		}

		this.focused = focused;
	}

	private void applySelection(final FieldSelection selection) {
		this.selection = selection;
		this.alignCursor();
	}

	private void alignCursor() {
		if (!this.selection.isEmpty() && this.cursorPosition != this.selection.getStart() && this.cursorPosition != this.selection.getEnd()) {
			this.cursorValue.update(this.selection.getEnd());
		}
	}

	private void repeatInput() {
		if (!this.inputting || BridgeHandler.CLOCK.get().currentTimeMillis() - this.lastInput < (this.firstInput ? 500 : 100)) {
			return;
		}

		this.firstInput = false;
		if (!super.isEnabled() || !this.inputType.isDown() || (!this.handleKey(this.inputType) && !this.repeatKey(this.inputType))) {
			this.inputting = false;
			return;
		}

		this.lastInput = BridgeHandler.CLOCK.get().currentTimeMillis();
	}

	private boolean repeatKey(final Key key) {
		final int cursor = this.getCursorPosition();
		if (key == Key.LEFT) {
			this.placeCursor(this.isWordKeyDown() ? this.previousWordIndex() : cursor - 1, false);
			return true;
		}

		if (key == Key.RIGHT) {
			this.placeCursor(this.isWordKeyDown() ? this.nextWordIndex() : cursor + 1, true);
			return true;
		}

		if (key == Key.BACKSPACE) {
			if (cursor <= 0) {
				return false;
			}

			final int start = this.isWordKeyDown() ? this.previousWordIndex() : this.previousIndex(cursor);
			this.edit(this.text.substring(0, start) + this.text.substring(cursor), () -> this.decreaseCursor(cursor - start));
			return true;
		}

		if (key == Key.DELETE) {
			if (cursor >= this.text.length()) {
				return false;
			}

			this.edit(this.text.substring(0, cursor) + this.text.substring(this.isWordKeyDown() ? this.nextWordIndex() : this.nextIndex(cursor)), () -> {});
			return true;
		}

		return false;
	}

	private void pressKey(final Key key) {
		if (this.handleKey(key)) {
			if (this.focused) {
				this.holdInput(key);
			}
			return;
		}

		if (key == Key.UP || key == Key.DOWN) {
			this.stepValue(key == Key.UP ? 1 : -1);
			return;
		}

		if (key == Key.LEFT || key == Key.RIGHT) {
			this.holdInput(key);
			this.repeatKey(key);
			return;
		}

		if (key == Key.BACKSPACE || key == Key.DELETE) {
			if (!this.deleteSelection() && this.repeatKey(key)) {
				this.holdInput(key);
			}
			return;
		}

		if (key == Key.HOME) {
			this.placeCursor(0, false);
			return;
		}

		if (key == Key.END) {
			this.placeCursor(this.text.length(), true);
			return;
		}

		final FieldSelection selection = this.getSelection();
		if (key == Key.A && this.isShortcutKeyDown()) {
			this.cursorValue.set(this.text.length());
			this.selectionValue.set(FieldSelection.of(0, this.text.length()));
			return;
		}

		if (key == Key.C && this.isShortcutKeyDown()) {
			if (!selection.isEmpty()) {
				BridgeHandler.WINDOW.get().setClipboard(this.text.substring(selection.getStart(), selection.getEnd()));
			}
			return;
		}

		if (key == Key.X && this.isShortcutKeyDown()) {
			if (selection.isEmpty()) {
				return;
			}

			final String cut = this.text.substring(selection.getStart(), selection.getEnd());
			this.edit(this.text.substring(0, selection.getStart()) + this.text.substring(selection.getEnd()), () -> {
				BridgeHandler.WINDOW.get().setClipboard(cut);
				this.cursorValue.set(selection.getStart());
				this.selectionValue.set(FieldSelection.of(0, 0));
			});
			return;
		}

		if (key == Key.V && this.isShortcutKeyDown()) {
			this.insert(BridgeHandler.WINDOW.get().getClipboard());
		}
	}

	private boolean replace(final String newText, final boolean refusable) {
		final String text = this.limit(newText);
		final boolean shown = text.equals(this.text);
		if (shown && refusable) {
			super.getBindings().see("text");
			super.sync("text", this.text);
		}

		final boolean accepted = shown || this.rewrite(text, refusable);
		if (accepted) {
			final V parsed = this.parse(this.normalize.apply(newText));
			this.request(parsed, parsed != null || newText.isEmpty());
			this.requestedText = parsed != null || newText.isEmpty() ? null : newText;
		}

		if (!this.focused) {
			this.commit();
		}

		return accepted;
	}

	private boolean edit(final String newText, final Runnable place) {
		if (newText.equals(this.text) || !this.accepts(newText) || !this.accept.test(newText)) {
			return false;
		}

		super.getBindings().open();
		try {
			if (!this.changeText(newText, true)) {
				return false;
			}

			this.request(null, false);
			place.run();
			this.cursorValue.commit();
			this.selectionValue.commit();
			return true;
		} finally {
			super.getBindings().close();
		}
	}

	private void request(final V value, final boolean requesting) {
		this.requested = value;
		this.requesting = requesting;
		this.requestedText = null;
	}

	private void retext(final String text) {
		super.getBindings().batch(() -> this.replace(text, false));
	}

	private boolean changeText(final String text, final boolean refusable) {
		if (!refusable) {
			this.textValue.update(text);
			return true;
		}

		return FieldNode.settle(this.textValue, text);
	}

	private void commit() {
		super.getBindings().batch(() -> {
			final V value = this.getValue();
			if (value != null) {
				this.fallback = value;
			}

			final String text = value == null ? "" : this.limit(this.format(value));
			if (!text.equals(this.text)) {
				this.rewrite(text, false);
			}

			super.sync("text", this.text);
		});
	}

	private boolean rewrite(final String newText, final boolean refusable) {
		final int length = this.text.length();
		final int cursor = this.getCursorPosition();
		final FieldSelection selection = this.getSelection();
		if (!this.changeText(newText, refusable)) {
			return false;
		}

		if (!this.focused || !super.isAttached()) {
			this.cursorValue.reapply();
			this.selectionValue.reapply();
			return true;
		}

		final int shift = this.text.length() - length;
		this.cursorValue.update(cursor + shift);
		if (!selection.isEmpty()) {
			this.selectionValue.update(FieldSelection.of(selection.getStart() + shift, selection.getEnd() + shift));
		}

		return true;
	}

	private V valueOf(final String text) {
		if (text.isEmpty() && this.allowEmpty) {
			return null;
		}

		final V parsed = this.parse(this.normalize.apply(text));
		return parsed == null ? this.fallback : this.correct(parsed);
	}

	private boolean isValid(final String text) {
		if (text.isEmpty() && this.allowEmpty) {
			return true;
		}

		final V parsed = this.accepts(text) && this.accept.test(text) ? this.parse(this.normalize.apply(text)) : null;
		return parsed != null && parsed.equals(this.correct(parsed));
	}

	private boolean stepValue(final int count) {
		final V value = this.getValue();
		final V from = value != null ? value : this.fallback;
		if (from == null) {
			return false;
		}

		final V next = this.increment(from, count);
		if (next != null) {
			this.write(next, true);
		}
		return next != null;
	}

	private @NonNull String limit(final String text) {
		final String cleaned = this.clean(text);
		return this.maxTextLength >= 0 ? FieldNode.cut(cleaned, this.maxTextLength) : cleaned;
	}

	private @NonNull String clean(final String text) {
		final StringBuilder builder = new StringBuilder();
		text.replace("\r\n", "\n").replace("\r", "\n").codePoints().forEach(codepoint -> {
			if (codepoint == '\n' ? this.isMultiline() : !Character.isISOControl(codepoint)) {
				builder.appendCodePoint(codepoint);
			}
		});
		return builder.toString();
	}

	private void holdInput(final Key key) {
		this.firstInput = true;
		this.inputting  = true;
		this.lastInput  = BridgeHandler.CLOCK.get().currentTimeMillis();
		this.inputType  = key;
	}

	private void press(final double mouseX, final double mouseY, final boolean left) {
		final long now = BridgeHandler.CLOCK.get().currentTimeMillis();
		final boolean repeated = left && this.pressCount > 0 && now - this.lastPress <= 500L && Math.abs(mouseX - this.lastPressX) <= 4D && Math.abs(mouseY - this.lastPressY) <= 4D;
		this.pressCount = repeated ? Math.min(this.pressCount + 1, 3) : 1;
		this.lastPress  = now;
		this.lastPressX = mouseX;
		this.lastPressY = mouseY;
		this.selecting  = left;

		final int position = this.snap(this.getPositionAt(mouseX, mouseY), false);
		final int[] range = this.pressCount == 1 ? new int[] {UI.isShiftKeyDown() ? this.getAnchor() : position, position} : this.getRange(position);
		this.anchorStart = range[0];
		this.anchorEnd = this.pressCount == 1 ? range[0] : range[1];
		this.cursorValue.set(range[1]);
		this.selectionValue.set(FieldSelection.of(range[0], range[1]));
	}

	private void release() {
		if (FieldNode.settle(this.focusedValue, false)) {
			this.selectionValue.set(FieldSelection.of(0, 0));
			this.pressCount = 0;
		}
	}

	private void drag(final int position) {
		final int[] range = this.getRange(position);
		final int previous = this.getCursorPosition();
		final int anchor = range[0] < this.anchorStart ? this.anchorEnd : this.anchorStart;
		final int cursor = range[0] < this.anchorStart ? range[0] : Math.max(range[1], this.anchorEnd);
		this.cursorValue.set(cursor);
		this.selectionValue.set(FieldSelection.of(anchor, cursor));
		if (cursor < previous) {
			this.followCursorBackward();
		} else {
			this.followCursorForward();
		}
	}

	private int[] getRange(final int position) {
		if (this.pressCount == 2) {
			return this.getWordRange(position);
		}

		if (this.pressCount == 3) {
			return this.getLineRange(position);
		}

		return new int[] {position, position};
	}

	private int[] getWordRange(final int position) {
		final int[] tags = TextMarkup.tags(this.text, this.getShownInfo());
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

	private int[] getLineRange(final int position) {
		if (!this.isMultiline()) {
			return new int[] {0, this.text.length()};
		}

		final int end = this.text.indexOf('\n', position);
		return new int[] {this.text.lastIndexOf('\n', position - 1) + 1, end < 0 ? this.text.length() : end};
	}

	private boolean deleteSelection() {
		final FieldSelection selection = this.getSelection();
		if (selection.isEmpty()) {
			return false;
		}

		this.edit(this.text.substring(0, selection.getStart()) + this.text.substring(selection.getEnd()), () -> {
			this.selectionValue.set(FieldSelection.of(0, 0));
			this.cursorValue.set(selection.getStart());
		});
		return true;
	}

	private int getAnchor() {
		final FieldSelection selection = this.getSelection();
		final int cursor = this.getCursorPosition();
		if (selection.isEmpty()) {
			return cursor;
		}

		return cursor == selection.getStart() ? selection.getEnd() : selection.getStart();
	}

	private int snap(final int index, final boolean forward) {
		if (index <= 0 || index >= this.text.length() || !Character.isHighSurrogate(this.text.charAt(index - 1)) || !Character.isLowSurrogate(this.text.charAt(index))) {
			return index;
		}

		return forward ? index + 1 : index - 1;
	}

	private int nextIndex(final int index) {
		return Character.offsetByCodePoints(this.text, index, 1);
	}

	private int previousIndex(final int index) {
		return Character.offsetByCodePoints(this.text, index, -1);
	}

	private int nextWordIndex() {
		final int[] tags = TextMarkup.tags(this.text, this.getShownInfo());
		int index = this.getCursorPosition();
		while (index < this.text.length() && !this.isSeparator(tags, index)) {
			index++;
		}

		while (index < this.text.length() && this.isSeparator(tags, index)) {
			index++;
		}

		return index;
	}

	private int previousWordIndex() {
		final int[] tags = TextMarkup.tags(this.text, this.getShownInfo());
		int index = this.getCursorPosition();
		while (index > 0 && this.isSeparator(tags, index - 1)) {
			index--;
		}

		while (index > 0 && !this.isSeparator(tags, index - 1)) {
			index--;
		}

		return index;
	}

	private boolean isMac() {
		return Platform.current() == Platform.MACOS;
	}

	private boolean isWordKeyDown() {
		return this.isMac() ? UI.isAltKeyDown() : UI.isCtrlKeyDown();
	}

	private boolean isShortcutKeyDown() {
		return this.isMac() ? Key.LEFT_SUPER.isDown() || Key.RIGHT_SUPER.isDown() : UI.isCtrlKeyDown();
	}

	private boolean isSeparator(final int[] tags, final int index) {
		final char c = this.text.charAt(index);
		return tags[index] == -1 && (c == ' ' || c == '\n');
	}

	private static void release(final List<Node> nodeList, final FieldNode<?> pressed) {
		for (final Node node : nodeList) {
			if (node != pressed && node instanceof FieldNode && ((FieldNode<?>) node).focused) {
				((FieldNode<?>) node).release();
			}

			FieldNode.release(node.getChildren(), pressed);
		}
	}

	private static <X> boolean settle(final InteractiveValue<X> value, final X next) {
		value.set(next);
		value.commit();
		return Objects.equals(value.get(), next);
	}

	private static @NonNull String cut(final String text, final int codepoints) {
		return text.codePointCount(0, text.length()) <= codepoints ? text : text.substring(0, text.offsetByCodePoints(0, codepoints));
	}

}