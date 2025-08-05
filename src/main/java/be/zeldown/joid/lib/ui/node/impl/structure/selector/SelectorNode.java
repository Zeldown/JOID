package be.zeldown.joid.lib.ui.node.impl.structure.selector;

import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import be.zeldown.joid.lib.ui.node.impl.structure.selector.callback.NodeSelectorChangeCallback;
import be.zeldown.joid.lib.utils.click.ClickType;
import be.zeldown.joid.lib.utils.context.InternalContext;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public abstract class SelectorNode extends Node {

	private static final int CALLBACK_CHANGE = NodeCallbackRegistry.next(NodeSelectorChangeCallback.class);

	private SelectorDirection direction;

	private boolean active;
	private Node selected;

	protected SelectorNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.direction = SelectorDirection.DOWN;
		this.active    = false;
		this.selected  = null;
	}

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

	public abstract void drawBackground(final double mouseX, final double mouseY);

	@Override
	public final void mousePressed(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
		if (this.selected == null) {
			return;
		}

		if (this.active) {
			Node tmpSelected = null;
			for (final Node child : this.getChildren()) {
				if (!child.isHovered(mouseX, mouseY)) {
					continue;
				}

				tmpSelected = child;
				break;
			}

			if (tmpSelected == null || tmpSelected == this.selected) {
				this.active = false;
				return;
			}

			final Node selected = tmpSelected == null ? this.selected : tmpSelected;
			context.cancel(() -> {
				super.executeCallback(SelectorNode.CALLBACK_CHANGE, context, () -> {
					this.selected = selected;
					this.active = false;
				}, selected);
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

	public final <T extends SelectorNode> @NonNull T direction(final @NonNull SelectorDirection direction) {
		this.direction = direction;
		return (T) this;
	}

	public final <T extends SelectorNode> @NonNull T active(final boolean active) {
		this.active = active;
		return (T) this;
	}

	public final <T extends SelectorNode> @NonNull T selected(final @NonNull Node selected) {
		this.selected = selected;
		return (T) this;
	}

	/* [ Callback Section ] */
	public final <T extends SelectorNode> @NonNull T onChange(final @NonNull NodeSelectorChangeCallback<T> callback) {
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