package com.example.describing.lifecycle;

import com.example.describing.DiagramUI;

public class LifecycleDiagramUI extends DiagramUI {

	@Override
	public void init() {
		super.backdrop(160, 340, 1600, 340).body(backdrop -> {
			super.box(40, 60, 200, 100, "attach", "to a UI").attach(backdrop);
			super.subject(290, 60, 200, 100, "onInit", "once per attach").attach(backdrop);
			super.subject(540, 60, 220, 100, "onUpdate", "before drawing").attach(backdrop);
			super.subject(810, 60, 220, 100, "onMount", "first drawn frame").attach(backdrop);
			super.subject(1080, 60, 240, 100, "onDraw, onRender", "while drawing").attach(backdrop);
			super.subject(1370, 60, 190, 100, "onDetach", "after remove").attach(backdrop);
			super.arrow(backdrop, 240, 110, 290, 110);
			super.arrow(backdrop, 490, 110, 540, 110);
			super.arrow(backdrop, 760, 110, 810, 110);
			super.arrow(backdrop, 1030, 110, 1080, 110);
			super.arrow(backdrop, 1320, 110, 1370, 110);
			super.line(backdrop, 1200, 160, 1200, 230);
			super.line(backdrop, 1200, 230, 650, 230);
			super.arrow(backdrop, 650, 230, 650, 160);
			super.label(540, 245, 760, 40, "every frame while attached", 24F, DiagramUI.LABEL).attach(backdrop);
		}).attach(this);
	}

}