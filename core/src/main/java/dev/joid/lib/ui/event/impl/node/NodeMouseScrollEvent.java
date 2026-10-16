package dev.joid.lib.ui.event.impl.node;

import dev.joid.lib.ui.event.IConsumableEvent;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class NodeMouseScrollEvent<N extends Node> extends NodeInputEvent<N> {

	private final double mouseX;
	private final double mouseY;
	private final double notchesX;
	private final double notchesY;

	private NodeMouseScrollEvent(final N node, final double mouseX, final double mouseY, final double notchesX, final double notchesY, final IConsumableEvent source) {
		super(node, source);
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.notchesX = notchesX;
		this.notchesY = notchesY;
	}

	public static <N extends Node> @NonNull NodeMouseScrollEvent<N> create(final @NonNull N node, final double mouseX, final double mouseY, final double notchesX, final double notchesY, final IConsumableEvent source) {
		return new NodeMouseScrollEvent<>(node, mouseX, mouseY, notchesX, notchesY, source);
	}

}