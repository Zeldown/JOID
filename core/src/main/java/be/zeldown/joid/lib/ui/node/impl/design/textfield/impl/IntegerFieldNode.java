package be.zeldown.joid.lib.ui.node.impl.design.textfield.impl;

import be.zeldown.joid.lib.ui.node.impl.design.textfield.TextFieldNode;
import lombok.NonNull;

@SuppressWarnings("unchecked")
public class IntegerFieldNode extends TextFieldNode {

    private int maxValue = Integer.MAX_VALUE;
    private int minValue = Integer.MIN_VALUE;

    protected IntegerFieldNode(final double x, final double y, final double width, final double height) {
        super(x, y, width, height);
        this.filter((oldValue, nextValue) -> {
            if (nextValue.isEmpty()) {
                return Integer.toString(this.minValue);
            }

            final String newValue = nextValue.replaceAll("[^0-9]", "");
			if (newValue.isEmpty()) {
				return Integer.toString(this.minValue);
			}

			try {
				final int value = Integer.parseInt(newValue);
				if (value > this.maxValue) {
					return Integer.toString(this.maxValue);
				} else if (value < this.minValue) {
					return Integer.toString(this.minValue);
				}

	            return newValue;
			} catch (final Exception silent) {}

			return Integer.toString(this.maxValue);
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
        return (T) this;
    }

	public final <T extends IntegerFieldNode> @NonNull T min(final int minValue) {
		this.minValue = minValue;
		return (T) this;
	}

	public final <T extends IntegerFieldNode> @NonNull T range(final int minValue, final int maxValue) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		return (T) this;
	}

	public final <T extends IntegerFieldNode> @NonNull T value(final int value) {
		this.text(Integer.toString(value));
		return (T) this;
	}

	public final int getValue() {
		return Integer.parseInt(this.getText());
	}

}