package dev.joid.lib.ui.node.impl.control.slider;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

public abstract class IntegerSliderNode extends SliderNode<Integer> {

	protected IntegerSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public final <T extends IntegerSliderNode> @NonNull T range(final int min, final int max) {
		return this.range(min, max, 1);
	}

	public final <T extends IntegerSliderNode> @NonNull T range(final int min, final int max, final int step) {
		if (step <= 0) {
			throw new IllegalArgumentException("The step " + step + " of the slider range must be positive, pass a step of 1 or more");
		}

		final List<Integer> values = new ArrayList<>();
		for (long i = min; i <= max; i += step) {
			values.add((int) i);
		}
		return super.values(values.toArray(new Integer[0]));
	}

}