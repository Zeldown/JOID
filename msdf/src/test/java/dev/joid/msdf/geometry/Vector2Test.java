package dev.joid.msdf.geometry;

import org.junit.Assert;
import org.junit.Test;

public class Vector2Test {

	@Test
	public void computesTheUsualOperations() {
		final Vector2 a = new Vector2(3D, 4D);
		final Vector2 b = new Vector2(1D, -2D);
		Assert.assertEquals(4D, a.add(b).getX(), 0D);
		Assert.assertEquals(2D, a.add(b).getY(), 0D);
		Assert.assertEquals(2D, a.subtract(b).getX(), 0D);
		Assert.assertEquals(6D, a.subtract(b).getY(), 0D);
		Assert.assertEquals(6D, a.scale(2D).getX(), 0D);
		Assert.assertEquals(-5D, a.dot(b), 0D);
		Assert.assertEquals(-10D, a.cross(b), 0D);
		Assert.assertEquals(5D, a.length(), 0D);
		Assert.assertEquals(0.6D, a.normalize().getX(), 1E-12D);
		Assert.assertEquals(0.8D, a.normalize().getY(), 1E-12D);
	}

}