package dev.joid.lib.ui.event.impl.node;

import dev.joid.lib.ui.event.NodeEvent;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class NodeChildEvent<N extends Node> extends NodeEvent<N> {

	@NonNull
	private final Node child;

	private NodeChildEvent(final N node, final Node child) {
		super(node);
		this.child = child;
	}

	public static <N extends Node> @NonNull NodeChildEvent<N> create(final @NonNull N node, final @NonNull Node child) {
		return new NodeChildEvent<>(node, child);
	}

}