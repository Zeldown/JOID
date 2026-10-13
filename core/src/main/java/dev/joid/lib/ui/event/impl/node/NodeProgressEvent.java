package dev.joid.lib.ui.event.impl.node;

import dev.joid.lib.ui.event.NodeEvent;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class NodeProgressEvent<N extends Node> extends NodeEvent<N> {

	private final double progress;
	private final double currentTime;

	private NodeProgressEvent(final N node, final double progress, final double currentTime) {
		super(node);
		this.progress = progress;
		this.currentTime = currentTime;
	}

	public static <N extends Node> @NonNull NodeProgressEvent<N> create(final @NonNull N node, final double progress, final double currentTime) {
		return new NodeProgressEvent<>(node, progress, currentTime);
	}

}