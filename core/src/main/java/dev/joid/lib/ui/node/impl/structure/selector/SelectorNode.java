package dev.joid.lib.ui.node.impl.structure.selector;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.impl.node.NodeChangeEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

public abstract class SelectorNode<V> extends Node {

	private static final EventType<NodeChangeEvent.Post<?, ?>> CHANGE        = EventType.create("onChange");
	private static final EventType<NodeChangeEvent.Pre<?, ?>>  BEFORE_CHANGE = EventType.create("beforeChange");

	private static final EventType<NodeChangeEvent.Post<?, Boolean>> OPEN         = EventType.create("onOpen");
	private static final EventType<NodeChangeEvent.Post<?, Boolean>> CLOSE        = EventType.create("onClose");
	private static final EventType<NodeChangeEvent.Pre<?, Boolean>>  BEFORE_OPEN  = EventType.create("beforeOpen");
	private static final EventType<NodeChangeEvent.Pre<?, Boolean>>  BEFORE_CLOSE = EventType.create("beforeClose");

	private final Map<Node, V> optionMap;

	@Getter private SelectorDirection direction;

	@Getter private Node    selected;
	@Getter private boolean active;

	private V      pendingValue;
	private double raise;
	private double raisedLayoutY;

	protected SelectorNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.optionMap = new LinkedHashMap<>();
		this.direction = SelectorDirection.DOWN;
		this.active    = false;
		this.selected  = null;

		super.<SelectorNode<V>>require("values", "call values(...) before attaching it", node -> !node.optionMap.isEmpty());
	}

	protected abstract @NonNull Node option(final @NonNull V value);

	public abstract void drawBackground(final double mouseX, final double mouseY);

	@Override
	public void draw(final double mouseX, final double mouseY) {
		for (final Node child : super.getChildren()) {
			if (!this.optionMap.containsKey(child)) {
				super.remove(child);
				throw new IllegalStateException("The node " + child.getClass().getSimpleName() + " is not an option of the selector, add the options with values(...)");
			}
		}

		final double size = super.getDefaultHeight();
		final double height = this.active ? size * this.optionMap.size() : size;
		final double raise = this.direction.isDown() ? 0D : height - size;
		final double layoutY = super.getLayoutY() == this.raisedLayoutY ? super.getLayoutY() + this.raise : super.getLayoutY();
		this.raise = raise;
		this.raisedLayoutY = layoutY - raise;
		super.layoutPosition(this, super.getLayoutX(), this.raisedLayoutY);

		this.selected.y(raise);

		double oy = this.direction.isDown() ? size : raise - size;
		for (final Node child : super.getChildren()) {
			child.x(0);
			child.width(super.getDefaultWidth());
			child.height(size);

			if (this.isSelected(child)) {
				continue;
			}

			child.y(oy);
			child.visible(node -> this.active || this.isSelected(node));
			oy += this.direction.isDown() ? size : -size;
		}

		super.height(height);
		this.drawBackground(mouseX, mouseY);
	}

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (!event.getButton().isLeft()) {
			return;
		}

		if (this.active) {
			Node clicked = null;
			for (final Node child : this.getChildren()) {
				if (child.isHovered()) {
					clicked = child;
					break;
				}
			}

			if (event.isConsumed() || clicked == null && !super.isHovered()) {
				this.activate(false, true);
				return;
			}

			if (clicked == null || clicked == this.selected) {
				this.activate(false, true);
				event.consume();
				return;
			}

			this.select(clicked, true);
			event.consume();
			return;
		}

		if (this.selected.isHovered()) {
			this.activate(true, true);
			event.consume();
		}
	}

	@Override
	public void detach() {
		this.activate(false, false);
	}

	public final boolean isSelected(final @NonNull Node node) {
		return this.selected == node;
	}

	public final V getValue() {
		if (this.optionMap.isEmpty()) {
			throw new IllegalStateException("The " + this.getClass().getSimpleName() + " has no values, call values(...) before getValue()");
		}

		return this.optionMap.get(this.selected);
	}

	public final @NonNull Map<Node, V> getOptionMap() {
		return Collections.unmodifiableMap(this.optionMap);
	}

	@SafeVarargs
	public final <T extends SelectorNode<V>> @NonNull T values(final @NonNull V @NonNull... values) {
		if (values.length == 0) {
			throw new IllegalArgumentException("A selector needs at least one option, got none: pass them to values(...)");
		}

		final V previous = this.optionMap.get(this.selected);
		final V pending = this.pendingValue;
		this.pendingValue = null;
		super.clearChildren();
		this.optionMap.clear();
		for (final V option : values) {
			final Node node = this.option(option);
			this.optionMap.put(node, option);
			super.append(node);
		}

		final V value;
		if (pending != null) {
			value = pending;
		} else if (this.optionMap.containsValue(previous)) {
			value = previous;
		} else {
			value = values[0];
		}

		if (Objects.equals(value, previous)) {
			this.selected = this.find(value);
		} else {
			this.change(this.find(value), previous, false);
		}
		return (T) this;
	}

	public final <T extends SelectorNode<V>> @NonNull T value(final @NonNull V value) {
		return super.bind("value", value, (final SelectorNode<V> node, final V option) -> node.choose(option, false));
	}

	public final <T extends SelectorNode<V>> @NonNull T value(final @NonNull Signal<V> value) {
		return super.bind("value", value, (final SelectorNode<V> node, final V option) -> {
			if (node.optionMap.isEmpty() || node.optionMap.containsValue(option)) {
				node.choose(option, false);
			}
		});
	}

	public final <T extends SelectorNode<V>> @NonNull T direction(final @NonNull SelectorDirection direction) {
		return this.direction(Signal.from(direction));
	}

	public final <T extends SelectorNode<V>> @NonNull T direction(final @NonNull Supplier<@NonNull SelectorDirection> direction) {
		return super.follow("direction", direction, (final SelectorNode<V> node, final SelectorDirection value) -> node.direction = value);
	}

	public final <T extends SelectorNode<V>> @NonNull T active(final boolean active) {
		return super.bind("active", active, (final SelectorNode<V> node, final Boolean value) -> node.activate(value, false));
	}

	public final <T extends SelectorNode<V>> @NonNull T active(final @NonNull Signal<Boolean> active) {
		return super.bind("active", active, (final SelectorNode<V> node, final Boolean value) -> node.activate(value, false));
	}

	public final <T extends SelectorNode<V>> @NonNull T change(final @NonNull V value) {
		this.choose(value, true);
		return (T) this;
	}

	public final <T extends SelectorNode<V>> @NonNull T beforeChange(final @NonNull EventListener<NodeChangeEvent.Pre<T, V>> listener) {
		return super.listen(SelectorNode.BEFORE_CHANGE, listener);
	}

	public final <T extends SelectorNode<V>> @NonNull T onChange(final @NonNull EventListener<NodeChangeEvent.Post<T, V>> listener) {
		return super.listen(SelectorNode.CHANGE, listener);
	}

	public final <T extends SelectorNode<V>> @NonNull T open() {
		this.activate(true, true);
		return (T) this;
	}

	public final <T extends SelectorNode<V>> @NonNull T close() {
		this.activate(false, true);
		return (T) this;
	}

	public final <T extends SelectorNode<V>> @NonNull T beforeOpen(final @NonNull EventListener<NodeChangeEvent.Pre<T, Boolean>> listener) {
		return super.listen(SelectorNode.BEFORE_OPEN, listener);
	}

	public final <T extends SelectorNode<V>> @NonNull T onOpen(final @NonNull EventListener<NodeChangeEvent.Post<T, Boolean>> listener) {
		return super.listen(SelectorNode.OPEN, listener);
	}

	public final <T extends SelectorNode<V>> @NonNull T beforeClose(final @NonNull EventListener<NodeChangeEvent.Pre<T, Boolean>> listener) {
		return super.listen(SelectorNode.BEFORE_CLOSE, listener);
	}

	public final <T extends SelectorNode<V>> @NonNull T onClose(final @NonNull EventListener<NodeChangeEvent.Post<T, Boolean>> listener) {
		return super.listen(SelectorNode.CLOSE, listener);
	}

	private @NonNull Node find(final V value) {
		for (final Map.Entry<Node, V> entry : this.optionMap.entrySet()) {
			if (Objects.equals(entry.getValue(), value)) {
				return entry.getKey();
			}
		}
		throw new IllegalArgumentException("The value " + value + " is not an option of the " + this.getClass().getSimpleName() + " " + this.optionMap.values() + ", pass one of them or add it with values(...)");
	}

	private void choose(final V value, final boolean refusable) {
		if (this.optionMap.isEmpty()) {
			this.pendingValue = value;
			return;
		}

		this.select(this.find(value), refusable);
	}

	private void select(final Node option, final boolean refusable) {
		if (option != this.selected) {
			this.change(option, this.optionMap.get(this.selected), refusable);
		}
	}

	private void change(final Node option, final V previous, final boolean refusable) {
		final V value = this.optionMap.get(option);
		super.change(SelectorNode.BEFORE_CHANGE, SelectorNode.CHANGE, previous, value, refusable, () -> {
			this.selected = option;
			super.sync("value", value);
			this.activate(false, false);
		});
	}

	private void activate(final boolean active, final boolean refusable) {
		if (this.active == active) {
			return;
		}

		super.change(active ? SelectorNode.BEFORE_OPEN : SelectorNode.BEFORE_CLOSE, active ? SelectorNode.OPEN : SelectorNode.CLOSE, this.active, active, refusable, () -> {
			this.active = active;
			super.sync("active", active);
		});
	}

	public static enum SelectorDirection {

		UP,
		DOWN;

		public final boolean isDown() {
			return this == SelectorDirection.DOWN;
		}

	}

}