package dev.joid.lib.ui.node.impl.structure.selector;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import dev.joid.lib.ui.node.impl.structure.selector.callback.NodeSelectorChangeCallback;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.signal.Signal;
import dev.joid.lib.utils.signal.SignalSubscriber;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public abstract class SelectorNode<V> extends Node {

	public static final int CALLBACK_CHANGE = NodeCallbackRegistry.next(NodeSelectorChangeCallback.class);

	private final Map<Node, V> optionMap;

	private SelectorDirection   direction;
	private Signal<V>           signal;
	private SignalSubscriber<V> subscription;

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
	public final void draw(final double mouseX, final double mouseY) {
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
	public final void mousePressed(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
		if (this.selected == null) {
			return;
		}

		if (this.active) {
			Node clicked = null;
			for (final Node child : this.getChildren()) {
				if (child.isHovered(mouseX, mouseY)) {
					clicked = child;
					break;
				}
			}

			if (context.isCancelled() || clicked == null && !super.isHovered(mouseX, mouseY)) {
				this.active = false;
				return;
			}

			if (clicked == null || clicked == this.selected) {
				this.active = false;
				context.cancel();
				return;
			}

			final Node option = clicked;
			context.cancel(() -> this.select(option, context));
			return;
		}

		if (this.selected.isHovered(mouseX, mouseY)) {
			context.cancel(() -> this.active = true);
		}
	}

	@Override
	public void detach() {
		this.active = false;
	}

	public final boolean isSelected(final @NonNull Node node) {
		return this.selected == node;
	}

	public final @NonNull Map<Node, V> getOptionMap() {
		return Collections.unmodifiableMap(this.optionMap);
	}

	public final V getValue() {
		return this.selected == null ? null : this.optionMap.get(this.selected);
	}

	@SafeVarargs
	public final <T extends SelectorNode<V>> @NonNull T values(final @NonNull V value, final @NonNull V @NonNull... values) {
		if (!Arrays.asList(values).contains(value)) {
			throw new IllegalArgumentException("The value " + value + " is not an option of the selector");
		}

		final V previous = this.getValue();
		super.clearChildren();
		this.optionMap.clear();
		for (final V option : values) {
			final Node node = this.option(option);
			this.optionMap.put(node, option);
			super.append(node);
		}

		if (value.equals(previous)) {
			this.selected = this.find(value);
			return (T) this;
		}
		return this.value(value);
	}

	public final <T extends SelectorNode<V>> @NonNull T value(final @NonNull V value) {
		this.select(this.find(value), InternalContext.create());
		return (T) this;
	}

	public final <T extends SelectorNode<V>> @NonNull T signal(final @NonNull Signal<V> signal) {
		this.signal = super.writable(signal);
		this.subscription = super.rebind(this.subscription, signal, value -> {
			if (this.optionMap.containsValue(value)) {
				this.value(value);
			}
		});
		return (T) this;
	}

	public final <T extends SelectorNode<V>> @NonNull T direction(final @NonNull SelectorDirection direction) {
		this.direction = direction;
		return (T) this;
	}

	public final <T extends SelectorNode<V>> @NonNull T active(final boolean active) {
		this.active = active;
		return (T) this;
	}

	public final <T extends SelectorNode<V>> @NonNull T onChange(final @NonNull NodeSelectorChangeCallback<T, V> callback) {
		super.registerCallback(SelectorNode.CALLBACK_CHANGE, callback);
		return (T) this;
	}

	private @NonNull Node find(final V value) {
		for (final Map.Entry<Node, V> entry : this.optionMap.entrySet()) {
			if (Objects.equals(entry.getValue(), value)) {
				return entry.getKey();
			}
		}
		throw new IllegalArgumentException("The value " + value + " is not an option of the selector");
	}

	private void select(final Node option, final InternalContext context) {
		if (option == this.selected) {
			return;
		}

		final V value = this.optionMap.get(option);
		super.executeCallback(SelectorNode.CALLBACK_CHANGE, context, () -> {
			this.selected = option;
			this.active   = false;
			super.sync(this.signal, value);
		}, value);
	}

	public static enum SelectorDirection {

		UP,
		DOWN;

		public final boolean isDown() {
			return this == SelectorDirection.DOWN;
		}

	}

}