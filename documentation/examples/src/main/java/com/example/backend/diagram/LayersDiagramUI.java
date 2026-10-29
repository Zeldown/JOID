package com.example.backend.diagram;

import dev.joid.lib.ui.node.impl.shape.RectNode;

public class LayersDiagramUI extends BuildDiagramUI {

	@Override
	public void init() {
		final RectNode backdrop = super.backdrop(510, 155, 900, 770).attach(this);

		super.box(40, 40, 820, 130, "joid-core", "UIs, nodes, signals, the bridge contracts and the generic half of every backend").attach(backdrop);

		super.box(40, 240, 250, 130, "base-lwjgl3-glfw", "window, input, OpenAL binding").attach(backdrop);
		super.box(325, 240, 250, 130, "base-openal", "audio bridge over IAlBinding").attach(backdrop);
		super.box(610, 240, 250, 130, "base-opengl", "render bridge over IGlBinding").attach(backdrop);

		super.box(40, 460, 400, 100, "backend-lwjgl2", "openal, opengl").attach(backdrop);
		super.box(460, 460, 400, 100, "backend-lwjgl3", "lwjgl3-glfw, openal, opengl").attach(backdrop);
		super.box(40, 580, 400, 100, "backend-vulkan", "lwjgl3-glfw, openal").attach(backdrop);
		super.subject(460, 580, 400, 100, "Your backend", "the modules it reuses").attach(backdrop);

		super.label(40, 700, 820, 50, "A backend depends on the core and on the base modules it names").attach(backdrop);

		for (final double x : new double[] {165D, 450D, 735D}) {
			super.arrow(backdrop, x, 240D, x, 175D);
		}

		super.arrow(backdrop, 291D, 305D, 324D, 305D);

		super.note(40, 395, 820, 40, "Each backend uses the base modules named in its box").attach(backdrop);
	}

}