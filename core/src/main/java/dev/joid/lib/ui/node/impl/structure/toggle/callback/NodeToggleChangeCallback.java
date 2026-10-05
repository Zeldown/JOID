package dev.joid.lib.ui.node.impl.structure.toggle.callback;

import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import dev.joid.lib.ui.node.impl.structure.toggle.ToggleNode;
import dev.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeToggleChangeCallback<T extends ToggleNode<F, S>, F, S> extends NodeCallback {

	public void apply(final @NonNull T node, final boolean toggle);

	@NodeCallbackMethod(Type.PRE)
	public default void pre(final @NonNull T node, final @NonNull InternalContext context, final boolean toggle) {}

	@NodeCallbackMethod(Type.POST)
	public default void post(final @NonNull T node, final @NonNull InternalContext context, final boolean toggle) {
		context.cancel(() -> this.apply(node, toggle));
	}

}