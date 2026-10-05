package dev.joid.lib.ui.node.callback.impl.mouse;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeMouseDraggedCallback<T extends Node> extends NodeCallback {

	public void apply(final @NonNull T node, final double mouseX, final double mouseY, final @NonNull ClickType clickType, final long deltaTime);

	@NodeCallbackMethod(Type.PRE)
	public default void pre(final @NonNull T node, final @NonNull InternalContext context, final double mouseX, final double mouseY, final @NonNull ClickType clickType, final long deltaTime) {}

	@NodeCallbackMethod(Type.POST)
	public default void post(final @NonNull T node, final @NonNull InternalContext context, final double mouseX, final double mouseY, final @NonNull ClickType clickType, final long deltaTime) {
		context.cancel(() -> this.apply(node, mouseX, mouseY, clickType, deltaTime));
	}

}