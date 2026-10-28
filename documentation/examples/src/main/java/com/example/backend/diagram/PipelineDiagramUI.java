package com.example.backend.diagram;

import dev.joid.lib.ui.node.impl.shape.RectNode;

public class PipelineDiagramUI extends BuildDiagramUI {

	@Override
	public void init() {
		final RectNode backdrop = super.backdrop(315, 240, 1290, 560).attach(this);

		super.box(30, 40, 270, 110, "Nodes, DrawUtils", "describe vertices").attach(backdrop);
		super.box(350, 40, 270, 110, "Tessellator", "TRIANGLES or LINES").attach(backdrop);
		super.box(670, 40, 270, 110, "RenderBridge.draw", "picks the shader").attach(backdrop);
		super.subject(990, 40, 270, 110, "drawPrimitive", "one draw call").attach(backdrop);

		super.box(840, 210, 420, 110, "Read at each draw", "getState(), matrices, resolveSampler()").attach(backdrop);

		super.box(30, 410, 270, 110, "JOID GLSL", "one source per stage").attach(backdrop);
		super.box(350, 410, 270, 110, "ShaderSource", "parsed declarations").attach(backdrop);
		super.box(670, 410, 270, 110, "Shader", "translates once").attach(backdrop);
		super.subject(990, 410, 270, 110, "compileProgram", "your dialect").attach(backdrop);

		for (final double y : new double[] {95D, 465D}) {
			super.arrow(backdrop, 300D, y, 345D, y);
			super.arrow(backdrop, 620D, y, 665D, y);
			super.arrow(backdrop, 940D, y, 985D, y);
		}

		super.arrow(backdrop, 1125D, 210D, 1125D, 155D);
	}

}