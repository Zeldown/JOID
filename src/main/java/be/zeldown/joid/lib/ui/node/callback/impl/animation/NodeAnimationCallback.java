package be.zeldown.joid.lib.ui.node.callback.impl.animation;

import be.zeldown.joid.lib.animation.animator.TweenAnimator;
import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.callback.NodeCallback;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import be.zeldown.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeAnimationCallback<T extends Node> extends NodeCallback {

	void apply(final @NonNull T node, final @NonNull TweenAnimator animator, final float value);

	@NodeCallbackMethod(Type.PRE)
	default void pre(final @NonNull T node, final @NonNull InternalContext context, final @NonNull TweenAnimator animator, final float value) {}

	@NodeCallbackMethod(Type.POST)
	default void post(final @NonNull T node, final @NonNull InternalContext context, final @NonNull TweenAnimator animator, final float value) {
		context.cancel(() -> this.apply(node, animator, value));
	}

}