package com.example.guides;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.impl.control.checkbox.CheckboxNode;
import dev.joid.lib.ui.node.impl.control.slider.IntegerSliderNode;

// #region kit
public class RoundGuideKit implements IGuideKit {

	private static final Color SURFACE = new Color(54, 61, 77).lock();

	@Override
	public CheckboxNode checkbox(final double x, final double y) {
		return RoundGuideCheckboxNode.create(x, y, 50, 50);
	}

	@Override
	public IntegerSliderNode slider(final double x, final double y, final double width) {
		return RoundGuideSliderNode.create(x, y, width, 30);
	}

	@Override
	public Color getSurface() {
		return RoundGuideKit.SURFACE;
	}

}
// #endregion