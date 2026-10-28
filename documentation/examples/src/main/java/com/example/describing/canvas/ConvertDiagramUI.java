package com.example.describing.canvas;

import com.example.capture.CaptureStyle;
import com.example.describing.DiagramUI;

public class ConvertDiagramUI extends DiagramUI {

	@Override
	public void init() {
		super.backdrop(300, 290, 1320, 500).body(backdrop -> {
			super.box(40, 40, 320, 200, "Screen", "screen pixels").attach(backdrop);
			super.label(40, 186, 320, 36, "window, system mouse", 20F, DiagramUI.NOTE).attach(backdrop);
			super.box(500, 40, 320, 200, "Viewport", "design pixels").attach(backdrop);
			super.label(500, 186, 320, 36, "window / fit, bands", 20F, DiagramUI.NOTE).attach(backdrop);
			super.subject(960, 40, 320, 200, "UI", "design pixels").attach(backdrop);
			super.label(960, 186, 320, 36, "your nodes", 20F, CaptureStyle.ON_ACCENT).attach(backdrop);

			super.arrow(backdrop, 364, 140, 496, 140);
			super.label(360, 100, 140, 32, "/ fit", 20F, DiagramUI.NOTE).attach(backdrop);
			super.arrow(backdrop, 824, 140, 956, 140);
			super.label(820, 100, 140, 32, "anchor shift", 20F, DiagramUI.NOTE).attach(backdrop);
			super.label(820, 148, 140, 32, "scale, zoom", 20F, DiagramUI.NOTE).attach(backdrop);

			super.line(backdrop, 200, 244, 200, 380);
			super.line(backdrop, 1120, 244, 1120, 380);
			super.arrow(backdrop, 200, 310, 1116, 310);
			super.arrow(backdrop, 1120, 380, 204, 380);
			super.label(200, 264, 920, 36, "toUiX(x), toUiY(y): from the screen to the UI", 22F, DiagramUI.LABEL).attach(backdrop);
			super.label(200, 334, 920, 36, "toScreenX(x), toScreenY(y): from the UI to the screen", 22F, DiagramUI.LABEL).attach(backdrop);
			super.label(40, 430, 1240, 36, "toScreenWidth(w), toScreenHeight(h): a size, without the anchor shift", 22F, DiagramUI.NOTE).attach(backdrop);
		}).attach(this);
	}

}