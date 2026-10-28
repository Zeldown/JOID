package com.example.guides;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.impl.control.checkbox.CheckboxNode;
import dev.joid.lib.ui.node.impl.control.slider.IntegerSliderNode;

public class PlainGuideKit implements IGuideKit {

	private static final Color SURFACE = new Color(23, 26, 33).lock();

	@Override
	public CheckboxNode checkbox(final double x, final double y) {
		return PlainGuideCheckboxNode.create(x, y, 50, 50);
	}

	@Override
	public IntegerSliderNode slider(final double x, final double y, final double width) {
		return PlainGuideSliderNode.create(x, y, width, 50);
	}

	@Override
	public Color getSurface() {
		return PlainGuideKit.SURFACE;
	}

}