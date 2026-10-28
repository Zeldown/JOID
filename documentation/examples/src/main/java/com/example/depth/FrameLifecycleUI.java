package com.example.depth;

import com.example.capture.CaptureStyle;

import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.geom.Point;

public class FrameLifecycleUI extends DepthDiagramUI {

	@Override
	public void init() {
		super.backdrop(360, 190, 1200, 700)
		.body(card -> {
			super.label(600, 30, 575, 40, "Outlined: run by your loop", Align.END).attach(card);
			this.row(card, 30D, 5, true, "Each frame", "Input", "Resize", "Tasks", "Update", "Background", "Draw UIs", "Present");
			this.row(card, 270D, 3, false, "Each UI", "Tasks", "Backdrop", "Transition", "Nodes", "Tooltip");
			this.row(card, 510D, -1, false, "Each node", "Values", "Layout", "Hover", "Scroll, drag", "Effects", "Draw");
			card.layer((node, mouseX, mouseY) -> {
				DrawUtils.SHAPE.drawLine(DepthDiagramUI.ACCENT, 2F, Point.of(855D, 170D), Point.of(25D, 270D));
				DrawUtils.SHAPE.drawLine(DepthDiagramUI.ACCENT, 2F, Point.of(1005D, 170D), Point.of(839D, 270D));
				DrawUtils.SHAPE.drawLine(DepthDiagramUI.ACCENT, 2F, Point.of(523D, 410D), Point.of(25D, 510D));
				DrawUtils.SHAPE.drawLine(DepthDiagramUI.ACCENT, 2F, Point.of(673D, 410D), Point.of(1005D, 510D));
			});
		})
		.attach(this);
	}

	private void row(final Node card, final double top, final int zoomed, final boolean loop, final String title, final String... steps) {
		super.label(25, top, 400, 40, title, Align.START).attach(card);
		for (int i = 0; i < steps.length; i++) {
			final double x = 25D + i * 166D;
			if (i == zoomed) {
				super.subject(x, top + 50D, 150, 90, steps[i], "").attach(card);
			} else if (loop && (i == 0 || i == steps.length - 1)) {
				RectNode
				.create(x, top + 50D, 150, 90)
				.color(CaptureStyle.BACKGROUND)
				.effect(RoundedNodeEffect.create(12F))
				.effect(BorderNodeEffect.create(DepthDiagramUI.NOTE, 2F))
				.attach(card);
				super.label(x, top + 50D, 150, 90, steps[i], Align.CENTER).attach(card);
			} else {
				super.box(x, top + 50D, 150, 90, steps[i], "").attach(card);
			}

			if (i < steps.length - 1) {
				super.arrow(card, x + 151D, top + 95D, x + 165D, top + 95D);
			}
		}
	}

}