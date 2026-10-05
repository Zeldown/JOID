package dev.joid.lib.ui.node.impl.structure.toggle;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import dev.joid.lib.ui.node.impl.structure.toggle.callback.NodeToggleChangeCallback;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public abstract class ToggleNode<F, S> extends Node {

	public static final int CALLBACK_CHANGE = NodeCallbackRegistry.next(NodeToggleChangeCallback.class);

	private boolean toggle;
	private ToggleState<F, S> state;

	protected ToggleNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	@Override
	public void mousePressed(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
		if (context.isCancelled() || !super.isHovered(mouseX, mouseY)) {
			return;
		}

		context.cancel(() -> {
			super.executeCallback(ToggleNode.CALLBACK_CHANGE, context, () -> {
				this.toggle = !this.toggle;
			}, !this.toggle);
		});
	}

	public final <T extends ToggleNode<F, S>> @NonNull T toggle(final boolean toggle) {
		this.toggle = toggle;
		return (T) this;
	}

	public final <T extends ToggleNode<F, S>> @NonNull T state(final F toggle, final S back) {
		this.state = new ToggleState<>(toggle, back);
		return (T) this;
	}

	public final <T> @NonNull T getValue() {
		return (T) (this.toggle ? this.state.getToggle() : this.state.getBack());
	}

	public final <T extends ToggleNode<F, S>> @NonNull T onChange(final @NonNull NodeToggleChangeCallback<T, F, S> callback) {
		super.registerCallback(ToggleNode.CALLBACK_CHANGE, callback);
		return (T) this;
	}

}