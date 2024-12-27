package be.zeldown.joid.lib.ui.node.impl.structure.checkbox.callback;

import be.zeldown.joid.lib.ui.node.callback.NodeCallback;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import be.zeldown.joid.lib.ui.node.impl.structure.checkbox.CheckboxNode;
import be.zeldown.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeCheckboxChangeCallback<T extends CheckboxNode> extends NodeCallback {

	void apply(final @NonNull T node, final boolean checked);

	@NodeCallbackMethod(Type.PRE)
	default void pre(final @NonNull T node, final @NonNull InternalContext context, final boolean checked) {}

	@NodeCallbackMethod(Type.POST)
	default void post(final @NonNull T node, final @NonNull InternalContext context, final boolean checked) {
		context.cancel(() -> this.apply(node, checked));
	}

}