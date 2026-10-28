package com.example.interactivity.fields;

import dev.joid.lib.color.Color;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.node.impl.control.field.LineFieldNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

// #region field
public class HexColorFieldNode extends LineFieldNode<Color> {

	private static final Color ELEMENT = new Color(38, 43, 53).lock();

	protected HexColorFieldNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.marginHorizontal(12D);
		super.background(RectNode.create(0, 0, 0, 0).color(HexColorFieldNode.ELEMENT));
	}

	public static HexColorFieldNode create(final double x, final double y, final double width, final double height) {
		return new HexColorFieldNode(x, y, width, height);
	}

	public final <T extends HexColorFieldNode> T value(final Color value) {
		return super.startValue(value);
	}

	public final <T extends HexColorFieldNode> T value(final Signal<Color> value) {
		return super.bindValue(value);
	}

	@Override
	protected Color parse(final String text) {
		return text.matches("#[0-9A-Fa-f]{6}") ? Color.decode(text) : null;
	}

	@Override
	protected String format(final Color value) {
		return String.format("#%02X%02X%02X", value.getRed(), value.getGreen(), value.getBlue());
	}

	@Override
	protected boolean accepts(final String text) {
		return text.matches("#?[0-9A-Fa-f]{0,6}");
	}

}
// #endregion