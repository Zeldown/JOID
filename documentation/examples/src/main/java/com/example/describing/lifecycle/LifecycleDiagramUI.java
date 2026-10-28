package com.example.describing.lifecycle;

import com.example.describing.DiagramUI;

public class LifecycleDiagramUI extends DiagramUI {

	@Override
	public void init() {
		super.backdrop(515, 320, 890, 440).body(backdrop -> {
			super.box(30, 40, 250, 100, "attach", "to a UI").attach(backdrop);
			super.subject(320, 40, 250, 100, "onInit", "once per attach").attach(backdrop);
			super.subject(610, 40, 250, 100, "onUpdate", "before drawing").attach(backdrop);
			super.subject(610, 300, 250, 100, "onDraw, onRender", "while drawing").attach(backdrop);
			super.subject(320, 300, 250, 100, "onMount", "first drawn frame only").attach(backdrop);
			super.subject(30, 300, 250, 100, "onDetach", "after remove").attach(backdrop);
			super.arrow(backdrop, 280, 90, 320, 90);
			super.arrow(backdrop, 570, 90, 610, 90);
			super.arrow(backdrop, 680, 140, 680, 300);
			super.arrow(backdrop, 800, 300, 800, 140);
			super.arrow(backdrop, 610, 350, 570, 350);
			super.line(backdrop, 680, 255, 155, 255);
			super.arrow(backdrop, 155, 255, 155, 300);
			super.label(320, 160, 340, 34, "every frame", 24F, DiagramUI.LABEL).attach(backdrop);
			super.label(320, 194, 340, 34, "while attached", 24F, DiagramUI.LABEL).attach(backdrop);
		}).attach(this);
	}

}