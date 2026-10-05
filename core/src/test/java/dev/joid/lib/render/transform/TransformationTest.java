package dev.joid.lib.render.transform;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.render.modifier.Scale;
import dev.joid.lib.render.modifier.Vector;

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

}