package com.example.interactivity.kit;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.impl.control.checkbox.CheckboxNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class PlainCheckboxNode extends CheckboxNode {

	private static final Color INK = new Color(153, 153, 153).lock();

	protected PlainCheckboxNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.box(RectNode.create(0, 0, 0, 0).color(() -> Color.WHITE.withAlpha(super.isEnabled() ? 1F : 0.4F)));
		super.mark(RectNode.create(0, 0, width / 2D, height / 2D).color(() -> PlainCheckboxNode.INK.withAlpha(super.isEnabled() ? 1F : 0.4F)));
	}

	public static PlainCheckboxNode create(final double x, final double y, final double width, final double height) {
		return new PlainCheckboxNode(x, y, width, height);
	}

}