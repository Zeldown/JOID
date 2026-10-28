package com.example.reference.effects;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;

// #region underline
public class UnderlineUI extends UI {

	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		RectNode.create(80, 80, 240, 140)
		.color(UnderlineUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(UnderlineUI.ELEMENT, 1.5F))
		.effect(UnderlineNodeEffect.create(UnderlineUI.ACCENT))
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