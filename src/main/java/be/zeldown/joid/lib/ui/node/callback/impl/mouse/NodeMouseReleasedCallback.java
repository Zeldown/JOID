package be.zeldown.joid.lib.ui.node.callback.impl.mouse;

import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.callback.NodeCallback;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import be.zeldown.joid.lib.utils.click.ClickType;
import be.zeldown.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeMouseReleasedCallback<T extends Node> extends NodeCallback {

	void apply(final @NonNull T node, final double mouseX, final double mouseY, final @NonNull ClickType clickType);

	@NodeCallbackMethod(Type.PRE)
	default void pre(final @NonNull T node, final @NonNull InternalContext context, final double mouseX, final double mouseY, final @NonNull ClickType clickType) {}

	@NodeCallbackMethod(Type.POST)
	default void post(final @NonNull T node, final @NonNull InternalContext context, final double mouseX, final double mouseY, final @NonNull ClickType clickType) {
		context.cancel(() -> this.apply(node, mouseX, mouseY, clickType));
	}

}