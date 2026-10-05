package dev.joid.lib.ui.node.callback.impl.signal;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import dev.joid.lib.ui.node.property.watch.WatchProperty;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.signal.Signal;
import lombok.NonNull;

@FunctionalInterface
public interface NodeWatchCallback<T extends Node> extends NodeCallback {

	public void apply(final @NonNull T node, final @NonNull Signal<?> signal, final @NonNull WatchProperty @NonNull... properties);

	@NodeCallbackMethod(Type.PRE)
	public default void pre(final @NonNull T node, final @NonNull InternalContext context, final @NonNull Signal<?> signal, final @NonNull WatchProperty @NonNull... properties) {}

	@NodeCallbackMethod(Type.POST)
	public default void post(final @NonNull T node, final @NonNull InternalContext context, final @NonNull Signal<?> signal, final @NonNull WatchProperty @NonNull... properties) {
		context.cancel(() -> this.apply(node, signal, properties));
	}

}