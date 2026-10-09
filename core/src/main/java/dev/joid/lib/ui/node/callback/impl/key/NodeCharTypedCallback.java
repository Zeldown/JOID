package dev.joid.lib.ui.node.callback.impl.key;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Phase;
import lombok.NonNull;

@FunctionalInterface
public interface NodeCharTypedCallback<T extends Node> extends NodeCallback {

	public void apply(final @NonNull T node, final int codepoint);

	@NodeCallbackMethod(Phase.PRE)
	public default void pre(final @NonNull T node, final @NonNull DispatchContext context, final int codepoint) {}

	@NodeCallbackMethod(Phase.POST)
	public default void post(final @NonNull T node, final @NonNull DispatchContext context, final int codepoint) {
		if (!context.isCancelled()) {
			this.apply(node, codepoint);
		}
	}

}