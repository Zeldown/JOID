package com.example.describing.position;

import com.example.capture.CaptureStyle;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class FollowGrowUI extends UI {

	private final long start = BridgeHandler.CLOCK.get().currentTimeMillis();

	@Override
	public void init() {
		RectNode
		.create(560, 330, 480, 420)
		.color(CaptureStyle.SURFACE)
		.width(() -> this.getPanelWidth())
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(CaptureStyle.STROKE, 1.5F))
		.body(panel -> {
			RectNode.create(20, 20, panel.w() - 40D, 60).color(CaptureStyle.MUTED).attach(panel);
			RectNode.create(20, 100, 0, 60).color(CaptureStyle.ACCENT).width(() -> panel.getWidth() - 40D).attach(panel);
			RectNode.create(20, 180, 0, 0).color(CaptureStyle.ACCENT).width(() -> (panel.getWidth() - 40D) / 4D).aspectRatio(1D).attach(panel);
		})
		.attach(this);
	}

	private double getPanelWidth() {
		final double time = (BridgeHandler.CLOCK.get().currentTimeMillis() - this.start) / 4000D;
		return 480D + 160D * (1D - Math.cos(time * 2D * Math.PI));
	}

}