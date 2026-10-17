package dev.joid.demo.ui.node.impl.control.checkbox;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.impl.control.checkbox.CheckboxNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import lombok.NonNull;

public class DemoCheckboxNode extends CheckboxNode {

	private static final Color INK = new Color(153, 153, 153).lock();

	protected DemoCheckboxNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.box(RectNode.create(0, 0, 0, 0).color(() -> Color.WHITE.withAlpha(super.isEnabled() ? 1F : 0.4F)));
		super.mark(RectNode.create(0, 0, width / 2D, height / 2D).color(() -> DemoCheckboxNode.INK.withAlpha(super.isEnabled() ? 1F : 0.4F)));
	}

	public static @NonNull DemoCheckboxNode create(final double x, final double y, final double width, final double height) {
		return new DemoCheckboxNode(x, y, width, height);
	}

}