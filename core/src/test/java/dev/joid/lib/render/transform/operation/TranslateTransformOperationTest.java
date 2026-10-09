package dev.joid.lib.render.transform.operation;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.render.transform.Vector;

public class TranslateTransformOperationTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void movesTheMatrixByItsVector() {
		new TranslateTransformOperation(Vector.create(30D, 40D, 5D)).transform();
		final float[] matrix = this.bridges.getRender().getModelView().getMatrix();
		Assert.assertEquals(30F, matrix[12], 1E-4F);
		Assert.assertEquals(40F, matrix[13], 1E-4F);
		Assert.assertEquals(5F, matrix[14], 0F);
	}

	@Test
	public void movesByWholePixels() {
		this.bridges.resize(1366, 768);
		this.bridges.getRender().getProjection().ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
		final double before = this.bridges.getRender().getPixelGrid().toScreenX(0D);
		new TranslateTransformOperation(Vector.create(10.3D, 0D)).transform();
		final double after = this.bridges.getRender().getPixelGrid().toScreenX(0D);
		Assert.assertEquals(Math.rint(10.3D * 1366D / 1920D), after - before, 1E-4D);
	}

	@Test
	public void readsItsVectorOnEveryTransform() {
		final double[] offset = {10D};
		final TranslateTransformOperation operation = new TranslateTransformOperation(Vector.create(() -> offset[0], () -> 0D));
		operation.transform();
		offset[0] = 25D;
		operation.transform();
		final PixelGrid grid = this.bridges.getRender().getPixelGrid();
		Assert.assertEquals(35D, grid.toScreenX(0D), 1E-4D);
	}

	@Test
	public void keepsItsVector() {
		final Vector vector = Vector.create(1D, 2D);
		Assert.assertSame(vector, new TranslateTransformOperation(vector).getVector());
	}

}