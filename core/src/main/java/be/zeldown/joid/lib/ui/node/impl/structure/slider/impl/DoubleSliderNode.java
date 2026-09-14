package be.zeldown.joid.lib.ui.node.impl.structure.slider.impl;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import be.zeldown.joid.lib.ui.node.impl.structure.slider.SliderNode;
import lombok.NonNull;

public abstract class DoubleSliderNode extends SliderNode<Double> {

	protected DoubleSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}
	
	public final <T extends DoubleSliderNode> @NonNull T values(final double min, final double max, final double step, final double value) {
		final Set<Double> values = new LinkedHashSet<>();
		for (double i = min; i <= max; i += step) {
			values.add(i);
		}
		return this.valueSet(values, value);
	}
	
	public final <T extends DoubleSliderNode> @NonNull T values(final double value, final Double... values) {
		return this.valueSet(new LinkedHashSet<>(Arrays.asList(values)), value);
	}

}