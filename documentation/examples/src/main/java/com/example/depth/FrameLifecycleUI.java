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
		super.backdrop(510, 210, 900, 660)
		.body(card -> {
			this.column(card, 25D, 5, true, "Each frame", "Input", "Resize", "Tasks", "Update", "Background", "Draw UIs", "Present");
			this.column(card, 340D, 3, false, "Each UI", "Tasks", "Backdrop", "Transition", "Nodes", "Tooltip");
			this.column(card, 655D, -1, false, "Each node", "Values", "Layout", "Hover", "Scroll, drag", "Effects", "Draw");
			super.label(25, 590, 850, 40, "Outlined: run by your loop", Align.START).attach(card);
			card.layer((node, mouseX, mouseY) -> {
				DrawUtils.SHAPE.drawLine(DepthDiagramUI.ACCENT, 2F, Point.of(240D, 440D), Point.of(340D, 30D));
				DrawUtils.SHAPE.drawLine(DepthDiagramUI.ACCENT, 2F, Point.of(240D, 494D), Point.of(340D, 422D));
				DrawUtils.SHAPE.drawLine(DepthDiagramUI.ACCENT, 2F, Point.of(555D, 296D), Point.of(655D, 30D));
				DrawUtils.SHAPE.drawLine(DepthDiagramUI.ACCENT, 2F, Point.of(555D, 350D), Point.of(655D, 494D));
			});
		})
		.attach(this);
	}

	private void column(final Node card, final double left, final int zoomed, final boolean loop, final String title, final String... steps) {
		super.label(left, 30, 215, 40, title, Align.START).attach(card);
		for (int i = 0; i < steps.length; i++) {
			final double y = 80D + i * 72D;
			if (i == zoomed) {
				super.subject(left, y, 215, 54, steps[i], "").attach(card);
			} else if (loop && (i == 0 || i == steps.length - 1)) {
				RectNode
				.create(left, y, 215, 54)
				.color(CaptureStyle.BACKGROUND)
				.effect(RoundedNodeEffect.create(12F))
				.effect(BorderNodeEffect.create(DepthDiagramUI.NOTE, 2F))
				.attach(card);
				super.label(left, y, 215, 54, steps[i], Align.CENTER).attach(card);
			} else {
				super.box(left, y, 215, 54, steps[i], "").attach(card);
			}

			if (i < steps.length - 1) {
				super.arrow(card, left + 107.5D, y + 55D, left + 107.5D, y + 71D);
			}
		}
	}

}