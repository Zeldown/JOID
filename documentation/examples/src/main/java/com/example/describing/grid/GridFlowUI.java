package com.example.describing.grid;

import com.example.capture.CaptureStyle;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.layout.GridNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class GridFlowUI extends UI {

	private final long start = BridgeHandler.CLOCK.get().currentTimeMillis();

	@Override
	public void init() {
		final GridNode grid = GridNode
		.create(20, 20, 200, 0)
		.margin(10D)
		.width(() -> this.getGridWidth())
		.body(node -> {
			for (int i = 0; i < 7; i++) {
				RectNode.create(0, 0, 60, 60).color(CaptureStyle.ACCENT).attach(node);
			}
		});

		RectNode
		.create(700, 400, 240, 0)
		.color(CaptureStyle.SURFACE)
		.width(() -> grid.getWidth() + 40D)
		.height(() -> grid.getHeight() + 40D)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(CaptureStyle.STROKE, 1.5F))
		.body(card -> grid.attach(card))
		.attach(this);
	}

	private double getGridWidth() {
		final double time = (BridgeHandler.CLOCK.get().currentTimeMillis() - this.start) / 4000D;
		return 200D + 140D * (1D - Math.cos(time * 2D * Math.PI));
	}

}