package dev.joid.lib.ui.event.impl.node;

import dev.joid.lib.ui.event.NodeEvent;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class NodeScrollEvent<N extends Node> extends NodeEvent<N> {

	private final double     offset;
	private final ScrollAxis axis;

	private NodeScrollEvent(final N node, final ScrollAxis axis, final double offset) {
		super(node);
		this.axis = axis;
		this.offset = offset;
	}

	public static <N extends Node> @NonNull NodeScrollEvent<N> create(final @NonNull N node, final @NonNull ScrollAxis axis, final double offset) {
		return new NodeScrollEvent<>(node, axis, offset);
	}

	public static enum ScrollAxis {

		HORIZONTAL,
		VERTICAL;

		public final boolean isHorizontal() {
			return this == ScrollAxis.HORIZONTAL;
		}

	}

}