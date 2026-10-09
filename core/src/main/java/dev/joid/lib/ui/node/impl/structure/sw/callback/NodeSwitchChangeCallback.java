package dev.joid.lib.ui.node.impl.structure.sw.callback;

import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Phase;
import dev.joid.lib.ui.node.impl.structure.sw.SwitchNode;
import dev.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeSwitchChangeCallback<T extends SwitchNode> extends NodeCallback {

	public void apply(final @NonNull T node, final @NonNull String value);

	@NodeCallbackMethod(Phase.PRE)
	public default void pre(final @NonNull T node, final @NonNull InternalContext context, final @NonNull String value) {}

	@NodeCallbackMethod(Phase.POST)
	public default void post(final @NonNull T node, final @NonNull InternalContext context, final @NonNull String value) {
		context.cancel(() -> this.apply(node, value));
	}

}