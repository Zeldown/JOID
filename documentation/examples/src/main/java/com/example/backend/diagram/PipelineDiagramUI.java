package com.example.backend.diagram;

import dev.joid.lib.ui.node.impl.shape.RectNode;

public class PipelineDiagramUI extends BuildDiagramUI {

	@Override
	public void init() {
		final RectNode backdrop = super.backdrop(360, 240, 1200, 560).attach(this);

		super.box(30, 40, 240, 110, "Nodes, DrawUtils", "describe vertices").attach(backdrop);
		super.box(330, 40, 240, 110, "Tessellator", "TRIANGLES or LINES").attach(backdrop);
		super.box(630, 40, 240, 110, "RenderBridge.draw", "picks the shader").attach(backdrop);
		super.subject(930, 40, 240, 110, "drawPrimitive", "one draw call").attach(backdrop);

		super.box(780, 210, 390, 110, "Read at each draw", "getState(), matrices, resolveSampler()").attach(backdrop);

		super.box(30, 410, 240, 110, "JOID GLSL", "one source per stage").attach(backdrop);
		super.box(330, 410, 240, 110, "ShaderSource", "parsed declarations").attach(backdrop);
		super.box(630, 410, 240, 110, "Shader", "translates once").attach(backdrop);
		super.subject(930, 410, 240, 110, "compileProgram", "your dialect").attach(backdrop);

		for (final double y : new double[] {95D, 465D}) {
			super.arrow(backdrop, 270D, y, 325D, y);
			super.arrow(backdrop, 570D, y, 625D, y);
			super.arrow(backdrop, 870D, y, 925D, y);
		}

		super.arrow(backdrop, 1050D, 210D, 1050D, 155D);
	}

}