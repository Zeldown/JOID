package com.example.backend.diagram;

import dev.joid.lib.ui.node.impl.shape.RectNode;

public class PipelineDiagramUI extends BuildDiagramUI {

	@Override
	public void init() {
		final RectNode backdrop = super.backdrop(510, 170, 900, 740).attach(this);

		super.box(40, 40, 380, 90, "Nodes, DrawUtils", "describe vertices").attach(backdrop);
		super.box(40, 175, 380, 90, "Tessellator", "TRIANGLES or LINES").attach(backdrop);
		super.box(40, 310, 380, 90, "RenderBridge.draw", "picks the shader").attach(backdrop);
		super.subject(40, 445, 380, 90, "drawPrimitive", "one draw call").attach(backdrop);

		super.box(40, 580, 380, 120, "Read at each draw", "getState(), matrices, resolveSampler()").attach(backdrop);

		super.box(480, 40, 380, 90, "JOID GLSL", "one source per stage").attach(backdrop);
		super.box(480, 175, 380, 90, "ShaderSource", "parsed declarations").attach(backdrop);
		super.box(480, 310, 380, 90, "Shader", "translates once").attach(backdrop);
		super.subject(480, 445, 380, 90, "compileProgram", "your dialect").attach(backdrop);

		for (final double x : new double[] {230D, 670D}) {
			super.arrow(backdrop, x, 130D, x, 170D);
			super.arrow(backdrop, x, 265D, x, 305D);
			super.arrow(backdrop, x, 400D, x, 440D);
		}

		super.arrow(backdrop, 230D, 580D, 230D, 540D);
	}

}