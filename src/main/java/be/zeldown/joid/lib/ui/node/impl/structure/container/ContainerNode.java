package be.zeldown.joid.lib.ui.node.impl.structure.container;

import be.zeldown.joid.lib.ui.node.Node;
import lombok.NonNull;

public class ContainerNode extends Node {

	protected ContainerNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull ContainerNode create(final double x, final double y, final double width, final double height) {
		return new ContainerNode(x, y, width, height);
	}

	public static @NonNull ContainerNode create(final @NonNull Node parent) {
		return new ContainerNode(0, 0, parent.getWidth(), parent.getHeight()).attach(parent);
	}

	@Override
	public final void draw(final double mouseX, final double mouseY) {}

	@Override
	public final void drawSkeleton(final double mouseX, final double mouseY) {}

}