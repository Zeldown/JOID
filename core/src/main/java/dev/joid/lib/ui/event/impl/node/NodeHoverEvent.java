package dev.joid.lib.ui.event.impl.node;

import dev.joid.lib.ui.event.NodeEvent;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class NodeHoverEvent<N extends Node> extends NodeEvent<N> {

	private final double mouseX;
	private final double mouseY;

	private NodeHoverEvent(final N node, final double mouseX, final double mouseY) {
		super(node);
		this.mouseX = mouseX;
		this.mouseY = mouseY;
	}

	public static <N extends Node> @NonNull NodeHoverEvent<N> create(final @NonNull N node, final double mouseX, final double mouseY) {
		return new NodeHoverEvent<>(node, mouseX, mouseY);
	}

}