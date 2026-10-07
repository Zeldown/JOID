package dev.joid.demo.ui.textfield.node;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.design.textfield.TextFieldNode;
import lombok.NonNull;

public class DemoTextFieldNode extends TextFieldNode {

	private static final Color INK = new Color(153, 153, 153);

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
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.WHITE.copyAlpha(super.isEnabled() ? 1F : 0.4F));
		if (super.isFocused()) {
			DrawUtils.SHAPE.drawFilledBorder(super.getX(), super.getY(), super.getX() + super.getWidth(), super.getY() + super.getHeight(), DemoTextFieldNode.INK, 2D);
		}

		super.draw(mouseX, mouseY);
	}

}