package dev.joid.lib.ui.node.impl.structure.selector;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import dev.joid.lib.ui.node.impl.structure.selector.callback.NodeSelectorChangeCallback;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.signal.Signal;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public abstract class SelectorNode<V> extends Node {

	public static final int CALLBACK_CHANGE = NodeCallbackRegistry.next(NodeSelectorChangeCallback.class);

	private final Map<Node, V> optionMap;

	private SelectorDirection direction;
	private Signal<V>         signal;

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

			if (clicked == null || clicked == this.selected) {
				this.active = false;
				return;
			}

			final Node option = clicked;
			context.cancel(() -> {
				super.executeCallback(SelectorNode.CALLBACK_CHANGE, context, () -> {
					this.selected = option;
					this.active = false;
					if (this.signal != null) {
						this.signal.set(this.optionMap.get(option));
					}
				}, this.optionMap.get(option));
			});
			return;
		}

		if (this.selected.isHovered(mouseX, mouseY)) {
			context.cancel(() -> this.active = true);
		}
	}

	public final boolean isSelected(final @NonNull Node node) {
		return this.selected == node;
	}

	public final @NonNull Map<Node, V> getOptionMap() {
		return Collections.unmodifiableMap(this.optionMap);
	}

	public final @NonNull Optional<V> getValue() {
		return Optional.ofNullable(this.selected).map(this.optionMap::get);
	}

	@SafeVarargs
	public final <T extends SelectorNode<V>> @NonNull T values(final @NonNull V value, final @NonNull V @NonNull... values) {
		if (!Arrays.asList(values).contains(value)) {
			throw new IllegalArgumentException("The value " + value + " is not an option of the selector");
		}

		super.clearChildren();
		this.optionMap.clear();
		for (final V option : values) {
			final Node node = this.option(option);
			this.optionMap.put(node, option);
			super.append(node);
		}
		return this.value(value);
	}

	public final <T extends SelectorNode<V>> @NonNull T value(final @NonNull V value) {
		for (final Map.Entry<Node, V> entry : this.optionMap.entrySet()) {
			if (Objects.equals(entry.getValue(), value)) {
				this.selected = entry.getKey();
				super.sync(this.signal, value);
				return (T) this;
			}
		}
		throw new IllegalArgumentException("The value " + value + " is not an option of the selector");
	}

	public final <T extends SelectorNode<V>> @NonNull T signal(final @NonNull Signal<V> signal) {
		this.signal = signal;
		super.bind(signal, value -> {
			if (this.optionMap.containsValue(value) && !value.equals(this.getValue().orElse(null))) {
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

	public static enum SelectorDirection {

		UP,
		DOWN;

		public final boolean isDown() {
			return this == SelectorDirection.DOWN;
		}

	}

}