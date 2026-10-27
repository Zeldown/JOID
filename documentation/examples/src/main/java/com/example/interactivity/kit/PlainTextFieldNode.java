package com.example.interactivity.kit;

import dev.joid.lib.color.Color;
import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.impl.control.field.TextFieldNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class PlainTextFieldNode extends TextFieldNode {

	private static final Color INK = new Color(153, 153, 153).lock();

	protected PlainTextFieldNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.marginHorizontal(12D);
		super.background(RectNode.create(0, 0, 0, 0).color(Color.WHITE).effect(BorderNodeEffect.create(PlainTextFieldNode.INK, 2F).mode(BorderMode.IN).width(() -> super.isFocused() ? 2F : 0F)));
	}

	public static PlainTextFieldNode create(final double x, final double y, final double width, final double height) {
		return new PlainTextFieldNode(x, y, width, height);
	}

}