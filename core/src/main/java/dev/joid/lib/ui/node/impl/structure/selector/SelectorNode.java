package dev.joid.lib.ui.node.impl.structure.selector;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import dev.joid.lib.signal.ISignalSubscriber;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventPriority;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.impl.node.NodeChangeEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public abstract class SelectorNode<V> extends Node {

	private static final EventType<NodeChangeEvent.Post<?, ?>> CHANGE        = EventType.create("onChange");
	private static final EventType<NodeChangeEvent.Pre<?, ?>>  BEFORE_CHANGE = EventType.create("beforeChange");

	private final Map<Node, V> optionMap;

	private Signal<V>            signal;
	private SelectorDirection    direction;
	private ISignalSubscriber<V> subscription;

	private Node    selected;
	private boolean active;

	protected SelectorNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.optionMap = new LinkedHashMap<>();
		this.direction = SelectorDirection.DOWN;
		this.active    = false;
		this.selected  = null;
	}

	protected abstract @NonNull Node option(final @NonNull V value);

	public abstract void drawBackground(final double mouseX, final double mouseY);

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (super.getChildren().isEmpty()) {
			return;
		}

		for (final Node child : super.getChildren().ordered()) {
			if (!this.optionMap.containsKey(child)) {
				super.getChildren().remove(child);
				throw new IllegalStateException("The node " + child.getClass().getSimpleName() + " is not an option of the selector, add the options with values(...)");
			}
		}

		if (this.selected == null) {
			this.selected = super.getChildren().ordered().get(0);
		}

		this.selected.y(0);

		double oy = super.getDefaultHeight();
		for (final Node child : super.getChildren()) {
			child.x(0);
			child.width(super.getDefaultWidth());
			child.height(super.getDefaultHeight());

			if (this.isSelected(child)) {
				continue;
			}

			child.y(this.direction.isDown() ? oy : -oy);
			child.visible(node -> this.active || this.isSelected(node));
			oy += super.getDefaultHeight();
		}

		super.height(this.active && this.direction.isDown() ? oy : super.getDefaultHeight());
		this.drawBackground(mouseX, mouseY);
	}

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (this.selected == null) {
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
				this.active = false;
				return;
			}

			if (clicked == null || clicked == this.selected) {
				this.active = false;
				event.consume();
				return;
			}

			this.select(clicked, true);
			event.consume();
			return;
		}

		if (this.selected.isHovered()) {
			this.active = true;
			event.consume();
		}
	}

	@Override
	public void detach() {
		this.active = false;
	}

	public final boolean isSelected(final @NonNull Node node) {
		return this.selected == node;
	}

	public final V getValue() {
		return this.selected == null ? null : this.optionMap.get(this.selected);
	}

	public final @NonNull Map<Node, V> getOptionMap() {
		return Collections.unmodifiableMap(this.optionMap);
	}

	@SafeVarargs
	public final <T extends SelectorNode<V>> @NonNull T values(final @NonNull V @NonNull... values) {
		if (values.length == 0) {
			throw new IllegalArgumentException("A selector needs at least one option");
		}

		final V previous = this.getValue();
		super.clearChildren();
		this.optionMap.clear();
		for (final V option : values) {
			final Node node = this.option(option);
			this.optionMap.put(node, option);
			super.append(node);
		}

		if (this.optionMap.containsValue(previous)) {
			this.selected = this.find(previous);
		} else {
			this.select(this.find(values[0]), false);
		}
		return (T) this;
	}

	public final <T extends SelectorNode<V>> @NonNull T value(final @NonNull V value) {
		return this.value(Signal.from(value));
	}

	public final <T extends SelectorNode<V>> @NonNull T value(final @NonNull Supplier<@NonNull V> value) {
		return super.follow("value", value, option -> this.select(this.find(option), false));
	}

	public final <T extends SelectorNode<V>> @NonNull T signal(final @NonNull Signal<V> signal) {
		this.signal = super.writable(signal);
		this.subscription = super.rebind(this.subscription, signal, value -> {
			if (this.optionMap.containsValue(value)) {
				this.select(this.find(value), false);
			}
		});
		return (T) this;
	}

	public final <T extends SelectorNode<V>> @NonNull T direction(final @NonNull SelectorDirection direction) {
		return this.direction(Signal.from(direction));
	}

	public final <T extends SelectorNode<V>> @NonNull T direction(final @NonNull Supplier<@NonNull SelectorDirection> direction) {
		return super.follow("direction", direction, value -> this.direction = value);
	}

	public final <T extends SelectorNode<V>> @NonNull T active(final boolean active) {
		return this.active(Signal.from(active));
	}

	public final <T extends SelectorNode<V>> @NonNull T active(final @NonNull Supplier<Boolean> active) {
		return super.follow("active", active, value -> this.active = value);
	}

	public final <T extends SelectorNode<V>> @NonNull T beforeChange(final @NonNull EventListener<NodeChangeEvent.Pre<T, V>> listener) {
		return this.beforeChange(EventPriority.NORMAL, listener);
	}

	public final <T extends SelectorNode<V>> @NonNull T beforeChange(final @NonNull EventPriority priority, final @NonNull EventListener<NodeChangeEvent.Pre<T, V>> listener) {
		return super.listen(SelectorNode.BEFORE_CHANGE, priority, listener);
	}

	public final <T extends SelectorNode<V>> @NonNull T onChange(final @NonNull EventListener<NodeChangeEvent.Post<T, V>> listener) {
		return this.onChange(EventPriority.NORMAL, listener);
	}

	public final <T extends SelectorNode<V>> @NonNull T onChange(final @NonNull EventPriority priority, final @NonNull EventListener<NodeChangeEvent.Post<T, V>> listener) {
		return super.listen(SelectorNode.CHANGE, priority, listener);
	}

	private @NonNull Node find(final V value) {
		for (final Map.Entry<Node, V> entry : this.optionMap.entrySet()) {
			if (Objects.equals(entry.getValue(), value)) {
				return entry.getKey();
			}
		}
		throw new IllegalArgumentException("The value " + value + " is not an option of the selector");
	}

	private void select(final Node option, final boolean user) {
		if (option == this.selected) {
			return;
		}

		final V value = this.optionMap.get(option);
		super.change(SelectorNode.BEFORE_CHANGE, SelectorNode.CHANGE, this.getValue(), value, user, () -> {
			this.selected = option;
			this.active   = false;
			super.sync(this.signal, value);
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