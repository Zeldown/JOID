package com.example.kit;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.node.impl.control.selector.SelectorNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class KitSelectorNode<V> extends SelectorNode<V> {

	private static final Color HOVER = new Color(238, 238, 238).lock();

	protected KitSelectorNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.field(RectNode.create(0, 0, 0, 0).color(KitColors.INK));
		super.option(value -> RectNode
		.create(0, 0, 0, 0)
		.color(KitColors.SURFACE)
		.hoveredColor(KitSelectorNode.HOVER)
		.label(Text.create(value, TextInfo.create(ExampleFont.REGULAR, 22F).color(KitColors.INK))));
	}

	public static <V> KitSelectorNode<V> create(final double x, final double y, final double width, final double height) {
		return new KitSelectorNode<>(x, y, width, height);
	}

}