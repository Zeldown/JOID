package com.example.further;

import com.example.capture.CaptureStyle;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.render.transform.Rotation;
import dev.joid.lib.render.transform.Transformation;
import dev.joid.lib.render.transform.Vector;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;

public class PivotSpinUI extends UI {

	private final long start = BridgeHandler.CLOCK.get().currentTimeMillis();

	private final Transformation center = Transformation
	.create()
	.rotate(() -> this.getAngle(), Rotation.ROLL, Vector.create(200D, 200D));
	private final Transformation corner = Transformation
	.create()
	.rotate(() -> this.getAngle(), Rotation.ROLL, Vector.create(520D, 200D));

	@Override
	public void init() {
		ContainerNode
		.create(560, 340, 800, 400)
		.layer((node, mouseX, mouseY) -> {
			this.center.apply(() -> DrawUtils.SHAPE.drawRect(130D, 160D, 140D, 80D, CaptureStyle.ACCENT));
			this.corner.apply(() -> DrawUtils.SHAPE.drawRect(520D, 200D, 140D, 80D, CaptureStyle.ACCENT));
			DrawUtils.SHAPE.drawBorder(130D, 160D, 140D, 80D, CaptureStyle.MUTED, 2D);
			DrawUtils.SHAPE.drawBorder(520D, 200D, 140D, 80D, CaptureStyle.MUTED, 2D);
			DrawUtils.SHAPE.drawCircle(200D, 200D, CaptureStyle.TEXT, 7D);
			DrawUtils.SHAPE.drawCircle(520D, 200D, CaptureStyle.TEXT, 7D);
		})
		.attach(this);
	}

	private double getAngle() {
		return (BridgeHandler.CLOCK.get().currentTimeMillis() - this.start) % 4000L * 0.09D;
	}

}