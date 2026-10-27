package com.example.further;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.node.impl.control.choice.SwitchNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

// #region switch
public class KitSwitchNode<V> extends SwitchNode<V> {

	protected KitSwitchNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.option((value, index) -> RectNode
		.create(0, 0, 0, 0)
		.color(() -> this.isSelected(value) ? KitColors.INK : KitColors.SURFACE)
		.label(() -> Text.create(String.valueOf(value), TextInfo.create(ExampleFont.REGULAR, 20F).color(this.getInk(value)))));
	}

	public static <V> KitSwitchNode<V> create(final double x, final double y, final double width, final double height) {
		return new KitSwitchNode<>(x, y, width, height);
	}

	private boolean isSelected(final V value) {
		return value.equals(super.getValue());
	}

	private Color getInk(final V value) {
		return this.isSelected(value) ? KitColors.SURFACE : KitColors.INK;
	}

}
// #endregion