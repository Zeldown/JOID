package com.example.interactivity.kit;

import dev.joid.lib.color.Color;
import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.impl.control.field.IntegerFieldNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class PlainIntegerFieldNode extends IntegerFieldNode {

	private static final Color INK = new Color(153, 153, 153).lock();

	protected PlainIntegerFieldNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.marginHorizontal(12D);
		super.background(RectNode.create(0, 0, 0, 0).color(Color.WHITE).effect(BorderNodeEffect.create(PlainIntegerFieldNode.INK, 2F).mode(BorderMode.IN).width(() -> super.isFocused() ? 2F : 0F)));
	}

	public static PlainIntegerFieldNode create(final double x, final double y, final double width, final double height) {
		return new PlainIntegerFieldNode(x, y, width, height);
	}

}