package dev.joid.demo.ui.node.impl.design.textfield;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.textfield.TextFieldNode;
import lombok.NonNull;

public class DemoTextFieldNode extends TextFieldNode {

	private static final Color INK = new Color(153, 153, 153).lock();

	protected DemoTextFieldNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.background(RectNode.create(0, 0, 0, 0).color(() -> Color.WHITE.withAlpha(super.isEnabled() ? 1F : 0.4F)).layer((node, mouseX, mouseY) -> {
			if (super.isFocused()) {
				DrawUtils.SHAPE.drawBorder(0D, 0D, node.getWidth(), node.getHeight(), DemoTextFieldNode.INK, 2D);
			}
		}));
	}

	public static @NonNull DemoTextFieldNode create(final double x, final double y, final double width) {
		return new DemoTextFieldNode(x, y, width, 0);
	}

	public static @NonNull DemoTextFieldNode create(final double x, final double y, final double width, final double height) {
		return new DemoTextFieldNode(x, y, width, height);
	}

}