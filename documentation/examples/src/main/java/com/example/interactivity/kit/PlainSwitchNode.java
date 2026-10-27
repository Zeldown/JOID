package com.example.interactivity.kit;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.node.impl.control.choice.SwitchNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class PlainSwitchNode<V> extends SwitchNode<V> {

	private static final Color INK = new Color(153, 153, 153).lock();

	protected PlainSwitchNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.option((value, index) -> RectNode
		.create(0, 0, 0, 0)
		.color(() -> value.equals(super.getValue()) ? PlainSwitchNode.INK : Color.WHITE)
		.label(() -> Text.create(value, TextInfo.create(ExampleFont.REGULAR, 22).color(value.equals(super.getValue()) ? Color.WHITE : PlainSwitchNode.INK))));
	}

	public static <V> PlainSwitchNode<V> create(final double x, final double y, final double width, final double height) {
		return new PlainSwitchNode<>(x, y, width, height);
	}

}