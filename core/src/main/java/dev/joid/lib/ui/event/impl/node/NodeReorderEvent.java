package dev.joid.lib.ui.event.impl.node;

import dev.joid.lib.ui.event.ICancellableEvent;
import dev.joid.lib.ui.event.NodeEvent;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class NodeReorderEvent<N extends Node> extends NodeEvent<N> {

	private final Node child;
	private final int  oldIndex;
	private final int  newIndex;

	private NodeReorderEvent(final N node, final Node child, final int oldIndex, final int newIndex) {
		super(node);
		this.child = child;
		this.oldIndex = oldIndex;
		this.newIndex = newIndex;
	}

	@Getter
	public static final class Pre<N extends Node> extends NodeReorderEvent<N> implements ICancellableEvent {

		private boolean cancelled;

		private Pre(final N node, final Node child, final int oldIndex, final int newIndex) {
			super(node, child, oldIndex, newIndex);
		}

		public static <N extends Node> @NonNull Pre<N> create(final @NonNull N node, final @NonNull Node child, final int oldIndex, final int newIndex) {
			return new Pre<>(node, child, oldIndex, newIndex);
		}

		@Override
		public void cancel() {
			super.checkWritable("cancel");
			this.cancelled = true;
		}

	}

	public static final class Post<N extends Node> extends NodeReorderEvent<N> {

		private Post(final N node, final Node child, final int oldIndex, final int newIndex) {
			super(node, child, oldIndex, newIndex);
		}

		public static <N extends Node> @NonNull Post<N> create(final @NonNull N node, final @NonNull Node child, final int oldIndex, final int newIndex) {
			return new Post<>(node, child, oldIndex, newIndex);
		}

	}

}