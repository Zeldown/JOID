package com.example.depth;

import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.geom.Point;

public class FrameLifecycleUI extends DepthDiagramUI {

	@Override
	public void init() {
		super.backdrop(370, 190, 1180, 700)
		.body(card -> {
			this.row(card, 30D, 5, true, "Each frame", "Input", "Resize", "Tasks", "Update", "Background", "Draw UIs", "Present");
			this.row(card, 270D, 3, false, "Each UI", "Tasks", "Backdrop", "Transition", "Nodes", "Tooltip");
			this.row(card, 510D, -1, false, "Each node", "Values", "Layout", "Hover", "Scroll, drag", "Effects", "Draw");
			card.layer((node, mouseX, mouseY) -> {
				DrawUtils.SHAPE.drawLine(DepthDiagramUI.ACCENT, 2F, Point.of(840D, 170D), Point.of(40D, 320D));
				DrawUtils.SHAPE.drawLine(DepthDiagramUI.ACCENT, 2F, Point.of(970D, 170D), Point.of(810D, 320D));
				DrawUtils.SHAPE.drawLine(DepthDiagramUI.ACCENT, 2F, Point.of(520D, 410D), Point.of(40D, 560D));
				DrawUtils.SHAPE.drawLine(DepthDiagramUI.ACCENT, 2F, Point.of(650D, 410D), Point.of(970D, 560D));
			});
		})
		.attach(this);
	}

	private void row(final Node card, final double top, final int zoomed, final boolean loop, final String title, final String... steps) {
		super.label(40, top, 400, 40, title, Align.START).attach(card);
		for (int i = 0; i < steps.length; i++) {
			final double x = 40D + i * 160D;
			if (i == zoomed) {
				super.subject(x, top + 50D, 130, 90, steps[i], "").attach(card);
			} else if (loop && (i == 0 || i == steps.length - 1)) {
				RectNode
				.create(x, top + 50D, 130, 90)
				.color(DepthDiagramUI.INK)
				.body(rect -> RectNode.create(3, 3, 124, 84).color(DepthDiagramUI.BACKDROP).attach(rect))
				.attach(card);
				super.label(x, top + 50D, 130, 90, steps[i], Align.CENTER).attach(card);
			} else {
				super.box(x, top + 50D, 130, 90, steps[i], "").attach(card);
			}

			if (i < steps.length - 1) {
				super.arrow(card, x + 132D, top + 95D, x + 158D, top + 95D);
			}
		}
	}

}