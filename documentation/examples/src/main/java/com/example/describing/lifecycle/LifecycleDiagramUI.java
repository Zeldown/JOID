package com.example.describing.lifecycle;

import com.example.describing.DiagramUI;

public class LifecycleDiagramUI extends DiagramUI {

	@Override
	public void init() {
		super.backdrop(360, 300, 1200, 400).body(backdrop -> {
			super.box(40, 40, 250, 100, "attach", "to a UI").attach(backdrop);
			super.subject(330, 40, 250, 100, "onInit", "once per attach").attach(backdrop);
			super.subject(620, 40, 250, 100, "onUpdate", "before drawing").attach(backdrop);
			super.subject(910, 40, 250, 100, "onDraw, onRender", "while drawing").attach(backdrop);
			super.subject(910, 260, 250, 100, "onMount", "first drawn frame only").attach(backdrop);
			super.subject(40, 260, 250, 100, "onDetach", "after remove").attach(backdrop);
			super.arrow(backdrop, 290, 90, 330, 90);
			super.arrow(backdrop, 580, 90, 620, 90);
			super.arrow(backdrop, 870, 90, 910, 90);
			super.line(backdrop, 960, 140, 960, 190);
			super.line(backdrop, 960, 190, 745, 190);
			super.arrow(backdrop, 745, 190, 745, 140);
			super.arrow(backdrop, 1110, 140, 1110, 260);
			super.line(backdrop, 850, 190, 850, 310);
			super.arrow(backdrop, 850, 310, 290, 310);
			super.label(380, 170, 350, 40, "every frame while attached", 24F, DiagramUI.LABEL).attach(backdrop);
		}).attach(this);
	}

}