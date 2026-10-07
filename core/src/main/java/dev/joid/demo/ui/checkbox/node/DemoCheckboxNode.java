package dev.joid.demo.ui.checkbox.node;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.structure.checkbox.CheckboxNode;
import lombok.NonNull;

public class DemoCheckboxNode extends CheckboxNode {

	private static final Color INK = new Color(153, 153, 153);

	protected DemoCheckboxNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull DemoCheckboxNode create(final double x, final double y, final double width, final double height) {
		return new DemoCheckboxNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.WHITE);
		if (super.isChecked()) {
			DrawUtils.SHAPE.drawRect(super.getX() + super.dw(2) - super.dw(4), super.getY() + super.dh(2) - super.dh(4), super.dw(2), super.dh(2), DemoCheckboxNode.INK);
		}
	}

}