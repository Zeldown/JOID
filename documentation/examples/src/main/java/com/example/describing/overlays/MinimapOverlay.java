package com.example.describing.overlays;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.core.data.UIDataOverlay;
import dev.joid.lib.ui.core.data.UIDataOverlayInteraction;
import dev.joid.lib.ui.core.data.UIDataOverlayRender;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.utils.align.Align;

// #region class
@UIData(anchorX = Align.END, anchorY = Align.START)
@UIDataOverlay(
	active = true,
	render = @UIDataOverlayRender(aboveScreens = true),
	interaction = @UIDataOverlayInteraction(active = true)
)
public class MinimapOverlay extends UI {

	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	private double zoom = 1D;

	@Override
	public void init() {
		RectNode
		.create(1560, 40, 320, 320)
		.color(MinimapOverlay.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(MinimapOverlay.ELEMENT, 1.5F))
		.onClick(event -> this.zoom = this.zoom == 1D ? 2D : 1D)
		.body(map -> {
			CircleNode
			.create(160, 160, 40)
			.color(MinimapOverlay.ACCENT)
			.width(() -> 40D * this.zoom)
			.height(() -> 40D * this.zoom)
			.anchor(Align.CENTER)
			.attach(map);
		})
		.attach(this);
	}

}
// #endregion