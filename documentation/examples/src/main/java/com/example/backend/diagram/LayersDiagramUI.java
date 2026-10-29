package com.example.backend.diagram;

import dev.joid.lib.ui.node.impl.shape.RectNode;

public class LayersDiagramUI extends BuildDiagramUI {

	@Override
	public void init() {
		final RectNode backdrop = super.backdrop(510, 70, 900, 940).attach(this);

		super.box(40, 40, 820, 100, "joid-core", "UIs, nodes, signals, the bridge contracts and the generic half of every backend").attach(backdrop);

		super.box(40, 200, 400, 90, "base-openal", "audio bridge over IAlBinding").attach(backdrop);
		super.box(460, 200, 400, 90, "base-opengl", "render bridge over IGlBinding").attach(backdrop);

		super.box(40, 350, 400, 90, "base-lwjgl2", "bindings and bridges of LWJGL 2").attach(backdrop);
		super.box(460, 350, 400, 90, "base-lwjgl3-glfw", "window, input and keys of GLFW").attach(backdrop);
		super.box(40, 460, 400, 90, "base-lwjgl3-openal", "binding of base-openal").attach(backdrop);
		super.box(460, 460, 400, 90, "base-lwjgl3-opengl", "binding of base-opengl").attach(backdrop);

		super.box(40, 630, 400, 100, "backend-lwjgl2", "lwjgl2, openal, opengl").attach(backdrop);
		super.box(460, 630, 400, 100, "backend-lwjgl3", "lwjgl3-glfw, lwjgl3-openal, lwjgl3-opengl, openal, opengl").attach(backdrop);
		super.box(40, 750, 400, 100, "backend-vulkan", "lwjgl3-glfw, lwjgl3-openal, openal").attach(backdrop);
		super.subject(460, 750, 400, 100, "Your backend", "the modules it reuses").attach(backdrop);

		super.label(40, 880, 820, 50, "A backend depends on the core and on the base modules it names").attach(backdrop);

		for (final double x : new double[] {240D, 660D}) {
			super.arrow(backdrop, x, 200D, x, 145D);
		}

		super.note(40, 305, 820, 30, "Library modules below bind their library to the API modules above").attach(backdrop);
		super.note(40, 570, 820, 40, "Each backend uses the base modules named in its box").attach(backdrop);
	}

}