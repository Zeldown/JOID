package dev.joid.lib.ui.node.impl.layout;

import dev.joid.lib.ui.node.Node;
import lombok.NonNull;

public class ContainerNode extends Node {

	protected ContainerNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull ContainerNode create(final double x, final double y, final double width, final double height) {
		return new ContainerNode(x, y, width, height);
	}

	@Override
	public void drawSkeleton(final double mouseX, final double mouseY) {}

}