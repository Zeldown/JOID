package dev.joid.lib.ui.event.impl.node;

import dev.joid.lib.ui.event.ICancellableEvent;
import dev.joid.lib.ui.event.NodeEvent;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class NodeChangeEvent<N extends Node, V> extends NodeEvent<N> {

	private final V value;
	private final V previous;

	private NodeChangeEvent(final N node, final V previous, final V value) {
		super(node);
		this.previous = previous;
		this.value = value;
	}

	@Getter
	public static final class Pre<N extends Node, V> extends NodeChangeEvent<N, V> implements ICancellableEvent {

		private boolean cancelled;

		private Pre(final N node, final V previous, final V value) {
			super(node, previous, value);
		}

		public static <N extends Node, V> @NonNull Pre<N, V> create(final @NonNull N node, final V previous, final V value) {
			return new Pre<>(node, previous, value);
		}

		@Override
		public void cancel() {
			super.checkWritable("cancel");
			this.cancelled = true;
		}

	}

	public static final class Post<N extends Node, V> extends NodeChangeEvent<N, V> {

		private Post(final N node, final V previous, final V value) {
			super(node, previous, value);
		}

		public static <N extends Node, V> @NonNull Post<N, V> create(final @NonNull N node, final V previous, final V value) {
			return new Post<>(node, previous, value);
		}

	}

}