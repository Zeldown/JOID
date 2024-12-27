package be.zeldown.joid.lib.ui.node.impl.design.textfield.callback;

import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.callback.NodeCallback;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import be.zeldown.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeTextFieldChangeCallback<T extends Node> extends NodeCallback {

	void apply(final @NonNull T node, final @NonNull String oldText, final @NonNull String newText);

	@NodeCallbackMethod(Type.PRE)
	default void pre(final @NonNull T node, final @NonNull InternalContext context, final @NonNull String oldText, final @NonNull String newText) {}

	@NodeCallbackMethod(Type.POST)
	default void post(final @NonNull T node, final @NonNull InternalContext context, final @NonNull String oldText, final @NonNull String newText) {
		context.cancel(() -> this.apply(node, oldText, newText));
	}

}