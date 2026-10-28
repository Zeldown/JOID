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

		super.box(40, 470, 255, 110, "backend-lwjgl2", "openal, opengl").attach(backdrop);
		super.box(315, 470, 255, 110, "backend-lwjgl3", "glfw, openal, opengl").attach(backdrop);
		super.box(590, 470, 255, 110, "backend-vulkan", "glfw, openal").attach(backdrop);
		super.subject(865, 470, 255, 110, "Your backend", "the modules it reuses").attach(backdrop);

		super.label(40, 605, 1080, 50, "A backend depends on the core and on the base modules it names").attach(backdrop);

		for (final double x : new double[] {210D, 580D, 950D}) {
			super.arrow(backdrop, x, 250D, x, 165D);
		}

		super.arrow(backdrop, 381D, 305D, 409D, 305D);

		super.note(40, 385, 1080, 40, "Each backend uses the base modules named in its box").attach(backdrop);
	}

}