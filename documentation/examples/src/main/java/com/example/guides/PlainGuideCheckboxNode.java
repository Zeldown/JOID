package com.example.guides;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.impl.control.checkbox.CheckboxNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class PlainGuideCheckboxNode extends CheckboxNode {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	protected PlainGuideCheckboxNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.box(RectNode.create(0, 0, 0, 0).color(PlainGuideCheckboxNode.PLACEHOLDER));
		super.mark(RectNode.create(0, 0, width / 2D, height / 2D).color(PlainGuideCheckboxNode.INK));
	}

	public static PlainGuideCheckboxNode create(final double x, final double y, final double width, final double height) {
		return new PlainGuideCheckboxNode(x, y, width, height);
	}

}