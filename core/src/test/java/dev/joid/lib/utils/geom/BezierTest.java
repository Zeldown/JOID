package dev.joid.lib.utils.geom;

import org.junit.Assert;
import org.junit.Test;

public class BezierTest {

	@Test
	public void runsAQuadraticCurveFromStartToEnd() {
		final Point start = Point.create(0D, 0D);
		final Point end = Point.create(4D, 0D);
		final Point control = Point.create(2D, 4D);
		Assert.assertEquals(start, Bezier.quadratic(0D, start, control, end));
		Assert.assertEquals(end, Bezier.quadratic(1D, start, control, end));
	}

	@Test
	public void pullsAQuadraticCurveTowardsItsControl() {
		final Point start = Point.create(0D, 0D);
		final Point end = Point.create(4D, 0D);
		final Point control = Point.create(2D, 4D);
		Assert.assertEquals(Point.create(2D, 2D), Bezier.quadratic(0.5D, start, control, end));
		Assert.assertEquals(Point.create(1D, 1.5D), Bezier.quadratic(0.25D, start, control, end));
	}

	@Test
	public void runsACubicCurveFromStartToEnd() {
		final Point start = Point.create(0D, 0D);
		final Point end = Point.create(4D, 4D);
		Assert.assertEquals(start, Bezier.cubic(0D, start, Point.create(0D, 4D), Point.create(4D, 0D), end));
		Assert.assertEquals(end, Bezier.cubic(1D, start, Point.create(0D, 4D), Point.create(4D, 0D), end));
	}

	@Test
	public void pullsACubicCurveTowardsEachControl() {
		final Point start = Point.create(0D, 0D);
		final Point end = Point.create(4D, 4D);
		final Point middle = Bezier.cubic(0.5D, start, Point.create(0D, 4D), Point.create(4D, 0D), end);
		Assert.assertEquals(2D, middle.getX(), 0D);
		Assert.assertEquals(2D, middle.getY(), 0D);
		final Point early = Bezier.cubic(0.25D, start, Point.create(0D, 4D), Point.create(4D, 0D), end);
		Assert.assertEquals(0.625D, early.getX(), 0.0001D);
		Assert.assertEquals(1.75D, early.getY(), 0.0001D);
	}

}