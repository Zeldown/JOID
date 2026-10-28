package com.example.describing.position;

import com.example.capture.CaptureStyle;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.utils.align.Align;

public class AnchorGrowUI extends UI {

	private final long start = BridgeHandler.CLOCK.get().currentTimeMillis();

	@Override
	public void init() {
		RectNode
		.create(660, 340, 600, 400)
		.color(CaptureStyle.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(CaptureStyle.STROKE, 1.5F))
		.body(card -> {
			RectNode.create(299, 20, 2, 360).color(CaptureStyle.MUTED).attach(card);
			RectNode.create(300, 60, 160, 80).color(CaptureStyle.STROKE).anchorX(Align.START).width(() -> this.getBarWidth()).attach(card);
			RectNode.create(300, 160, 160, 80).color(CaptureStyle.ACCENT).anchorX(Align.CENTER).width(() -> this.getBarWidth()).attach(card);
			RectNode.create(300, 260, 160, 80).color(CaptureStyle.STROKE).anchorX(Align.END).width(() -> this.getBarWidth()).attach(card);
		})
		.attach(this);
	}

	private double getBarWidth() {
		final double time = (BridgeHandler.CLOCK.get().currentTimeMillis() - this.start) / 2000D;
		return 160D + 50D * (1D - Math.cos(time * 2D * Math.PI));
	}

}