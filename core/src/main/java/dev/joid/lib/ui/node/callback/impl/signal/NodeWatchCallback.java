package dev.joid.lib.ui.node.callback.impl.signal;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Phase;
import dev.joid.lib.ui.node.property.watch.WatchProperty;
import lombok.NonNull;

@FunctionalInterface
public interface NodeWatchCallback<T extends Node> extends NodeCallback {

	public void apply(final @NonNull T node, final @NonNull Signal<?> signal, final @NonNull WatchProperty @NonNull... properties);

	@NodeCallbackMethod(Phase.PRE)
	public default void pre(final @NonNull T node, final @NonNull DispatchContext context, final @NonNull Signal<?> signal, final @NonNull WatchProperty @NonNull... properties) {}

	@NodeCallbackMethod(Phase.POST)
	public default void post(final @NonNull T node, final @NonNull DispatchContext context, final @NonNull Signal<?> signal, final @NonNull WatchProperty @NonNull... properties) {
		context.cancel(() -> this.apply(node, signal, properties));
	}

}