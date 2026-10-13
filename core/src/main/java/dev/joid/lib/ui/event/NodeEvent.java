package dev.joid.lib.ui.event;

import dev.joid.lib.ui.node.Node;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class NodeEvent<N extends Node> extends Event {

	@NonNull
	private final N node;

	public static <N extends Node> @NonNull NodeEvent<N> create(final @NonNull N node) {
		return new NodeEvent<>(node);
	}

}