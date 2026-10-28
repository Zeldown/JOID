package com.example.depth;

import dev.joid.lib.utils.align.Align;

public class PerformanceUI extends DepthDiagramUI {

	@Override
	public void init() {
		super.backdrop(510, 176, 900, 730)
		.body(card -> {
			super.label(40, 30, 390, 40, "Every frame", Align.START).attach(card);
			super.subject(40, 80, 390, 100, "Visible nodes", "layout, hover test, draw").attach(card);
			super.box(40, 196, 390, 100, "Every node", "followed values, onAnimate, update()").attach(card);
			super.box(40, 312, 390, 100, "Started animators", "until their steps end").attach(card);
			super.box(40, 428, 390, 100, "Off-screen effects", "one pass per effect,\nover the node area").attach(card);

			super.label(470, 30, 390, 40, "Once", Align.START).attach(card);
			super.box(470, 80, 390, 100, "Decode a resource", "on its first draw").attach(card);
			super.box(470, 196, 390, 100, "Generate a font atlas", "on the first load on a machine").attach(card);
			super.box(470, 312, 390, 100, "Compile a shader", "on its first draw").attach(card);

			super.label(40, 548, 820, 40, "GPU memory", Align.START).attach(card);
			super.box(40, 598, 390, 100, "Released by JOID", "resources, fonts, effect images").attach(card);
			super.subject(470, 598, 390, 100, "Released by you", "the framebuffers you create").attach(card);
		})
		.attach(this);
	}

}