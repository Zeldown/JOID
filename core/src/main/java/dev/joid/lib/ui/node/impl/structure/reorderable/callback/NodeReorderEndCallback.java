package dev.joid.lib.ui.node.impl.structure.reorderable.callback;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import dev.joid.lib.ui.node.impl.structure.reorderable.ReorderableFlexNode;
import dev.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeReorderEndCallback extends NodeCallback {

	public void apply(final @NonNull ReorderableFlexNode node, final @NonNull Node child, final int oldIndex, final int newIndex);

	@NodeCallbackMethod(Type.PRE)
	public default void pre(final @NonNull ReorderableFlexNode node, final @NonNull InternalContext context, final @NonNull Node child, final int oldIndex, final int newIndex) {}

	@NodeCallbackMethod(Type.POST)
	public default void post(final @NonNull ReorderableFlexNode node, final @NonNull InternalContext context, final @NonNull Node child, final int oldIndex, final int newIndex) {
		context.cancel(() -> this.apply(node, child, oldIndex, newIndex));
	}

}