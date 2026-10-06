package dev.joid.lib.ui.node.impl.design.textfield.impl;

import dev.joid.lib.ui.node.impl.design.textfield.LineFieldNode;
import lombok.NonNull;

@SuppressWarnings("unchecked")
public class IntegerFieldNode extends LineFieldNode<Integer> {

	private int maxValue = Integer.MAX_VALUE;
	private int minValue = Integer.MIN_VALUE;

	protected IntegerFieldNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.filter((oldValue, nextValue) -> {
			final boolean negative = nextValue.startsWith("-");
			final String digits = nextValue.replaceAll("[^0-9]", "");
			if (digits.isEmpty()) {
				return negative && this.minValue < 0 ? "-" : "";
			}

			final String newValue = negative ? "-" + digits : digits;

			try {
				final int value = Integer.parseInt(newValue);
				if (value > this.maxValue) {
					return Integer.toString(this.maxValue);
				} else if (value < this.minValue) {
					return Integer.toString(this.minValue);
				}

				return newValue;
			} catch (final Exception silent) {}

			return Integer.toString(negative ? this.minValue : this.maxValue);
		});
		super.<IntegerFieldNode>onFocus(field -> {
			if (!field.isFocused() && field.getText().replace("-", "").isEmpty()) {
				field.value(field.getValue());
			}
		});
	}

	public static @NonNull IntegerFieldNode create(final double x, final double y, final double width) {
		return new IntegerFieldNode(x, y, width, 0);
	}

	public static @NonNull IntegerFieldNode create(final double x, final double y, final double width, final double height) {
		return new IntegerFieldNode(x, y, width, height);
	}

	public final <T extends IntegerFieldNode> @NonNull T max(final int maxValue) {
		this.maxValue = maxValue;
		super.text(super.getText());
		return (T) this;
	}

	public final <T extends IntegerFieldNode> @NonNull T min(final int minValue) {
		this.minValue = minValue;
		super.text(super.getText());
		return (T) this;
	}

	public final <T extends IntegerFieldNode> @NonNull T range(final int minValue, final int maxValue) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		super.text(super.getText());
		return (T) this;
	}

	public final <T extends IntegerFieldNode> @NonNull T value(final int value) {
		super.text(Integer.toString(value));
		return (T) this;
	}

	@Override
	public final @NonNull Integer getValue() {
		try {
			return Integer.parseInt(super.getText());
		} catch (final NumberFormatException silent) {
			return (int) (((long) this.minValue + this.maxValue) / 2L);
		}
	}

}