package be.zeldown.joid.lib.ui.node.impl.structure.selector.callback;

import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.callback.NodeCallback;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import be.zeldown.joid.lib.ui.node.impl.structure.selector.SelectorNode;
import be.zeldown.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeSelectorChangeCallback<T extends SelectorNode> extends NodeCallback {

	void apply(final @NonNull T node, final @NonNull Node selected);

	@NodeCallbackMethod(Type.PRE)
	default void pre(final @NonNull T node, final @NonNull InternalContext context, final @NonNull Node selected) {}

	@NodeCallbackMethod(Type.POST)
	default void post(final @NonNull T node, final @NonNull InternalContext context, final @NonNull Node selected) {
		context.cancel(() -> this.apply(node, selected));
	}

}