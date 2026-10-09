package dev.joid.lib.ui.node.impl.structure.slider.callback;

import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Phase;
import dev.joid.lib.ui.node.impl.structure.slider.SliderNode;
import lombok.NonNull;

@FunctionalInterface
public interface NodeSliderChangeCallback<T extends SliderNode<O>, O> extends NodeCallback {

	public void apply(final @NonNull T node, final @NonNull O value);

	@NodeCallbackMethod(Phase.PRE)
	public default void pre(final @NonNull T node, final @NonNull DispatchContext context, final @NonNull O value) {}

	@NodeCallbackMethod(Phase.POST)
	public default void post(final @NonNull T node, final @NonNull DispatchContext context, final @NonNull O value) {
		context.cancel(() -> this.apply(node, value));
	}

}