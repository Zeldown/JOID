package be.zeldown.joid.lib.ui.node.property.watch;

import java.util.function.Consumer;

import be.zeldown.joid.lib.ui.node.Node;
import lombok.NonNull;

public enum WatchProperty {

	NONE(node -> {}),
	CLEAR_CHILDREN(Node::clearChildren),
	BODY(node -> {
		if (node.getBodyConsumer() == null) {
			return;
		}

		node.getBodyConsumer().accept(node);
	}),
	RELOAD(Node::reload);

	private final @NonNull Consumer<@NonNull Node> callback;

	WatchProperty(final @NonNull Consumer<@NonNull Node> callback) {
		this.callback = callback;
	}

	public void apply(final @NonNull Node node) {
		this.callback.accept(node);
	}

}