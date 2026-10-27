package com.example.capture;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.core.data.UIDataOverlay;
import dev.joid.lib.ui.core.data.UIDataOverlayRender;
import dev.joid.lib.ui.core.view.UIView;
import dev.joid.lib.ui.node.impl.shape.RectNode;

@UIData(zindex = Integer.MIN_VALUE)
@UIDataOverlay(active = true, render = @UIDataOverlayRender(always = true))
public class StageUI extends UI {

	@Override
	public void init() {
		RectNode
		.create(0, 0, 1920, 1080)
		.color(Color.TRANSPARENT)
		.interactive(false)
		.layer((node, mouseX, mouseY) -> this.drawStage())
		.attach(this);
	}

	private void drawStage() {
		final UIView view = super.getView();
		final double radius = 1.2D / view.toScreenWidth(1D);
		DrawUtils.SHAPE.drawRect(super.getViewX(), super.getViewY(), super.getViewWidth(), super.getViewHeight(), CaptureStyle.BACKGROUND);
		for (double y = 10D; y < view.toScreenY(super.getViewY() + super.getViewHeight()); y += 20D) {
			for (double x = 10D; x < view.toScreenX(super.getViewX() + super.getViewWidth()); x += 20D) {
				DrawUtils.SHAPE.drawCircle(view.toUiX(x), view.toUiY(y), CaptureStyle.DOT, radius);
			}
		}
	}

}