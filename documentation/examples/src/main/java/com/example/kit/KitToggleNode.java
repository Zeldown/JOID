package com.example.kit;

import dev.joid.lib.ui.node.impl.control.toggle.ToggleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class KitToggleNode<F, S> extends ToggleNode<F, S> {

	protected KitToggleNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.track(RectNode.create(0, 0, 0, 0).color(() -> KitColors.ELEMENT.withAlpha(super.isEnabled() ? 1F : 0.4F)));
		super.knob(RectNode.create(0, 0, 0, 0).color(() -> KitColors.ACCENT.withAlpha(super.isEnabled() ? 1F : 0.4F)));
	}

	public static <F, S> KitToggleNode<F, S> create(final double x, final double y, final double width, final double height) {
		return new KitToggleNode<>(x, y, width, height);
	}

}