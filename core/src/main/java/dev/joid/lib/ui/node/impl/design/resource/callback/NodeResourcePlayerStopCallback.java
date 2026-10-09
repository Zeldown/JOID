package dev.joid.lib.ui.node.impl.design.resource.callback;

import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Phase;
import dev.joid.lib.ui.node.impl.design.resource.ResourcePlayerNode;
import lombok.NonNull;

@FunctionalInterface
public interface NodeResourcePlayerStopCallback<T extends ResourcePlayerNode> extends NodeCallback {

	public void apply(final @NonNull T node);

	@NodeCallbackMethod(Phase.PRE)
	public default void pre(final @NonNull T node, final @NonNull DispatchContext context) {}

	@NodeCallbackMethod(Phase.POST)
	public default void post(final @NonNull T node, final @NonNull DispatchContext context) {
		context.cancel(() -> this.apply(node));
	}

}