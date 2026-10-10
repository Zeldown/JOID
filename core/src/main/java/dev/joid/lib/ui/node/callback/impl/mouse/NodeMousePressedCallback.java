package dev.joid.lib.ui.node.callback.impl.mouse;

import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Phase;
import lombok.NonNull;

@FunctionalInterface
public interface NodeMousePressedCallback<T extends Node> extends NodeCallback {

	public void apply(final @NonNull T node, final double mouseX, final double mouseY, final @NonNull MouseButton button);

	@NodeCallbackMethod(Phase.PRE)
	public default void pre(final @NonNull T node, final @NonNull DispatchContext context, final double mouseX, final double mouseY, final @NonNull MouseButton button) {}

	@NodeCallbackMethod(Phase.POST)
	public default void post(final @NonNull T node, final @NonNull DispatchContext context, final double mouseX, final double mouseY, final @NonNull MouseButton button) {
		this.apply(node, mouseX, mouseY, button);
	}

}