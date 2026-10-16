package dev.joid.lib.ui.event.impl.node;

import dev.joid.lib.ui.event.IConsumableEvent;
import dev.joid.lib.ui.event.NodeEvent;
import dev.joid.lib.ui.node.Node;

public abstract class NodeInputEvent<N extends Node> extends NodeEvent<N> implements IConsumableEvent {

	private final IConsumableEvent source;

	private boolean consumed;

	protected NodeInputEvent(final N node, final IConsumableEvent source) {
		super(node);
		this.source = source;
	}

	@Override
	public final void consume() {
		super.checkWritable("consume");
		if (this.source != null) {
			this.source.consume();
			return;
		}

		this.consumed = true;
	}

	@Override
	public final boolean isConsumed() {
		return this.source != null ? this.source.isConsumed() : this.consumed;
	}

}