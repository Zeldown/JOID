package com.example.describing.nodes;

import com.example.describing.DiagramUI;

import dev.joid.lib.ui.node.impl.shape.RectNode;

public class TreeDiagramUI extends DiagramUI {

	@Override
	public void init() {
		super.backdrop(360, 190, 1200, 560).body(backdrop -> {
			super.box(40, 40, 320, 70, "MenuUI", "").attach(backdrop);
			super.box(40, 160, 320, 90, "RectNode card", "at (760, 340)").attach(backdrop);
			super.box(110, 300, 250, 70, "TextNode", "").attach(backdrop);
			super.subject(110, 410, 250, 90, "RectNode bar", "at (50, 130)").attach(backdrop);
			super.arrow(backdrop, 200, 110, 200, 160);
			super.line(backdrop, 60, 250, 60, 455);
			super.arrow(backdrop, 60, 335, 110, 335);
			super.arrow(backdrop, 60, 455, 110, 455);

			RectNode
			.create(440, 50, 672, 378)
			.color(DiagramUI.SURFACE)
			.body(canvas -> {
				RectNode
				.create(266, 119, 140, 140)
				.color(DiagramUI.ELEMENT)
				.body(card -> {
					super.label(17.5, 10.5, 105, 28, "Text", 18F, DiagramUI.LABEL).attach(card);
					RectNode.create(17.5, 45.5, 105, 24.5).color(DiagramUI.ACCENT).attach(card);
				})
				.attach(canvas);
			})
			.attach(backdrop);

			super.label(440, 445, 672, 30, "card at (760, 340) on the canvas", 22F, DiagramUI.LABEL).attach(backdrop);
			super.label(440, 475, 672, 30, "bar at (50, 130) in the card", 22F, DiagramUI.LABEL).attach(backdrop);
			super.label(440, 505, 672, 30, "so bar at (810, 470) on the canvas", 22F, DiagramUI.ACCENT).attach(backdrop);
		}).attach(this);
	}

}