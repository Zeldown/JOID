package be.zeldown.joid.lib.ui.node.impl.structure.checkbox;

import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import be.zeldown.joid.lib.ui.node.impl.structure.checkbox.callback.NodeCheckboxChangeCallback;
import be.zeldown.joid.lib.utils.click.ClickType;
import be.zeldown.joid.lib.utils.context.InternalContext;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public abstract class CheckboxNode extends Node {

	private static final int CALLBACK_CHANGE = NodeCallbackRegistry.next(NodeCheckboxChangeCallback.class);

	private boolean checked;

	protected CheckboxNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	@Override
	public void mousePressed(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
		if (context.isCancelled() || !super.isHovered(mouseX, mouseY)) {
			return;
		}

		context.cancel(() -> {
			super.executeCallback(CheckboxNode.CALLBACK_CHANGE, context, () -> {
				this.checked = !this.checked;
			}, !this.checked);
		});
	}

	public final <T extends CheckboxNode> @NonNull T checked(final boolean checked) {
		this.checked = checked;
		return (T) this;
	}

	/* [ Callback Section ] */
	public final <T extends CheckboxNode> @NonNull T onChange(final @NonNull NodeCheckboxChangeCallback<T> callback) {
		super.registerCallback(CheckboxNode.CALLBACK_CHANGE, callback);
		return (T) this;
	}

}