package dev.joid.lib.ui.event.impl.node;

import dev.joid.lib.ui.event.ConsumableEvent;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class NodeCharEvent<N extends Node> extends NodeInputEvent<N> {

	private final int codepoint;

	private NodeCharEvent(final N node, final int codepoint, final ConsumableEvent source) {
		super(node, source);
		this.codepoint = codepoint;
	}

	public static <N extends Node> @NonNull NodeCharEvent<N> create(final @NonNull N node, final int codepoint, final ConsumableEvent source) {
		return new NodeCharEvent<>(node, codepoint, source);
	}

}