package dev.joid.lib.ui.node.impl.structure.slider.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import dev.joid.lib.ui.node.impl.structure.slider.SliderNode;
import lombok.NonNull;

public abstract class DoubleSliderNode extends SliderNode<Double> {

	protected DoubleSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public final <T extends DoubleSliderNode> @NonNull T values(final double value, final Double... values) {
		return this.valueSet(new LinkedHashSet<>(Arrays.asList(values)), value);
	}

	public final <T extends DoubleSliderNode> @NonNull T values(final double min, final double max, final double step, final double value) {
		final Set<Double> values = new LinkedHashSet<>();
		for (BigDecimal i = BigDecimal.valueOf(min); i.doubleValue() <= max; i = i.add(BigDecimal.valueOf(step))) {
			values.add(i.doubleValue());
		}
		return this.valueSet(values, value);
	}

}