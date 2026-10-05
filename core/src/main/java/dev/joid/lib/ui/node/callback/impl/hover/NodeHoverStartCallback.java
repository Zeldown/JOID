package dev.joid.lib.ui.node.callback.impl.hover;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import dev.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeHoverStartCallback<T extends Node> extends NodeCallback {

	public void apply(final @NonNull T node, final double mouseX, final double mouseY);

	@NodeCallbackMethod(Type.PRE)
	public default void pre(final @NonNull T node, final @NonNull InternalContext context, final double mouseX, final double mouseY) {}

	@NodeCallbackMethod(Type.POST)
	public default void post(final @NonNull T node, final @NonNull InternalContext context, final double mouseX, final double mouseY) {
		context.cancel(() -> this.apply(node, mouseX, mouseY));
	}

}