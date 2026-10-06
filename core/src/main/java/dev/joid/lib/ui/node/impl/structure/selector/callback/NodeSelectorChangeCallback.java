package dev.joid.lib.ui.node.impl.structure.selector.callback;

import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import dev.joid.lib.ui.node.impl.structure.selector.SelectorNode;
import dev.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeSelectorChangeCallback<T extends SelectorNode<V>, V> extends NodeCallback {

	public void apply(final @NonNull T node, final @NonNull V value);

	@NodeCallbackMethod(Type.PRE)
	public default void pre(final @NonNull T node, final @NonNull InternalContext context, final @NonNull V value) {}

	@NodeCallbackMethod(Type.POST)
	public default void post(final @NonNull T node, final @NonNull InternalContext context, final @NonNull V value) {
		context.cancel(() -> this.apply(node, value));
	}

}