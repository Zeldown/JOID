package dev.joid.lib.ui.node.impl.design.resource.callback;

import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Phase;
import dev.joid.lib.ui.node.impl.design.resource.ResourcePlayerNode;
import dev.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeResourcePlayerProgressCallback<T extends ResourcePlayerNode> extends NodeCallback {

	public void apply(final @NonNull T node, final double progress, final double currentTime);

	@NodeCallbackMethod(Phase.PRE)
	public default void pre(final @NonNull T node, final @NonNull InternalContext context, final double progress, final double currentTime) {}

	@NodeCallbackMethod(Phase.POST)
	public default void post(final @NonNull T node, final @NonNull InternalContext context, final double progress, final double currentTime) {
		context.cancel(() -> this.apply(node, progress, currentTime));
	}

}