package dev.joid.lib.ui.node.impl.control.slider;

import java.util.Arrays;

import lombok.NonNull;

public abstract class StringSliderNode extends SliderNode<String> {

	protected StringSliderNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public final <T extends StringSliderNode> @NonNull T values(final @NonNull Enum<?> @NonNull... values) {
		return super.values(Arrays.stream(values).map(Enum::name).toArray(String[]::new));
	}

	public final <T extends StringSliderNode> @NonNull T value(final @NonNull Enum<?> value) {
		return super.value(value.name());
	}

}