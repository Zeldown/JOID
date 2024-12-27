package be.zeldown.joid.demo.ui.selector.node;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.ui.node.impl.structure.selector.SelectorNode;
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