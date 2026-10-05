package dev.joid.lib.utils.bezier;

import javax.vecmath.Vector2d;

import org.junit.Assert;
import org.junit.Test;

public class BezierTest {

	@Test
	public void runsAQuadraticCurveFromStartToEnd() {
		final Vector2d start = new Vector2d(0D, 0D);
		final Vector2d end = new Vector2d(4D, 0D);
		final Vector2d control = new Vector2d(2D, 4D);
		Assert.assertEquals(start, Bezier.quadratic(0D, start, end, control));
		Assert.assertEquals(end, Bezier.quadratic(1D, start, end, control));
	}

	@Test
	public void pullsAQuadraticCurveTowardsItsControl() {
		final Vector2d start = new Vector2d(0D, 0D);
		final Vector2d end = new Vector2d(4D, 0D);
		final Vector2d control = new Vector2d(2D, 4D);
		Assert.assertEquals(new Vector2d(2D, 2D), Bezier.quadratic(0.5D, start, end, control));
		Assert.assertEquals(new Vector2d(1D, 1.5D), Bezier.quadratic(0.25D, start, end, control));
	}

	@Test
	public void runsACubicCurveFromStartToEnd() {
		final Vector2d start = new Vector2d(0D, 0D);
		final Vector2d end = new Vector2d(4D, 4D);
		Assert.assertEquals(start, Bezier.cubic(0D, start, new Vector2d(0D, 4D), end, new Vector2d(4D, 0D)));
		Assert.assertEquals(end, Bezier.cubic(1D, start, new Vector2d(0D, 4D), end, new Vector2d(4D, 0D)));
	}

	@Test
	public void pullsACubicCurveTowardsEachControl() {
		final Vector2d start = new Vector2d(0D, 0D);
		final Vector2d end = new Vector2d(4D, 4D);
		final Vector2d middle = Bezier.cubic(0.5D, start, new Vector2d(0D, 4D), end, new Vector2d(4D, 0D));
		Assert.assertEquals(2D, middle.x, 0D);
		Assert.assertEquals(2D, middle.y, 0D);
		final Vector2d early = Bezier.cubic(0.25D, start, new Vector2d(0D, 4D), end, new Vector2d(4D, 0D));
		Assert.assertEquals(0.625D, early.x, 0.0001D);
		Assert.assertEquals(1.75D, early.y, 0.0001D);
	}

}