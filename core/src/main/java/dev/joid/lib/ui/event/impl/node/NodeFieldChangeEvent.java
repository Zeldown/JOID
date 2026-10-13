package dev.joid.lib.ui.event.impl.node;

import dev.joid.lib.ui.event.CancellableEvent;
import dev.joid.lib.ui.event.NodeEvent;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class NodeFieldChangeEvent<N extends Node, V> extends NodeEvent<N> {

	private final V       value;
	private final String  text;
	private final boolean valid;
	private final String  previous;

	private NodeFieldChangeEvent(final N node, final String previous, final String text, final V value, final boolean valid) {
		super(node);
		this.previous = previous;
		this.text = text;
		this.value = value;
		this.valid = valid;
	}

	@Getter
	public static final class Pre<N extends Node, V> extends NodeFieldChangeEvent<N, V> implements CancellableEvent {

		private boolean cancelled;

		private Pre(final N node, final String previous, final String text, final V value, final boolean valid) {
			super(node, previous, text, value, valid);
		}

		public static <N extends Node, V> @NonNull Pre<N, V> create(final @NonNull N node, final @NonNull String previous, final @NonNull String text, final V value, final boolean valid) {
			return new Pre<>(node, previous, text, value, valid);
		}

		@Override
		public void cancel() {
			super.checkWritable("cancel");
			this.cancelled = true;
		}

	}

	public static final class Post<N extends Node, V> extends NodeFieldChangeEvent<N, V> {

		private Post(final N node, final String previous, final String text, final V value, final boolean valid) {
			super(node, previous, text, value, valid);
		}

		public static <N extends Node, V> @NonNull Post<N, V> create(final @NonNull N node, final @NonNull String previous, final @NonNull String text, final V value, final boolean valid) {
			return new Post<>(node, previous, text, value, valid);
		}

	}

}