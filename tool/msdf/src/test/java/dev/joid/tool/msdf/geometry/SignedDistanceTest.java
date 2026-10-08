package dev.joid.tool.msdf.geometry;

import org.junit.Assert;
import org.junit.Test;

public class SignedDistanceTest {

	@Test
	public void startsInfinitelyFar() {
		final SignedDistance distance = new SignedDistance();
		Assert.assertEquals(-Double.MAX_VALUE, distance.getDistance(), 0D);
		Assert.assertEquals(1D, distance.getOrthogonality(), 0D);
		Assert.assertNull(distance.getEdge());
	}

	@Test
	public void comparesTheAbsoluteDistanceThenTheOrthogonality() {
		final SignedDistance near = new SignedDistance();
		near.set(-1D, 0.5D, 1);
		final SignedDistance far = new SignedDistance();
		far.set(2D, 0D, 1);
		Assert.assertTrue(near.closerThan(far));
		Assert.assertFalse(far.closerThan(near));

		final SignedDistance straighter = new SignedDistance();
		straighter.set(1D, 0.2D, 1);
		Assert.assertTrue(straighter.closerThan(near));
	}

	@Test
	public void copiesAndResets() {
		final SignedDistance source = new SignedDistance();
		source.set(3D, 0.1D, -1);
		final SignedDistance copy = new SignedDistance();
		copy.copy(source);
		Assert.assertEquals(3D, copy.getDistance(), 0D);
		Assert.assertEquals(-1, copy.getSide());
		copy.reset();
		Assert.assertEquals(0, copy.getSide());
	}

}