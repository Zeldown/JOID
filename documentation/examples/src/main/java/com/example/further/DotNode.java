package com.example.further;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.Node;

// #region node
public class DotNode extends Node {

	private static final Color INK = new Color(153, 153, 153).lock();

	protected DotNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static DotNode create(final double x, final double y, final double size) {
		return new DotNode(x, y, size, size);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		final double centerX = super.getX() + super.dw(2D);
		final double centerY = super.getY() + super.dh(2D);
		DrawUtils.SHAPE.drawCircle(centerX, centerY, Color.WHITE, super.dw(2D));
		DrawUtils.SHAPE.drawCircle(centerX, centerY, DotNode.INK, super.dw(4D));
	}

}
// #endregion