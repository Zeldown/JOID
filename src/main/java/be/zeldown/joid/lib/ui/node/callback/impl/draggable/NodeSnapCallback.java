package be.zeldown.joid.lib.ui.node.callback.impl.draggable;

import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.callback.NodeCallback;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import be.zeldown.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeSnapCallback<T extends Node> extends NodeCallback {

	void apply(final @NonNull T node, final @NonNull Node snapNode);

	@NodeCallbackMethod(Type.PRE)
	default void pre(final @NonNull T node, final @NonNull InternalContext context, final @NonNull Node snapNode) {}

	@NodeCallbackMethod(Type.POST)
	default void post(final @NonNull T node, final @NonNull InternalContext context, final @NonNull Node snapNode) {
		context.cancel(() -> this.apply(node, snapNode));
	}

}