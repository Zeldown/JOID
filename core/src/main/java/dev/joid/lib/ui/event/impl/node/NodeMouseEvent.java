package dev.joid.lib.ui.event.impl.node;

import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.ui.event.ConsumableEvent;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class NodeMouseEvent<N extends Node> extends NodeInputEvent<N> {

	private final double      mouseX;
	private final double      mouseY;
	private final long        deltaTime;
	private final MouseButton button;

	private NodeMouseEvent(final N node, final double mouseX, final double mouseY, final MouseButton button, final long deltaTime, final ConsumableEvent source) {
		super(node, source);
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.button = button;
		this.deltaTime = deltaTime;
	}

	public static <N extends Node> @NonNull NodeMouseEvent<N> create(final @NonNull N node, final double mouseX, final double mouseY, final @NonNull MouseButton button, final long deltaTime, final ConsumableEvent source) {
		return new NodeMouseEvent<>(node, mouseX, mouseY, button, deltaTime, source);
	}

}