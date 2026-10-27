package com.example.describing.overlays;

import com.example.describing.DiagramUI;

public class StackDiagramUI extends DiagramUI {

	@Override
	public void init() {
		super.backdrop(410, 190, 1100, 700).body(backdrop -> {
			super.label(40, 30, 660, 40, "Drawn last, on top", 24F, DiagramUI.LABEL).attach(backdrop);
			super.box(40, 90, 660, 110, "Overlays above the screens", "render = @UIDataOverlayRender(aboveScreens = true)").attach(backdrop);
			super.subject(40, 230, 660, 110, "Popups and screens", "by zindex, then in opening order").attach(backdrop);
			super.box(40, 370, 660, 110, "Overlays", "drawn below the screens by default").attach(backdrop);
			super.label(40, 510, 660, 40, "Drawn first, at the bottom", 24F, DiagramUI.LABEL).attach(backdrop);
			super.arrow(backdrop, 760, 90, 760, 480);
			super.label(780, 200, 300, 40, "Input goes", 24F, DiagramUI.LABEL).attach(backdrop);
			super.label(780, 240, 300, 40, "from the top down", 24F, DiagramUI.LABEL).attach(backdrop);
			super.label(780, 300, 300, 36, "and stops at the first UI", 20F, DiagramUI.LABEL).attach(backdrop);
			super.label(780, 336, 300, 36, "that consumes it,", 20F, DiagramUI.LABEL).attach(backdrop);
			super.label(780, 372, 300, 36, "or at a popup", 20F, DiagramUI.LABEL).attach(backdrop);
			super.label(40, 600, 1020, 40, "Overlays take input only with interaction = @UIDataOverlayInteraction(active = true)", 22F, DiagramUI.ACCENT).attach(backdrop);
		}).attach(this);
	}

}