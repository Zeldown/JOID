package com.example.describing.canvas;

import com.example.capture.CaptureStyle;
import com.example.describing.DiagramUI;

public class ConvertDiagramUI extends DiagramUI {

	@Override
	public void init() {
		super.backdrop(385, 290, 1150, 500).body(backdrop -> {
			super.box(30, 40, 270, 200, "Screen", "screen pixels").attach(backdrop);
			super.label(30, 186, 270, 36, "window, system mouse", 21F, DiagramUI.NOTE).attach(backdrop);
			super.box(440, 40, 270, 200, "Viewport", "design pixels").attach(backdrop);
			super.label(440, 186, 270, 36, "window / fit, bands", 21F, DiagramUI.NOTE).attach(backdrop);
			super.subject(850, 40, 270, 200, "UI", "design pixels").attach(backdrop);
			super.label(850, 186, 270, 36, "your nodes", 21F, CaptureStyle.ON_ACCENT).attach(backdrop);

			super.arrow(backdrop, 304, 140, 436, 140);
			super.label(300, 100, 140, 32, "/ fit", 21F, DiagramUI.NOTE).attach(backdrop);
			super.arrow(backdrop, 714, 140, 846, 140);
			super.label(710, 100, 140, 32, "anchor shift", 21F, DiagramUI.NOTE).attach(backdrop);
			super.label(710, 148, 140, 32, "scale, zoom", 21F, DiagramUI.NOTE).attach(backdrop);

			super.line(backdrop, 165, 244, 165, 380);
			super.line(backdrop, 985, 244, 985, 380);
			super.arrow(backdrop, 165, 310, 981, 310);
			super.arrow(backdrop, 985, 380, 169, 380);
			super.label(165, 264, 820, 36, "toUiX(x), toUiY(y): from the screen to the UI", 22F, DiagramUI.LABEL).attach(backdrop);
			super.label(165, 334, 820, 36, "toScreenX(x), toScreenY(y): from the UI to the screen", 22F, DiagramUI.LABEL).attach(backdrop);
			super.label(30, 430, 1090, 36, "toScreenWidth(w), toScreenHeight(h): a size, without the anchor shift", 22F, DiagramUI.NOTE).attach(backdrop);
		}).attach(this);
	}

}