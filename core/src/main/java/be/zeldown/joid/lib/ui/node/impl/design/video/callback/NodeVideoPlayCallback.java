package be.zeldown.joid.lib.ui.node.impl.design.video.callback;

import be.zeldown.joid.lib.ui.node.callback.NodeCallback;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import be.zeldown.joid.lib.ui.node.impl.design.video.VideoPlayerNode;
import be.zeldown.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@FunctionalInterface
public interface NodeVideoPlayCallback<T extends VideoPlayerNode> extends NodeCallback {

	void apply(final @NonNull T node);

	@NodeCallbackMethod(Type.PRE)
	default void pre(final @NonNull T node, final @NonNull InternalContext context) {}

	@NodeCallbackMethod(Type.POST)
	default void post(final @NonNull T node, final @NonNull InternalContext context) {
		context.cancel(() -> this.apply(node));
	}

}