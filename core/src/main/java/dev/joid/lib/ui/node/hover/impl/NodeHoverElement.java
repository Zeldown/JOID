package dev.joid.lib.ui.node.hover.impl;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.hover.IHoverElement;
import lombok.NonNull;

public class NodeHoverElement extends PositionedHoverElement {

	protected NodeHoverElement(final @NonNull Node node, final HoverElementPosition position) {
		super(new IHoverElement() {

			@Override
			public void render(final @NonNull Node parentNode, final double mouseX, final double mouseY) {
				if (!node.isAttached()) {
					node.load(parentNode.getUi());
				}

				node.fireUpdate();
				BridgeHandler.RENDER.get().getModelView().translate(-node.getX(), -node.getY(), 0D);
				node.render(mouseX, mouseY);
			}

			@Override
			public void detach() {
				node.fireDetach();
			}

			@Override
			public double getX() {
				return node.getX();
			}

			@Override
			public double getY() {
				return node.getY();
			}

			@Override
			public double getWidth() {
				return node.getWidth();
			}

			@Override
			public double getHeight() {
				return node.getHeight();
			}

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