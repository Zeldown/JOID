package be.zeldown.joid.lib.ui.node.impl.structure.reorderable.callback;

import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.callback.NodeCallback;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import be.zeldown.joid.lib.ui.node.impl.structure.reorderable.ReorderableFlexNode;
import be.zeldown.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeReorderEndCallback extends NodeCallback {

	void apply(final @NonNull ReorderableFlexNode node, final @NonNull Node child, final int oldIndex, final int newIndex);

	@NodeCallbackMethod(Type.PRE)
	default void pre(final @NonNull ReorderableFlexNode node, final @NonNull InternalContext context, final @NonNull Node child, final int oldIndex, final int newIndex) {}

	@NodeCallbackMethod(Type.POST)
	default void post(final @NonNull ReorderableFlexNode node, final @NonNull InternalContext context, final @NonNull Node child, final int oldIndex, final int newIndex) {
		context.cancel(() -> this.apply(node, child, oldIndex, newIndex));
	}

}