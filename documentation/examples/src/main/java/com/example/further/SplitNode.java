package com.example.further;

import java.util.ArrayList;
import java.util.List;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.property.PositionProperty;

public class SplitNode extends Node {

	protected SplitNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static SplitNode create(final double x, final double y, final double width, final double height) {
		return new SplitNode(x, y, width, height);
	}

	// #region layout
	@Override
	public void layout() {
		final List<Node> panels = new ArrayList<>();
		for (final Node child : super.getChildren()) {
			if (child.isVisibleProperty() && child.getPosition() != PositionProperty.ABSOLUTE) {
				panels.add(child);
			}
		}

		final double width = (super.getWidth() - 10D * (panels.size() - 1)) / panels.size();
		for (int i = 0; i < panels.size(); i++) {
			panels.get(i).layoutPosition((width + 10D) * i, 0D).fit(width, super.getHeight());
		}
	}
	// #endregion

}