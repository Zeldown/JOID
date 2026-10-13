package dev.joid.lib.ui.event.impl.node;

import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.ui.event.NodeEvent;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class NodeAnimateEvent<N extends Node> extends NodeEvent<N> {

	private final float         value;
	private final TweenAnimator animator;

	private NodeAnimateEvent(final N node, final TweenAnimator animator, final float value) {
		super(node);
		this.animator = animator;
		this.value = value;
	}

	public static <N extends Node> @NonNull NodeAnimateEvent<N> create(final @NonNull N node, final @NonNull TweenAnimator animator, final float value) {
		return new NodeAnimateEvent<>(node, animator, value);
	}

}