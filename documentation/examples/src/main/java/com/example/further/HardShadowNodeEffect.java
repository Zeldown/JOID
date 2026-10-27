package com.example.further;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;

// #region effect
public class HardShadowNodeEffect extends NodeEffect<Node> {

	private final Color  color;
	private final double offset;

	// #region priority
	protected HardShadowNodeEffect(final Color color, final double offset) {
		super(-60);
		this.color = color;
		this.offset = offset;
	}
	// #endregion

	public static HardShadowNodeEffect create(final Color color, final double offset) {
		return new HardShadowNodeEffect(color, offset);
	}

	@Override
	public void draw(final Node node, final Runnable body) {
		DrawUtils.SHAPE.drawRect(node.getX() + this.offset, node.getY() + this.offset, node.w(), node.h(), this.color);
		body.run();
	}

}
// #endregion