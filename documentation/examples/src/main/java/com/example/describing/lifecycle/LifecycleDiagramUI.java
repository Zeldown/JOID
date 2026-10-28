package com.example.describing.lifecycle;

import com.example.describing.DiagramUI;

public class LifecycleDiagramUI extends DiagramUI {

	@Override
	public void init() {
		super.backdrop(485, 320, 950, 440).body(backdrop -> {
			super.box(20, 40, 280, 100, "attach", "to a UI").attach(backdrop);
			super.subject(335, 40, 280, 100, "onInit", "once per attach").attach(backdrop);
			super.subject(650, 40, 280, 100, "onUpdate", "before drawing").attach(backdrop);
			super.subject(650, 300, 280, 100, "onDraw, onRender", "while drawing").attach(backdrop);
			super.subject(335, 300, 280, 100, "onMount", "first drawn frame only").attach(backdrop);
			super.subject(20, 300, 280, 100, "onDetach", "after remove").attach(backdrop);
			super.arrow(backdrop, 300, 90, 335, 90);
			super.arrow(backdrop, 615, 90, 650, 90);
			super.arrow(backdrop, 720, 140, 720, 300);
			super.arrow(backdrop, 860, 300, 860, 140);
			super.arrow(backdrop, 650, 350, 615, 350);
			super.line(backdrop, 720, 255, 160, 255);
			super.arrow(backdrop, 160, 255, 160, 300);
			super.label(335, 160, 340, 34, "every frame", 24F, DiagramUI.LABEL).attach(backdrop);
			super.label(335, 194, 340, 34, "while attached", 24F, DiagramUI.LABEL).attach(backdrop);
		}).attach(this);
	}

}