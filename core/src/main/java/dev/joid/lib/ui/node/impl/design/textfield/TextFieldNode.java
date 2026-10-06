package dev.joid.lib.ui.node.impl.design.textfield;

import lombok.NonNull;

public class TextFieldNode extends LineFieldNode<String> {

	protected TextFieldNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull TextFieldNode create(final double x, final double y, final double width) {
		return new TextFieldNode(x, y, width, 0D);
	}

	public static @NonNull TextFieldNode create(final double x, final double y, final double width, final double height) {
		return new TextFieldNode(x, y, width, height);
	}

	@Override
	public final @NonNull String getValue() {
		return super.getText();
	}

}