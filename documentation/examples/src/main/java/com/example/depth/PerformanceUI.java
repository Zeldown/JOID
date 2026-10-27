package com.example.depth;

import dev.joid.lib.utils.align.Align;

public class PerformanceUI extends DepthDiagramUI {

	@Override
	public void init() {
		super.backdrop(360, 200, 1200, 680)
		.body(card -> {
			super.label(40, 30, 540, 40, "Every frame", Align.START).attach(card);
			super.subject(40, 80, 540, 100, "Visible nodes", "layout, hover test, draw").attach(card);
			super.box(40, 200, 540, 100, "Every node", "followed values, animators, update()").attach(card);
			super.box(40, 320, 540, 100, "Off-screen effects", "one pass per effect, over the node area").attach(card);

			super.label(620, 30, 540, 40, "Once", Align.START).attach(card);
			super.box(620, 80, 540, 100, "Decode a resource", "on its first draw").attach(card);
			super.box(620, 200, 540, 100, "Generate a font atlas", "on the first load on a machine").attach(card);
			super.box(620, 320, 540, 100, "Compile a shader", "on its first draw").attach(card);

			super.label(40, 450, 1120, 40, "GPU memory", Align.START).attach(card);
			super.box(40, 500, 540, 120, "Released by JOID", "resources, fonts, effect images").attach(card);
			super.subject(620, 500, 540, 120, "Released by you", "the framebuffers you create").attach(card);
		})
		.attach(this);
	}

}