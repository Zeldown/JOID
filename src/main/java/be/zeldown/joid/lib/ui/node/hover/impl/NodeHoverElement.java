package be.zeldown.joid.lib.ui.node.hover.impl;

import be.zeldown.joid.lib.ui.node.Node;
import lombok.NonNull;

public class NodeHoverElement extends CustomHoverElement {

	protected NodeHoverElement(final @NonNull Node node, final HoverElementPosition position) {
		super((n, mx, my) -> {
			if (!node.hasUi()) {
				node.load(n.getUi());
			}
			node.render(mx, my);
		}, position);
	}

	public static @NonNull NodeHoverElement follow(final @NonNull Node node) {
		return new NodeHoverElement(node, HoverElementPosition.FOLLOW);
	}

	public static @NonNull NodeHoverElement fixed(final @NonNull Node node) {
		return new NodeHoverElement(node, HoverElementPosition.FIXED);
	}

	public static @NonNull NodeHoverElement relative(final @NonNull Node node) {
		return new NodeHoverElement(node, HoverElementPosition.RELATIVE);
	}

}