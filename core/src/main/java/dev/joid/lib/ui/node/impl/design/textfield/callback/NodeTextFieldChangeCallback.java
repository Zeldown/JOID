package dev.joid.lib.ui.node.impl.design.textfield.callback;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Phase;
import lombok.NonNull;

@FunctionalInterface
public interface NodeTextFieldChangeCallback<T extends Node, V> extends NodeCallback {

	public void apply(final @NonNull T node, final @NonNull String text, final V value, final boolean valid);

	@NodeCallbackMethod(Phase.PRE)
	public default void pre(final @NonNull T node, final @NonNull DispatchContext context, final @NonNull String text, final V value, final boolean valid) {}

	@NodeCallbackMethod(Phase.POST)
	public default void post(final @NonNull T node, final @NonNull DispatchContext context, final @NonNull String text, final V value, final boolean valid) {
		context.cancel(() -> this.apply(node, text, value, valid));
	}

}