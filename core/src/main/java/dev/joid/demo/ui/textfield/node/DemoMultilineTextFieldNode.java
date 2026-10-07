package dev.joid.demo.ui.textfield.node;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.design.textfield.MultilineTextFieldNode;
import lombok.NonNull;

public class DemoMultilineTextFieldNode extends MultilineTextFieldNode {

	private static final Color INK = new Color(153, 153, 153);

	protected DemoMultilineTextFieldNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull DemoMultilineTextFieldNode create(final double x, final double y, final double width, final double height) {
		return new DemoMultilineTextFieldNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.WHITE.copyAlpha(super.isEnabled() ? 1F : 0.4F));
		if (super.isFocused()) {
			DrawUtils.SHAPE.drawFilledBorder(super.getX(), super.getY(), super.getX() + super.getWidth(), super.getY() + super.getHeight(), DemoMultilineTextFieldNode.INK, 2D);
		}

		super.draw(mouseX, mouseY);
	}

}