package dev.joid.lib.render.transform;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.render.modifier.Rotation;
import dev.joid.lib.render.modifier.Scale;
import dev.joid.lib.render.modifier.Vector;
import dev.joid.lib.render.transform.operation.ScaleOperation;
import dev.joid.lib.render.transform.operation.TranslateOperation;

public class TransformationTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Before
	public void useAFractionalScale() {
		this.bridges.resize(1366, 768);
		this.bridges.getRender().ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
	}

	@Test
	public void restoresTheMatrixOnReset() {
		final double before = this.bridges.getRender().getPixelGrid().toScreenX(0D);
		final Transformation transformation = Transformation.create().translate(Vector.create(10.3D, 4.6D)).scale(Scale.create(1.5D, 1.5D, 1D), Vector.create(40D, 40D));
		transformation.apply();
		Assert.assertNotEquals(before, this.bridges.getRender().getPixelGrid().toScreenX(0D), 1E-4D);
		transformation.reset();
		Assert.assertEquals(before, this.bridges.getRender().getPixelGrid().toScreenX(0D), 0D);
	}

	@Test
	public void movesByWholePixels() {
		final Transformation transformation = Transformation.create().translate(Vector.create(10.3D, -4.6D));
		transformation.apply();
		try {
			final double screenX = this.bridges.getRender().getPixelGrid().toScreenX(0D);
			final double screenY = this.bridges.getRender().getPixelGrid().toScreenY(0D);
			Assert.assertEquals(Math.rint(screenX), screenX, 1E-4D);
			Assert.assertEquals(Math.rint(screenY), screenY, 1E-4D);
			Assert.assertEquals(10.3D * 1366D / 1920D, screenX, 0.5D + 1E-4D);
		} finally {
			transformation.reset();
		}
	}

	@Test
	public void restoresTheMatrixEvenWhenTheDrawingThrows() {
		final double before = this.bridges.getRender().getPixelGrid().toScreenX(0D);
		try {
			Transformation.create().translate(Vector.create(50D, 0D)).apply(() -> {
				throw new IllegalStateException("Draw failed");
			});
			Assert.fail();
		} catch (final IllegalStateException exception) {
			Assert.assertEquals(before, this.bridges.getRender().getPixelGrid().toScreenX(0D), 0D);
		}
	}

	@Test
	public void appliesItsOperationsInOrder() {
		final Transformation transformation = Transformation.create(new TranslateOperation(Vector.create(10D, 0D))).add(new ScaleOperation(Scale.create(2D, 2D, 1D), Vector.create()));
		Assert.assertEquals(2, transformation.getOperations().size());
		transformation.apply();
		try {
			final float[] matrix = this.bridges.getRender().getModelView().getMatrix();
			Assert.assertEquals(2D, matrix[0], 1E-6D);
			Assert.assertEquals(10D, matrix[12], 0.5D);
		} finally {
			transformation.reset();
		}
	}

	@Test
	public void rotatesAroundItsPivot() {
		final Transformation transformation = Transformation.create().rotate(180D, Rotation.YAW, Vector.create(100D, 100D));
		transformation.apply();
		try {
			final float[] matrix = this.bridges.getRender().getModelView().getMatrix();
			Assert.assertEquals(100D, matrix[0] * 100D + matrix[4] * 100D + matrix[12], 1E-3D);
			Assert.assertEquals(0D, matrix[0] * 200D + matrix[4] * 100D + matrix[12], 1E-3D);
			Assert.assertEquals(100D, matrix[1] * 200D + matrix[5] * 100D + matrix[13], 1E-3D);
		} finally {
			transformation.reset();
		}
	}

	@Test
	public void drawsInsideItsTransformationOnly() {
		final double[] inside = new double[1];
		Transformation.create().translate(Vector.create(50D, 0D)).apply(() -> inside[0] = this.bridges.getRender().getModelView().getMatrix()[12]);
		Assert.assertEquals(50D, inside[0], 1D);
		Assert.assertEquals(0F, this.bridges.getRender().getModelView().getMatrix()[12], 0F);
	}

	@Test
	public void forgetsItsOperationsOnceCleared() {
		final Transformation transformation = Transformation.create().translate(Vector.create(50D, 0D));
		transformation.clear();
		Assert.assertTrue(transformation.getOperations().isEmpty());
		transformation.apply();
		try {
			Assert.assertEquals(0F, this.bridges.getRender().getModelView().getMatrix()[12], 0F);
		} finally {
			transformation.reset();
		}
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingOperation() {
		Transformation.create().add(null);
	}

}