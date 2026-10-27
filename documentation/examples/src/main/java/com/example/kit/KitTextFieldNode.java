package com.example.kit;

import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.impl.control.field.TextFieldNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class KitTextFieldNode extends TextFieldNode {

	protected KitTextFieldNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.marginHorizontal(12D);
		super.background(RectNode.create(0, 0, 0, 0).color(KitColors.SURFACE).effect(BorderNodeEffect.create(KitColors.INK, 2F).mode(BorderMode.IN).width(() -> super.isFocused() ? 2F : 0F)));
	}

	public static KitTextFieldNode create(final double x, final double y, final double width, final double height) {
		return new KitTextFieldNode(x, y, width, height);
	}

}