package dev.joid.demo.ui.selector.node;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.structure.selector.SelectorNode;
import lombok.NonNull;

public class DemoSelectorNode extends SelectorNode {

	protected DemoSelectorNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull DemoSelectorNode create(final double x, final double y, final double width, final double height) {
		return new DemoSelectorNode(x, y, width, height);
	}

	@Override
	public void drawBackground(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.BLACK);
	}

}