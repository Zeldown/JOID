package dev.joid.lib.render.transform.operation;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.render.transform.Scale;
import dev.joid.lib.render.transform.Vector;

public class ScaleTransformOperationTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void scalesEachAxis() {
		new ScaleTransformOperation(Scale.create(2D, 3D, 4D), Vector.create()).transform();
		final float[] matrix = this.bridges.getRender().getModelView().getMatrix();
		Assert.assertEquals(2F, matrix[0], 0F);
		Assert.assertEquals(3F, matrix[5], 0F);
		Assert.assertEquals(4F, matrix[10], 0F);
	}

	@Test
	public void keepsItsPivotInPlace() {
		new ScaleTransformOperation(Scale.create(2D, 3D, 1D), Vector.create(100D, 50D)).transform();
		final PixelGrid grid = this.bridges.getRender().getPixelGrid();
		Assert.assertEquals(100D, grid.toScreenX(100D), 1E-4D);
		Assert.assertEquals(120D, grid.toScreenX(110D), 1E-4D);
		Assert.assertEquals(1030D, grid.toScreenY(50D), 1E-4D);
		Assert.assertEquals(1000D, grid.toScreenY(60D), 1E-4D);
	}

	@Test
	public void keepsADeepPivotInPlace() {
		new ScaleTransformOperation(Scale.DEPTH(2D), Vector.Z(10D)).transform();
		final float[] matrix = this.bridges.getRender().getModelView().getMatrix();
		Assert.assertEquals(10F, matrix[10] * 10F + matrix[14], 0F);
	}

	@Test
	public void readsItsScaleAndPivotOnEveryTransform() {
		final double[] values = {1D, 0D};
		final ScaleTransformOperation operation = new ScaleTransformOperation(Scale.WIDTH(() -> values[0]), Vector.X(() -> values[1]));
		values[0] = 2D;
		values[1] = 100D;
		operation.transform();
		final PixelGrid grid = this.bridges.getRender().getPixelGrid();
		Assert.assertEquals(100D, grid.toScreenX(100D), 1E-4D);
		Assert.assertEquals(140D, grid.toScreenX(120D), 1E-4D);
	}

}