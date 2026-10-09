package dev.joid.lib.ui.node.callback.impl.scroll;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Phase;
import dev.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeScrollEndCallback<T extends Node> extends NodeCallback {

	public void apply(final @NonNull T node, final double scrollX, final double scrollY);

	@NodeCallbackMethod(Phase.PRE)
	public default void pre(final @NonNull T node, final @NonNull InternalContext context, final double scrollX, final double scrollY) {}

	@NodeCallbackMethod(Phase.POST)
	public default void post(final @NonNull T node, final @NonNull InternalContext context, final double scrollX, final double scrollY) {
		context.cancel(() -> this.apply(node, scrollX, scrollY));
	}

}