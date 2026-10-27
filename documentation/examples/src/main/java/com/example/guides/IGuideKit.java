package com.example.guides;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.impl.control.checkbox.CheckboxNode;
import dev.joid.lib.ui.node.impl.control.slider.IntegerSliderNode;

public interface IGuideKit {

	public CheckboxNode checkbox(final double x, final double y);
	public IntegerSliderNode slider(final double x, final double y, final double width);

	public Color getSurface();

}