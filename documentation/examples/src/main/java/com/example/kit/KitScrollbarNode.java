package com.example.kit;

import dev.joid.lib.ui.node.impl.control.scrollbar.ScrollbarNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class KitScrollbarNode extends ScrollbarNode {

	protected KitScrollbarNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.track(RectNode.create(0, 0, 0, 0).color(KitColors.ELEMENT));
		super.thumb(RectNode.create(0, 0, 0, 0).color(KitColors.ACCENT));
	}

	public static KitScrollbarNode create(final double x, final double y, final double width, final double height) {
		return new KitScrollbarNode(x, y, width, height);
	}

}