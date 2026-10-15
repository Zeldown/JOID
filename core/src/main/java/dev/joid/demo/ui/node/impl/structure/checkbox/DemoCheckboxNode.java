package dev.joid.demo.ui.node.impl.structure.checkbox;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.structure.checkbox.CheckboxNode;
import dev.joid.lib.ui.node.impl.structure.container.ContainerNode;
import lombok.NonNull;

public class DemoCheckboxNode extends CheckboxNode {

	private static final Color INK = new Color(153, 153, 153).lock();

	protected DemoCheckboxNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.box(RectNode.create(0, 0, 0, 0).color(() -> Color.WHITE.withAlpha(super.isEnabled() ? 1F : 0.4F)));
		super.mark(ContainerNode.create(0, 0, 0, 0).layer((node, mouseX, mouseY) -> DrawUtils.SHAPE.drawRect(node.dw(4), node.dh(4), node.dw(2), node.dh(2), DemoCheckboxNode.INK.withAlpha(super.isEnabled() ? 1F : 0.4F))));
	}

	public static @NonNull DemoCheckboxNode create(final double x, final double y, final double width, final double height) {
		return new DemoCheckboxNode(x, y, width, height);
	}

}