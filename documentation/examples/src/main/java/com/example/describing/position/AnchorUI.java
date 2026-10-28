package com.example.describing.position;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.utils.align.Align;

public class AnchorUI extends UI {

	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color MUTED   = new Color(139, 147, 163).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		RectNode
		.create(660, 340, 600, 400)
		.color(AnchorUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(AnchorUI.ELEMENT, 1.5F))
		.body(card -> {
			RectNode.create(299, 20, 2, 360).color(AnchorUI.MUTED).attach(card);
			// #region anchors
			RectNode.create(300, 60, 160, 80).color(AnchorUI.ELEMENT).anchorX(Align.START).attach(card);
			RectNode.create(300, 160, 160, 80).color(AnchorUI.ACCENT).anchorX(Align.CENTER).attach(card);
			RectNode.create(300, 260, 160, 80).color(AnchorUI.ELEMENT).anchorX(Align.END).attach(card);
			// #endregion
		})
		.attach(this);
	}

}