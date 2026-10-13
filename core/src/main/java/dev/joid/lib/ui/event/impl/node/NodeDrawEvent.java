package dev.joid.lib.ui.event.impl.node;

import dev.joid.lib.ui.event.NodeEvent;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class NodeDrawEvent<N extends Node> extends NodeEvent<N> {

	private final double mouseX;
	private final double mouseY;

	private NodeDrawEvent(final N node, final double mouseX, final double mouseY) {
		super(node);
		this.mouseX = mouseX;
		this.mouseY = mouseY;
	}

	public static <N extends Node> @NonNull NodeDrawEvent<N> create(final @NonNull N node, final double mouseX, final double mouseY) {
		return new NodeDrawEvent<>(node, mouseX, mouseY);
	}

}