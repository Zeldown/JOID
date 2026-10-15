package dev.joid.lib.ui.event.impl.node;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dev.joid.lib.ui.event.NodeEvent;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.property.watch.WatchProperty;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class NodeWatchEvent<N extends Node> extends NodeEvent<N> {

	private final Object              value;
	private final List<WatchProperty> properties;

	private NodeWatchEvent(final N node, final Object value, final WatchProperty[] properties) {
		super(node);
		this.value = value;
		this.properties = Collections.unmodifiableList(Arrays.asList(properties.clone()));
	}

	public static <N extends Node> @NonNull NodeWatchEvent<N> create(final @NonNull N node, final Object value, final @NonNull WatchProperty @NonNull... properties) {
		return new NodeWatchEvent<>(node, value, properties);
	}

}