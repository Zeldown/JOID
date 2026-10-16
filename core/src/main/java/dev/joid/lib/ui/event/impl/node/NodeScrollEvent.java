package dev.joid.lib.ui.event.impl.node;

import dev.joid.lib.ui.event.ICancellableEvent;
import dev.joid.lib.ui.event.NodeEvent;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.utils.align.Axis;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class NodeScrollEvent<N extends Node> extends NodeEvent<N> {

	private final double     offset;
	private final double     previous;
	private final Axis axis;

	private NodeScrollEvent(final N node, final Axis axis, final double previous, final double offset) {
		super(node);
		this.axis = axis;
		this.previous = previous;
		this.offset = offset;
	}

	@Getter
	public static final class Pre<N extends Node> extends NodeScrollEvent<N> implements ICancellableEvent {

		private boolean cancelled;

		private Pre(final N node, final Axis axis, final double previous, final double offset) {
			super(node, axis, previous, offset);
		}

		public static <N extends Node> @NonNull Pre<N> create(final @NonNull N node, final @NonNull Axis axis, final double previous, final double offset) {
			return new Pre<>(node, axis, previous, offset);
		}

		@Override
		public void cancel() {
			super.checkWritable("cancel");
			this.cancelled = true;
		}

	}

	public static final class Post<N extends Node> extends NodeScrollEvent<N> {

		private Post(final N node, final Axis axis, final double previous, final double offset) {
			super(node, axis, previous, offset);
		}

		public static <N extends Node> @NonNull Post<N> create(final @NonNull N node, final @NonNull Axis axis, final double previous, final double offset) {
			return new Post<>(node, axis, previous, offset);
		}

	}

}