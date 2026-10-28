package com.example.depth;

import dev.joid.lib.utils.align.Align;

public class PerformanceUI extends DepthDiagramUI {

	@Override
	public void init() {
		super.backdrop(510, 190, 900, 700)
		.body(card -> {
			super.label(40, 30, 390, 40, "Every frame", Align.START).attach(card);
			super.subject(40, 80, 390, 120, "Visible nodes", "layout, hover test, draw").attach(card);
			super.box(40, 220, 390, 120, "Every node", "followed values, animators, update()").attach(card);
			super.box(40, 360, 390, 120, "Off-screen effects", "one pass per effect,\nover the node area").attach(card);

			super.label(470, 30, 390, 40, "Once", Align.START).attach(card);
			super.box(470, 80, 390, 120, "Decode a resource", "on its first draw").attach(card);
			super.box(470, 220, 390, 120, "Generate a font atlas", "on the first load on a machine").attach(card);
			super.box(470, 360, 390, 120, "Compile a shader", "on its first draw").attach(card);

			super.label(40, 500, 820, 40, "GPU memory", Align.START).attach(card);
			super.box(40, 550, 390, 120, "Released by JOID", "resources, fonts, effect images").attach(card);
			super.subject(470, 550, 390, 120, "Released by you", "the framebuffers you create").attach(card);
		})
		.attach(this);
	}

}