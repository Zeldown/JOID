package dev.joid.lib.utils.bezier;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.utils.point.Point;

public class BezierTest {

	@Test
	public void runsAQuadraticCurveFromStartToEnd() {
		final Point start = Point.create(0D, 0D);
		final Point end = Point.create(4D, 0D);
		final Point control = Point.create(2D, 4D);
		Assert.assertEquals(start, Bezier.quadratic(0D, start, end, control));
		Assert.assertEquals(end, Bezier.quadratic(1D, start, end, control));
	}

	@Test
	public void pullsAQuadraticCurveTowardsItsControl() {
		final Point start = Point.create(0D, 0D);
		final Point end = Point.create(4D, 0D);
		final Point control = Point.create(2D, 4D);
		Assert.assertEquals(Point.create(2D, 2D), Bezier.quadratic(0.5D, start, end, control));
		Assert.assertEquals(Point.create(1D, 1.5D), Bezier.quadratic(0.25D, start, end, control));
	}

	@Test
	public void runsACubicCurveFromStartToEnd() {
		final Point start = Point.create(0D, 0D);
		final Point end = Point.create(4D, 4D);
		Assert.assertEquals(start, Bezier.cubic(0D, start, Point.create(0D, 4D), end, Point.create(4D, 0D)));
		Assert.assertEquals(end, Bezier.cubic(1D, start, Point.create(0D, 4D), end, Point.create(4D, 0D)));
	}

	@Test
	public void pullsACubicCurveTowardsEachControl() {
		final Point start = Point.create(0D, 0D);
		final Point end = Point.create(4D, 4D);
		final Point middle = Bezier.cubic(0.5D, start, Point.create(0D, 4D), end, Point.create(4D, 0D));
		Assert.assertEquals(2D, middle.getX(), 0D);
		Assert.assertEquals(2D, middle.getY(), 0D);
		final Point early = Bezier.cubic(0.25D, start, Point.create(0D, 4D), end, Point.create(4D, 0D));
		Assert.assertEquals(0.625D, early.getX(), 0.0001D);
		Assert.assertEquals(1.75D, early.getY(), 0.0001D);
	}

}