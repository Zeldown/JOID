package be.zeldown.joid.demo.ui.textfield.node;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.ui.node.impl.design.textfield.TextFieldNode;
import lombok.NonNull;

public class DemoTextFieldNode extends TextFieldNode {

	protected DemoTextFieldNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull DemoTextFieldNode create(final double x, final double y, final double width) {
		return new DemoTextFieldNode(x, y, width, 0);
	}

	public static @NonNull DemoTextFieldNode create(final double x, final double y, final double width, final double height) {
		return new DemoTextFieldNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.BLACK);
		if (super.isFocused()) {
			DrawUtils.SHAPE.drawFilledBorder(super.getX(), super.getY(), super.getX() + super.getWidth(), super.getY() + super.getHeight(), Color.WHITE);
		}

		super.draw(mouseX, mouseY);
	}

}
