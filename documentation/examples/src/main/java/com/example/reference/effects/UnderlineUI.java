package com.example.reference.effects;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;

// #region underline
@UIData(background = true, backgroundColor = "#DDDDDD")
public class UnderlineUI extends UI {

	private static final Color BAR = new Color(85, 85, 85).lock();

	@Override
	public void init() {
		RectNode.create(80, 80, 240, 140)
		.color(Color.WHITE)
		.effect(UnderlineNodeEffect.create(UnderlineUI.BAR))
		.attach(this);
	}

	public static class UnderlineNodeEffect extends NodeEffect<Node> {

		private final Color color;

		protected UnderlineNodeEffect(final Color color) {
			this.color = color;
		}

		public static UnderlineNodeEffect create(final Color color) {
			return new UnderlineNodeEffect(color);
		}

		@Override
		public void draw(final Node node, final Runnable body) {
			body.run();
			DrawUtils.SHAPE.drawRect(node.getX(), node.getY() + node.getHeight() + 8D, node.getWidth(), 6D, this.color);
		}

	}

}
// #endregion