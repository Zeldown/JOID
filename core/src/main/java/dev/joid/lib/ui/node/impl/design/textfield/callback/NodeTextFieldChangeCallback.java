package dev.joid.lib.ui.node.impl.design.textfield.callback;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import dev.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeTextFieldChangeCallback<T extends Node, V> extends NodeCallback {

	public void apply(final @NonNull T node, final @NonNull String text, final V value, final boolean valid);

	@NodeCallbackMethod(Type.PRE)
	public default void pre(final @NonNull T node, final @NonNull InternalContext context, final @NonNull String text, final V value, final boolean valid) {}

	@NodeCallbackMethod(Type.POST)
	public default void post(final @NonNull T node, final @NonNull InternalContext context, final @NonNull String text, final V value, final boolean valid) {
		context.cancel(() -> this.apply(node, text, value, valid));
	}

}