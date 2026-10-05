package dev.joid.lib.ui.node.impl.design.textfield.callback;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import dev.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeTextFieldChangeCallback<T extends Node> extends NodeCallback {

	public void apply(final @NonNull T node, final @NonNull String oldText, final @NonNull String newText);

	@NodeCallbackMethod(Type.PRE)
	public default void pre(final @NonNull T node, final @NonNull InternalContext context, final @NonNull String oldText, final @NonNull String newText) {}

	@NodeCallbackMethod(Type.POST)
	public default void post(final @NonNull T node, final @NonNull InternalContext context, final @NonNull String oldText, final @NonNull String newText) {
		context.cancel(() -> this.apply(node, oldText, newText));
	}

}