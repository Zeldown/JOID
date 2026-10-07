package dev.joid.lib.ui.node.impl.design.textfield;

import java.util.function.UnaryOperator;

import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class TextFieldNode extends LineFieldNode<String> {

	private UnaryOperator<String> formatter;

	protected TextFieldNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.formatter = UnaryOperator.identity();
	}

	public static @NonNull TextFieldNode create(final double x, final double y, final double width) {
		return new TextFieldNode(x, y, width, 0D);
	}

	public static @NonNull TextFieldNode create(final double x, final double y, final double width, final double height) {
		return new TextFieldNode(x, y, width, height);
	}

	@Override
	protected final String parse(final @NonNull String text) {
		return this.formatter.apply(text);
	}

	@Override
	protected final @NonNull String format(final @NonNull String value) {
		return value;
	}

	public final <T extends TextFieldNode> @NonNull T format(final @NonNull UnaryOperator<@NonNull String> formatter) {
		this.formatter = formatter;
		return (T) this;
	}

}