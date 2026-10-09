package dev.joid.lib.render.transform.operation;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.render.transform.Rotation;
import dev.joid.lib.render.transform.Vector;

public class RotateOperationTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void turnsAroundItsPivot() {
		new RotateOperation(180D, Rotation.YAW, Vector.create(100D, 0D)).transform();
		final PixelGrid grid = this.bridges.getRender().getPixelGrid();
		Assert.assertTrue(grid.isAligned());
		Assert.assertEquals(100D, grid.toScreenX(100D), 1E-4D);
		Assert.assertEquals(90D, grid.toScreenX(110D), 1E-4D);
		Assert.assertEquals(250D, grid.toScreenX(-50D), 1E-4D);
	}

	@Test
	public void turnsByItsAngle() {
		new RotateOperation(60D, Rotation.YAW, Vector.create(100D, 0D)).transform();
		final PixelGrid grid = this.bridges.getRender().getPixelGrid();
		Assert.assertEquals(100D, grid.toScreenX(100D), 1E-4D);
		Assert.assertEquals(105D, grid.toScreenX(110D), 1E-4D);
	}

	@Test
	public void readsItsAngleOnEveryTransform() {
		final double[] angle = {0D};
		final RotateOperation operation = new RotateOperation(() -> angle[0], Rotation.YAW, Vector.create());
		operation.transform();
		Assert.assertEquals(1F, this.bridges.getRender().getModelView().getMatrix()[0], 0F);
		angle[0] = 180D;
		operation.transform();
		Assert.assertEquals(-1F, this.bridges.getRender().getModelView().getMatrix()[0], 1E-6F);
	}

	@Test
	public void leavesTheMatrixWithoutAxis() {
		new RotateOperation(90D, Rotation.create(), Vector.create(50D, 60D, 70D)).transform();
		Assert.assertArrayEquals(new float[] {1F, 0F, 0F, 0F, 0F, 1F, 0F, 0F, 0F, 0F, 1F, 0F, 0F, 0F, 0F, 1F}, this.bridges.getRender().getModelView().getMatrix(), 0F);
	}

}