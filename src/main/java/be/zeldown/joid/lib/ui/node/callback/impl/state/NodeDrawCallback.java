package be.zeldown.joid.lib.ui.node.callback.impl.state;

import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.callback.NodeCallback;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import be.zeldown.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeDrawCallback<T extends Node> extends NodeCallback {

	void apply(final @NonNull T node, final double mouseX, final double mouseY, final float partialTicks);

	@NodeCallbackMethod(Type.PRE)
	default void pre(final @NonNull T node, final @NonNull InternalContext context, final double mouseX, final double mouseY, final float partialTicks) {}

	@NodeCallbackMethod(Type.POST)
	default void post(final @NonNull T node, final @NonNull InternalContext context, final double mouseX, final double mouseY, final float partialTicks) {
		context.cancel(() -> this.apply(node, mouseX, mouseY, partialTicks));
	}

}