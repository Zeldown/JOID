package dev.joid.lib.ui.node.property;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.node.Node;
import lombok.NonNull;

@FunctionalInterface
public interface WatchProperty {

	public static final WatchProperty BODY           = (node, signal) -> {
		if (node.getBodyConsumer() != null) {
			node.getBodyConsumer().accept(node);
		}
	};
	public static final WatchProperty CLEAR_CHILDREN = (node, signal) -> node.clearChildren();

	public void apply(final @NonNull Node node, final @NonNull Signal<?> signal);

}