package dev.joid.lib.ui.node.callback.impl.draggable;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import dev.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeSnapCallback<T extends Node> extends NodeCallback {

	public void apply(final @NonNull T node, final @NonNull Node snapNode);

	@NodeCallbackMethod(Type.PRE)
	public default void pre(final @NonNull T node, final @NonNull InternalContext context, final @NonNull Node snapNode) {}

	@NodeCallbackMethod(Type.POST)
	public default void post(final @NonNull T node, final @NonNull InternalContext context, final @NonNull Node snapNode) {
		context.cancel(() -> this.apply(node, snapNode));
	}

}