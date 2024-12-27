package be.zeldown.joid.lib.ui.node.impl.structure.slider.impl;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import be.zeldown.joid.lib.ui.node.impl.structure.slider.SliderNode;
import lombok.NonNull;

public abstract class StringSliderNode extends SliderNode<String> {

	protected StringSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}
	
	public final <T extends StringSliderNode> @NonNull T values(final String value, final String... values) {
		return this.valueSet(new LinkedHashSet<>(Arrays.asList(values)), value);
	}
	
	public final <T extends StringSliderNode> @NonNull T values(final Enum<?> value, final Enum<?>... values) {
		final Set<String> mappedValues = new LinkedHashSet<>();
		for (Enum<?> v : values) {
			mappedValues.add(v.name());
		}
		return this.valueSet(mappedValues, value.name());
	}

}