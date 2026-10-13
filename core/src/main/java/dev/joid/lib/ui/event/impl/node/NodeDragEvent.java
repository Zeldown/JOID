package dev.joid.lib.ui.event.impl.node;

import dev.joid.lib.ui.event.CancellableEvent;
import dev.joid.lib.ui.event.NodeEvent;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class NodeDragEvent<N extends Node> extends NodeEvent<N> {

	private final Node target;

	private NodeDragEvent(final N node, final Node target) {
		super(node);
		this.target = target;
	}

	@Getter
	public static final class Pre<N extends Node> extends NodeDragEvent<N> implements CancellableEvent {

		private boolean cancelled;

		private Pre(final N node, final Node target) {
			super(node, target);
		}

		public static <N extends Node> @NonNull Pre<N> create(final @NonNull N node, final Node target) {
			return new Pre<>(node, target);
		}

		@Override
		public void cancel() {
			super.checkWritable("cancel");
			this.cancelled = true;
		}

	}

	public static final class Post<N extends Node> extends NodeDragEvent<N> {

		private Post(final N node, final Node target) {
			super(node, target);
		}

		public static <N extends Node> @NonNull Post<N> create(final @NonNull N node, final Node target) {
			return new Post<>(node, target);
		}

	}

}