package be.zeldown.joid.lib.ui.node.callback.impl.key;

import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.callback.NodeCallback;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import be.zeldown.joid.lib.utils.context.InternalContext;
import be.zeldown.joid.lib.utils.key.Key;
import lombok.NonNull;

@FunctionalInterface
public interface NodeKeyPressedCallback<T extends Node> extends NodeCallback {

	void apply(final @NonNull T node, final char c, final @NonNull Key key);

	@NodeCallbackMethod(Type.PRE)
	default void pre(final @NonNull T node, final @NonNull InternalContext context, final char c, final @NonNull Key key) {}

	@NodeCallbackMethod(Type.POST)
	default void post(final @NonNull T node, final @NonNull InternalContext context, final char c, final @NonNull Key key) {
		context.cancel(() -> this.apply(node, c, key));
	}

}