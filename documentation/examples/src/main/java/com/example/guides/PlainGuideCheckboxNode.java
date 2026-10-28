package com.example.guides;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.impl.control.checkbox.CheckboxNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class PlainGuideCheckboxNode extends CheckboxNode {

	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	protected PlainGuideCheckboxNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.box(RectNode.create(0, 0, 0, 0).color(PlainGuideCheckboxNode.ELEMENT));
		super.mark(RectNode.create(0, 0, width / 2D, height / 2D).color(PlainGuideCheckboxNode.ACCENT));
	}

	public static PlainGuideCheckboxNode create(final double x, final double y, final double width, final double height) {
		return new PlainGuideCheckboxNode(x, y, width, height);
	}

}