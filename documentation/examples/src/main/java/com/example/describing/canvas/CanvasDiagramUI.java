package com.example.describing.canvas;

import com.example.describing.DiagramUI;

import dev.joid.lib.ui.node.impl.shape.RectNode;

public class CanvasDiagramUI extends DiagramUI {

	@Override
	public void init() {
		super.backdrop(360, 290, 1200, 500).body(backdrop -> {
			RectNode
			.create(40, 80, 640, 270)
			.color(DiagramUI.ELEMENT)
			.body(window -> RectNode
				.create(80, 0, 480, 270)
				.color(DiagramUI.SURFACE)
				.body(canvas -> super.label(0, 0, 480, 270, "design canvas 1920 × 1080", 22F, DiagramUI.LABEL).attach(canvas))
				.attach(window))
			.attach(backdrop);

			RectNode
			.create(760, 40, 400, 360)
			.color(DiagramUI.ELEMENT)
			.body(window -> RectNode
				.create(0, 67.5, 400, 225)
				.color(DiagramUI.SURFACE)
				.body(canvas -> super.label(0, 0, 400, 225, "design canvas", 22F, DiagramUI.LABEL).attach(canvas))
				.attach(window))
			.attach(backdrop);

			super.label(40, 420, 640, 40, "Wider window: more on the sides", 22F, DiagramUI.LABEL).attach(backdrop);
			super.label(740, 420, 440, 40, "Taller window: more above and below", 22F, DiagramUI.LABEL).attach(backdrop);
		}).attach(this);
	}

}