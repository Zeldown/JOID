package be.zeldown.joid.lib.ui.node.hover.impl;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.hover.HoverElement;
import lombok.NonNull;

public class NodeHoverElement extends CustomHoverElement {

	protected NodeHoverElement(final @NonNull Node node, final HoverElementPosition position) {
		super(new HoverElement() {

			@Override
			public void render(final @NonNull Node parentNode, final double mouseX, final double mouseY) {
				if (!node.hasUi()) {
					node.load(parentNode.getUi());
				}

				GL11.glTranslated(-node.getX(), -node.getY(), 0);
				node.render(mouseX, mouseY);
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