package dev.joid.lib.ui.node.impl.design.textfield.callback;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Phase;
import dev.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeTextFieldEnterCallback<T extends Node> extends NodeCallback {

	public void apply(final @NonNull T node, final @NonNull String text);

	@NodeCallbackMethod(Phase.PRE)
	public default void pre(final @NonNull T node, final @NonNull InternalContext context, final @NonNull String text) {}

	@NodeCallbackMethod(Phase.POST)
	public default void post(final @NonNull T node, final @NonNull InternalContext context, final @NonNull String text) {
		context.cancel(() -> this.apply(node, text));
	}

}