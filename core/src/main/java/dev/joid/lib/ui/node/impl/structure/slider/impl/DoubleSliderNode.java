package dev.joid.lib.ui.node.impl.structure.slider.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import dev.joid.lib.ui.node.impl.structure.slider.SliderNode;
import lombok.NonNull;

public abstract class DoubleSliderNode extends SliderNode<Double> {

	protected DoubleSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public final <T extends DoubleSliderNode> @NonNull T range(final double min, final double max, final double step) {
		final List<Double> values = new ArrayList<>();
		for (BigDecimal i = BigDecimal.valueOf(min); i.doubleValue() <= max; i = i.add(BigDecimal.valueOf(step))) {
			values.add(i.doubleValue());
		}
		return super.values(values.toArray(new Double[0]));
	}

}