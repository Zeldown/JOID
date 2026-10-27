package com.example.reference.effects;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;

// #region badge
@UIData(background = true, backgroundColor = "#DDDDDD")
public class BadgeUI extends UI {

	private static final Color DOT = new Color(85, 85, 85).lock();

	@Override
	public void init() {
		RectNode.create(80, 80, 240, 140)
		.color(Color.WHITE)
		.effect(RoundedNodeEffect.create(16F))
		.effect(BadgeNodeEffect.create(BadgeUI.DOT))
		.attach(this);
	}

	public static class BadgeNodeEffect extends NodeEffect<Node> {

		private final Color color;

		protected BadgeNodeEffect(final Color color) {
			this.color = color;
		}

		public static BadgeNodeEffect create(final Color color) {
			return new BadgeNodeEffect(color);
		}

		@Override
		public void draw(final Node node, final Runnable body) {
			body.run();
			DrawUtils.SHAPE.drawCircle(node.getX() + node.getWidth() - 8D, node.getY() + 8D, this.color, 14D);
		}

	}

}
// #endregion