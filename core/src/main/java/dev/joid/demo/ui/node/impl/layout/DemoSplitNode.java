package dev.joid.demo.ui.node.impl.layout;

import java.util.List;

import dev.joid.lib.ui.node.Node;
import lombok.NonNull;

public class DemoSplitNode extends Node {

	protected DemoSplitNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull DemoSplitNode create(final double x, final double y, final double width, final double height) {
		return new DemoSplitNode(x, y, width, height);
	}

	@Override
	public void layout() {
		final List<Node> children = super.getChildren();
		final double width = (super.getWidth() - 10D * (children.size() - 1)) / children.size();
		for (int i = 0; i < children.size(); i++) {
			children.get(i).layoutPosition((width + 10D) * i, 0D).fit(width, super.getHeight());
		}
	}

}