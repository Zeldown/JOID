package be.zeldown.joid.lib.ui.node.callback.impl.signal;

import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.callback.NodeCallback;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import be.zeldown.joid.lib.ui.node.property.watch.WatchProperty;
import be.zeldown.joid.lib.utils.context.InternalContext;
import be.zeldown.joid.lib.utils.signal.Signal;
import lombok.NonNull;

@FunctionalInterface
public interface NodeWatchCallback<T extends Node> extends NodeCallback {

	void apply(final @NonNull T node, final @NonNull Signal<?> signal, final @NonNull WatchProperty @NonNull... properties);

	@NodeCallbackMethod(Type.PRE)
	default void pre(final @NonNull T node, final @NonNull InternalContext context, final @NonNull Signal<?> signal, final @NonNull WatchProperty @NonNull... properties) {}

	@NodeCallbackMethod(Type.POST)
	default void post(final @NonNull T node, final @NonNull InternalContext context, final @NonNull Signal<?> signal, final @NonNull WatchProperty @NonNull... properties) {
		context.cancel(() -> this.apply(node, signal, properties));
	}

}