package dev.joid.demo.ui.toggle.node;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.structure.toggle.ToggleNode;
import lombok.NonNull;

public class DemoToggleNode extends ToggleNode<String, String> {

	protected DemoToggleNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull DemoToggleNode create(final double x, final double y, final double width, final double height) {
		return new DemoToggleNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.BLACK);
		DrawUtils.SHAPE.drawRect(super.getX() + (super.isToggle() ? super.dw(2) : 0), super.getY(), super.dw(2), super.getHeight(), Color.RED);
	}

}