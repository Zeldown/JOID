package dev.joid.demo.ui.selector.node;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.structure.selector.SelectorNode;
import lombok.NonNull;

public class DemoSelectorNode extends SelectorNode<Color> {

	protected DemoSelectorNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull DemoSelectorNode create(final double x, final double y, final double width, final double height) {
		return new DemoSelectorNode(x, y, width, height);
	}

	@Override
	protected @NonNull Node option(final @NonNull Color color) {
		return RectNode.create(0, 0, super.getDefaultWidth(), super.getDefaultHeight()).color(color);
	}

	@Override
	public void drawBackground(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.BLACK);
	}

}