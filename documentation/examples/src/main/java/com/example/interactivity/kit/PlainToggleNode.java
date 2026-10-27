package com.example.interactivity.kit;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.impl.control.toggle.ToggleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class PlainToggleNode<F, S> extends ToggleNode<F, S> {

	private static final Color INK = new Color(153, 153, 153).lock();

	protected PlainToggleNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.track(RectNode.create(0, 0, 0, 0).color(() -> Color.WHITE.withAlpha(super.isEnabled() ? 1F : 0.4F)));
		super.knob(RectNode.create(0, 0, 0, 0).color(() -> PlainToggleNode.INK.withAlpha(super.isEnabled() ? 1F : 0.4F)));
	}

	public static <F, S> PlainToggleNode<F, S> create(final double x, final double y, final double width, final double height) {
		return new PlainToggleNode<>(x, y, width, height);
	}

}