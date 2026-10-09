package dev.joid.lib.ui.node.callback.impl.mouse;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Phase;
import dev.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeMouseScrollCallback<T extends Node> extends NodeCallback {

	public void apply(final @NonNull T node, final double mouseX, final double mouseY, final double notchesX, final double notchesY);

	@NodeCallbackMethod(Phase.PRE)
	public default void pre(final @NonNull T node, final @NonNull InternalContext context, final double mouseX, final double mouseY, final double notchesX, final double notchesY) {}

	@NodeCallbackMethod(Phase.POST)
	public default void post(final @NonNull T node, final @NonNull InternalContext context, final double mouseX, final double mouseY, final double notchesX, final double notchesY) {
		if (!context.isCancelled()) {
			this.apply(node, mouseX, mouseY, notchesX, notchesY);
		}
	}

}