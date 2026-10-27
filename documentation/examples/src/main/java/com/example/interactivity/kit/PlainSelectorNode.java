package com.example.interactivity.kit;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.node.impl.control.selector.SelectorNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class PlainSelectorNode<V> extends SelectorNode<V> {

	private static final Color INK   = new Color(153, 153, 153).lock();
	private static final Color HOVER = new Color(238, 238, 238).lock();

	protected PlainSelectorNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.field(RectNode.create(0, 0, 0, 0).color(PlainSelectorNode.INK));
		super.option(value -> RectNode
		.create(0, 0, 0, 0)
		.color(Color.WHITE)
		.hoveredColor(PlainSelectorNode.HOVER)
		.label(Text.create(value, TextInfo.create(ExampleFont.REGULAR, 22).color(PlainSelectorNode.INK))));
	}

	public static <V> PlainSelectorNode<V> create(final double x, final double y, final double width, final double height) {
		return new PlainSelectorNode<>(x, y, width, height);
	}

}