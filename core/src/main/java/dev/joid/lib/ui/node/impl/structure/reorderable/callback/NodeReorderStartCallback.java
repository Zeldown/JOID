package dev.joid.lib.ui.node.impl.structure.reorderable.callback;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Phase;
import dev.joid.lib.ui.node.impl.structure.reorderable.ReorderableFlexNode;
import lombok.NonNull;

@FunctionalInterface
public interface NodeReorderStartCallback extends NodeCallback {

	public void apply(final @NonNull ReorderableFlexNode node, final @NonNull Node child);

	@NodeCallbackMethod(Phase.PRE)
	public default void pre(final @NonNull ReorderableFlexNode node, final @NonNull DispatchContext context, final @NonNull Node child) {}

	@NodeCallbackMethod(Phase.POST)
	public default void post(final @NonNull ReorderableFlexNode node, final @NonNull DispatchContext context, final @NonNull Node child) {
		context.cancel(() -> this.apply(node, child));
	}

}