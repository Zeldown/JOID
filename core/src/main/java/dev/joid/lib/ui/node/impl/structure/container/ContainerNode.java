package dev.joid.lib.ui.node.impl.structure.container;

import dev.joid.lib.ui.node.Node;
import lombok.NonNull;

public class ContainerNode extends Node {

	protected ContainerNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull ContainerNode create(final @NonNull Node parent) {
		return new ContainerNode(0, 0, parent.getWidth(), parent.getHeight()).attach(parent);
	}

	public static @NonNull ContainerNode create(final double x, final double y, final double width, final double height) {
		return new ContainerNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {}

	@Override
	public void drawSkeleton(final double mouseX, final double mouseY) {}

}