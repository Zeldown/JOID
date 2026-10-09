package dev.joid.lib.ui.node.callback.impl.animation;

import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Phase;
import lombok.NonNull;

@FunctionalInterface
public interface NodeAnimationCallback<T extends Node> extends NodeCallback {

	public void apply(final @NonNull T node, final @NonNull TweenAnimator animator, final float value);

	@NodeCallbackMethod(Phase.PRE)
	public default void pre(final @NonNull T node, final @NonNull DispatchContext context, final @NonNull TweenAnimator animator, final float value) {}

	@NodeCallbackMethod(Phase.POST)
	public default void post(final @NonNull T node, final @NonNull DispatchContext context, final @NonNull TweenAnimator animator, final float value) {
		context.cancel(() -> this.apply(node, animator, value));
	}

}