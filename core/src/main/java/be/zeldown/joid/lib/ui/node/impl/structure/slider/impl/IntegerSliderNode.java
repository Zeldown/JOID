package be.zeldown.joid.lib.ui.node.impl.structure.slider.impl;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import be.zeldown.joid.lib.ui.node.impl.structure.slider.SliderNode;
import lombok.NonNull;

public abstract class IntegerSliderNode extends SliderNode<Integer> {

	protected IntegerSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}
	
	public final <T extends IntegerSliderNode> @NonNull T values(final int min, final int max, final int value) {
		final Set<Integer> values = new LinkedHashSet<>();
		for (int i = min; i <= max; i++) {
			values.add(i);
		}
		return this.valueSet(values, value);
	}
	
	public final <T extends IntegerSliderNode> @NonNull T values(final int value, final Integer... values) {
		return this.valueSet(new LinkedHashSet<>(Arrays.asList(values)), value);
	}

}