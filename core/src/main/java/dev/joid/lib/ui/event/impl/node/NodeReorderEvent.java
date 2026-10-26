package dev.joid.lib.ui.event.impl.node;

import dev.joid.lib.ui.event.ICancellableEvent;
import dev.joid.lib.ui.event.NodeEvent;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class NodeReorderEvent<N extends Node> extends NodeEvent<N> {

	private final int  index;
	private final Node child;
	private final int  previous;

	private NodeReorderEvent(final N node, final Node child, final int previous, final int index) {
		super(node);
		this.child = child;
		this.previous = previous;
		this.index = index;
	}

	@Getter
	public static final class Pre<N extends Node> extends NodeReorderEvent<N> implements ICancellableEvent {

		private boolean cancelled;

		private Pre(final N node, final Node child, final int previous, final int index) {
			super(node, child, previous, index);
		}

		public static <N extends Node> @NonNull Pre<N> create(final @NonNull N node, final @NonNull Node child, final int previous, final int index) {
			return new Pre<>(node, child, previous, index);
		}

		@Override
		public void cancel() {
			super.checkWritable("cancel");
			this.cancelled = true;
		}

	}

	public static final class Post<N extends Node> extends NodeReorderEvent<N> {

		private Post(final N node, final Node child, final int previous, final int index) {
			super(node, child, previous, index);
		}

		public static <N extends Node> @NonNull Post<N> create(final @NonNull N node, final @NonNull Node child, final int previous, final int index) {
			return new Post<>(node, child, previous, index);
		}

	}

}