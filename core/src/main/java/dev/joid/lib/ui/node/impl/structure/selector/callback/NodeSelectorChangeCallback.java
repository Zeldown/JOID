package dev.joid.lib.ui.node.impl.structure.selector.callback;

import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Phase;
import dev.joid.lib.ui.node.impl.structure.selector.SelectorNode;
import lombok.NonNull;

@FunctionalInterface
public interface NodeSelectorChangeCallback<T extends SelectorNode<V>, V> extends NodeCallback {

	public void apply(final @NonNull T node, final @NonNull V value);

	@NodeCallbackMethod(Phase.PRE)
	public default void pre(final @NonNull T node, final @NonNull DispatchContext context, final @NonNull V value) {}

	@NodeCallbackMethod(Phase.POST)
	public default void post(final @NonNull T node, final @NonNull DispatchContext context, final @NonNull V value) {
		context.cancel(() -> this.apply(node, value));
	}

}