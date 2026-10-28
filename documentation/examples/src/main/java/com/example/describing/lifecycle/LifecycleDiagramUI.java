package com.example.describing.lifecycle;

import com.example.describing.DiagramUI;

public class LifecycleDiagramUI extends DiagramUI {

	@Override
	public void init() {
		super.backdrop(300, 300, 1320, 400).body(backdrop -> {
			super.box(40, 40, 280, 100, "attach", "to a UI").attach(backdrop);
			super.subject(360, 40, 280, 100, "onInit", "once per attach").attach(backdrop);
			super.subject(680, 40, 280, 100, "onUpdate", "before drawing").attach(backdrop);
			super.subject(1000, 40, 280, 100, "onDraw, onRender", "while drawing").attach(backdrop);
			super.subject(1000, 260, 280, 100, "onMount", "first drawn frame only").attach(backdrop);
			super.subject(40, 260, 280, 100, "onDetach", "after remove").attach(backdrop);
			super.arrow(backdrop, 320, 90, 360, 90);
			super.arrow(backdrop, 640, 90, 680, 90);
			super.arrow(backdrop, 960, 90, 1000, 90);
			super.line(backdrop, 1050, 140, 1050, 190);
			super.line(backdrop, 1050, 190, 820, 190);
			super.arrow(backdrop, 820, 190, 820, 140);
			super.arrow(backdrop, 1230, 140, 1230, 260);
			super.line(backdrop, 935, 190, 935, 310);
			super.arrow(backdrop, 935, 310, 320, 310);
			super.label(450, 170, 350, 40, "every frame while attached", 24F, DiagramUI.LABEL).attach(backdrop);
		}).attach(this);
	}

}