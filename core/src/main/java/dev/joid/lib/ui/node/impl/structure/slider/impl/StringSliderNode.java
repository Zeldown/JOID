package dev.joid.lib.ui.node.impl.structure.slider.impl;

import dev.joid.lib.ui.node.impl.structure.slider.SliderNode;
import lombok.NonNull;

public abstract class StringSliderNode extends SliderNode<String> {

	protected StringSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public final <T extends StringSliderNode> @NonNull T values(final @NonNull Enum<?> @NonNull... values) {
		final String[] names = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			names[i] = values[i].name();
		}
		return super.values(names);
	}

	public final <T extends StringSliderNode> @NonNull T value(final @NonNull Enum<?> value) {
		return super.value(value.name());
	}

}