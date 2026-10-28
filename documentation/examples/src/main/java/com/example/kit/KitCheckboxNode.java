package com.example.kit;

import dev.joid.lib.ui.node.impl.control.checkbox.CheckboxNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

// #region checkbox
public class KitCheckboxNode extends CheckboxNode {

	protected KitCheckboxNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.box(RectNode.create(0, 0, 0, 0).color(KitColors.ELEMENT));
		super.mark(RectNode.create(0, 0, width / 2D, height / 2D).color(KitColors.ACCENT));
	}

	public static KitCheckboxNode create(final double x, final double y, final double size) {
		return new KitCheckboxNode(x, y, size, size);
	}

}
// #endregion