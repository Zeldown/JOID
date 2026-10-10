package dev.joid.lib.ui.node.impl.structure.slider.impl;

import java.util.ArrayList;
import java.util.List;

import dev.joid.lib.ui.node.impl.structure.slider.SliderNode;
import lombok.NonNull;

public abstract class IntegerSliderNode extends SliderNode<Integer> {

	protected IntegerSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public final <T extends IntegerSliderNode> @NonNull T range(final int min, final int max) {
		final List<Integer> values = new ArrayList<>();
		for (int i = min; i <= max; i++) {
			values.add(i);
		}
		return super.values(values.toArray(new Integer[0]));
	}

}