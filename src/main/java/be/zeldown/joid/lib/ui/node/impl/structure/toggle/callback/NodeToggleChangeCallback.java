package be.zeldown.joid.lib.ui.node.impl.structure.toggle.callback;

import be.zeldown.joid.lib.ui.node.callback.NodeCallback;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import be.zeldown.joid.lib.ui.node.impl.structure.toggle.ToggleNode;
import be.zeldown.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeToggleChangeCallback<T extends ToggleNode<F, S>, F, S> extends NodeCallback {

	void apply(final @NonNull T node, final boolean toggle);

	@NodeCallbackMethod(Type.PRE)
	default void pre(final @NonNull T node, final @NonNull InternalContext context, final boolean toggle) {}

	@NodeCallbackMethod(Type.POST)
	default void post(final @NonNull T node, final @NonNull InternalContext context, final boolean toggle) {
		context.cancel(() -> this.apply(node, toggle));
	}

}