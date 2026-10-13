package dev.joid.lib.ui.event.impl.node;

import dev.joid.lib.input.key.Key;
import dev.joid.lib.ui.event.ConsumableEvent;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class NodeKeyEvent<N extends Node> extends NodeInputEvent<N> {

	@NonNull
	private final Key key;

	private NodeKeyEvent(final N node, final Key key, final ConsumableEvent source) {
		super(node, source);
		this.key = key;
	}

	public static <N extends Node> @NonNull NodeKeyEvent<N> create(final @NonNull N node, final @NonNull Key key, final ConsumableEvent source) {
		return new NodeKeyEvent<>(node, key, source);
	}

}