package dev.joid.lib.ui.node.callback.impl.key;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.key.Key;
import lombok.NonNull;

@FunctionalInterface
public interface NodeKeyPressedCallback<T extends Node> extends NodeCallback {

	public void apply(final @NonNull T node, final char c, final @NonNull Key key);

	@NodeCallbackMethod(Type.PRE)
	public default void pre(final @NonNull T node, final @NonNull InternalContext context, final char c, final @NonNull Key key) {}

	@NodeCallbackMethod(Type.POST)
	public default void post(final @NonNull T node, final @NonNull InternalContext context, final char c, final @NonNull Key key) {
		if (!context.isCancelled()) {
			this.apply(node, c, key);
		}
	}

}