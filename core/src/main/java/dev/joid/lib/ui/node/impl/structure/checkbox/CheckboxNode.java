package dev.joid.lib.ui.node.impl.structure.checkbox;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import dev.joid.lib.ui.node.impl.structure.checkbox.callback.NodeCheckboxChangeCallback;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.signal.Signal;
import dev.joid.lib.utils.signal.SignalSubscriber;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public abstract class CheckboxNode extends Node {

	public static final int CALLBACK_CHANGE = NodeCallbackRegistry.next(NodeCheckboxChangeCallback.class);

	private boolean                   checked;
	private Signal<Boolean>           signal;
	private SignalSubscriber<Boolean> subscription;

	protected CheckboxNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	@Override
	public void mousePressed(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
		if (context.isCancelled() || !super.isHovered(mouseX, mouseY)) {
			return;
		}

		context.cancel(() -> this.change(!this.checked, context));
	}

	public final <T extends CheckboxNode> @NonNull T checked(final boolean checked) {
		this.change(checked, InternalContext.create());
		return (T) this;
	}

	public final <T extends CheckboxNode> @NonNull T signal(final @NonNull Signal<Boolean> signal) {
		this.signal = super.writable(signal);
		this.subscription = super.rebind(this.subscription, signal, value -> this.change(value, InternalContext.create()));
		return (T) this;
	}

	public final <T extends CheckboxNode> @NonNull T onChange(final @NonNull NodeCheckboxChangeCallback<T> callback) {
		super.registerCallback(CheckboxNode.CALLBACK_CHANGE, callback);
		return (T) this;
	}

	private void change(final boolean checked, final InternalContext context) {
		if (this.checked == checked) {
			return;
		}

		super.executeCallback(CheckboxNode.CALLBACK_CHANGE, context, () -> {
			this.checked = checked;
			super.sync(this.signal, checked);
		}, checked);
	}

}