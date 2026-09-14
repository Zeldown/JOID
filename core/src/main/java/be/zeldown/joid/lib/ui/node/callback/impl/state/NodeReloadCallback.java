package be.zeldown.joid.lib.ui.node.callback.impl.state;

import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.callback.NodeCallback;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import be.zeldown.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeReloadCallback<T extends Node> extends NodeCallback {

	void apply(final @NonNull T node);

	@NodeCallbackMethod(Type.PRE)
	default void pre(final @NonNull T node, final @NonNull InternalContext context) {}

	@NodeCallbackMethod(Type.POST)
	default void post(final @NonNull T node, final @NonNull InternalContext context) {
		context.cancel(() -> this.apply(node));
	}

}