package dev.joid.lib.ui.node.callback.impl.key;

import dev.joid.lib.input.key.Key;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Phase;
import lombok.NonNull;

@FunctionalInterface
public interface NodeKeyPressedCallback<T extends Node> extends NodeCallback {

	public void apply(final @NonNull T node, final char c, final @NonNull Key key);

	@NodeCallbackMethod(Phase.PRE)
	public default void pre(final @NonNull T node, final @NonNull DispatchContext context, final char c, final @NonNull Key key) {}

	@NodeCallbackMethod(Phase.POST)
	public default void post(final @NonNull T node, final @NonNull DispatchContext context, final char c, final @NonNull Key key) {
		if (!context.isCancelled()) {
			this.apply(node, c, key);
		}
	}

}