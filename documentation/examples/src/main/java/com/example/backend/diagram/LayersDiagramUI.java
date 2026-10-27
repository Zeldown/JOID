package com.example.backend.diagram;

import dev.joid.lib.ui.node.impl.shape.RectNode;

public class LayersDiagramUI extends BuildDiagramUI {

	@Override
	public void init() {
		final RectNode backdrop = super.backdrop(380, 150, 1160, 680).attach(this);

		super.box(40, 50, 1080, 110, "joid-core", "UIs, nodes, signals, the bridge contracts and the generic half of every backend").attach(backdrop);

		super.box(40, 250, 340, 110, "base-glfw", "window, input, OpenAL binding").attach(backdrop);
		super.box(410, 250, 340, 110, "base-openal", "audio bridge over IAlBinding").attach(backdrop);
		super.box(780, 250, 340, 110, "base-opengl", "render bridge over IGlBinding").attach(backdrop);

		super.box(40, 450, 255, 110, "backend-lwjgl2", "openal, opengl").attach(backdrop);
		super.box(315, 450, 255, 110, "backend-lwjgl3", "glfw, openal, opengl").attach(backdrop);
		super.box(590, 450, 255, 110, "backend-vulkan", "glfw, openal").attach(backdrop);
		super.subject(865, 450, 255, 110, "Your backend", "the modules it reuses").attach(backdrop);

		super.label(40, 590, 1080, 50, "Each layer depends only on the layers above it").attach(backdrop);

		for (final double x : new double[] {210D, 580D, 950D}) {
			super.arrow(backdrop, x, 250D, x, 165D);
		}

		for (final double x : new double[] {167D, 442D, 717D, 992D}) {
			super.arrow(backdrop, x, 450D, x, 365D);
		}
	}

}