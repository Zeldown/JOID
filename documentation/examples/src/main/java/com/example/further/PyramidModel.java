package com.example.further;

import dev.joid.lib.draw.model.IDrawableModel;
import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.render.tessellator.Tessellator;

// #region model
public class PyramidModel implements IDrawableModel {

	private static final double[][] CORNERS = {{-0.5D, -0.5D}, {0.5D, -0.5D}, {0.5D, 0.5D}, {-0.5D, 0.5D}};

	@Override
	public void render() {
		final Tessellator tessellator = Tessellator.inst();
		tessellator.start(DrawMode.TRIANGLES);
		tessellator.setColor(200, 200, 200);
		for (int side = 0; side < 4; side++) {
			final double[] start = PyramidModel.CORNERS[side];
			final double[] end = PyramidModel.CORNERS[(side + 1) % 4];
			final float normalX = (float) (start[0] + end[0]);
			final float normalZ = (float) (start[1] + end[1]);
			tessellator.setNormal(normalX, 0.5F, normalZ);
			tessellator.addVertex(start[0], -0.5D, start[1]);
			tessellator.addVertex(end[0], -0.5D, end[1]);
			tessellator.addVertex(0D, 0.5D, 0D);
		}
		tessellator.draw();
	}

	@Override
	public double getWidth() {
		return 1D;
	}

	@Override
	public double getHeight() {
		return 1D;
	}

	@Override
	public double getDepth() {
		return 1D;
	}

}
// #endregion