package be.zeldown.joid.lib.ui.node.callback.impl.scroll;

import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.callback.NodeCallback;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import be.zeldown.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeScrollEndCallback<T extends Node> extends NodeCallback {

	void apply(final @NonNull T node, final double scrollX, final double scrollY);

	@NodeCallbackMethod(Type.PRE)
	default void pre(final @NonNull T node, final @NonNull InternalContext context, final double scrollX, final double scrollY) {}

	@NodeCallbackMethod(Type.POST)
	default void post(final @NonNull T node, final @NonNull InternalContext context, final double scrollX, final double scrollY) {
		context.cancel(() -> this.apply(node, scrollX, scrollY));
	}

}