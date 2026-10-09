package dev.joid.lib.ui.node.impl.structure.checkbox.callback;

import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Phase;
import dev.joid.lib.ui.node.impl.structure.checkbox.CheckboxNode;
import dev.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeCheckboxChangeCallback<T extends CheckboxNode> extends NodeCallback {

	public void apply(final @NonNull T node, final boolean checked);

	@NodeCallbackMethod(Phase.PRE)
	public default void pre(final @NonNull T node, final @NonNull InternalContext context, final boolean checked) {}

	@NodeCallbackMethod(Phase.POST)
	public default void post(final @NonNull T node, final @NonNull InternalContext context, final boolean checked) {
		context.cancel(() -> this.apply(node, checked));
	}

}