package be.zeldown.joid.lib.ui.node.impl.structure.sw.callback;

import be.zeldown.joid.lib.ui.node.callback.NodeCallback;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import be.zeldown.joid.lib.ui.node.impl.structure.sw.SwitchNode;
import be.zeldown.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeSwitchChangeCallback<T extends SwitchNode> extends NodeCallback {

	void apply(final @NonNull T node, final @NonNull String value);

	@NodeCallbackMethod(Type.PRE)
	default void pre(final @NonNull T node, final @NonNull InternalContext context, final @NonNull String value) {}

	@NodeCallbackMethod(Type.POST)
	default void post(final @NonNull T node, final @NonNull InternalContext context, final @NonNull String value) {
		context.cancel(() -> this.apply(node, value));
	}

}