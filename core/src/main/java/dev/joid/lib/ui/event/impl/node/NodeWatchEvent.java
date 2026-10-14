package dev.joid.lib.ui.event.impl.node;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.event.NodeEvent;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.property.watch.WatchProperty;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class NodeWatchEvent<N extends Node> extends NodeEvent<N> {

	private final Signal<?>           signal;
	private final List<WatchProperty> properties;

	private NodeWatchEvent(final N node, final Signal<?> signal, final WatchProperty[] properties) {
		super(node);
		this.signal = signal;
		this.properties = Collections.unmodifiableList(Arrays.asList(properties.clone()));
	}

	public final @NonNull List<WatchProperty> getProperties() {
		return Collections.unmodifiableList(this.properties);
	}

	public static <N extends Node> @NonNull NodeWatchEvent<N> create(final @NonNull N node, final @NonNull Signal<?> signal, final @NonNull WatchProperty @NonNull... properties) {
		return new NodeWatchEvent<>(node, signal, properties);
	}

}