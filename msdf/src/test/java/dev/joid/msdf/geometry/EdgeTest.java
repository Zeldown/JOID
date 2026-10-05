package dev.joid.msdf.geometry;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class EdgeTest {

	@Test
	public void exposesItsEndsAndDirections() {
		final Edge edge = new Edge(Arrays.asList(new Vector2(0D, 0D), new Vector2(2D, 0D)));
		Assert.assertEquals(0D, edge.start().getX(), 0D);
		Assert.assertEquals(2D, edge.end().getX(), 0D);
		Assert.assertEquals(1D, edge.startDirection().normalize().getX(), 1E-12D);
		Assert.assertEquals(1D, edge.endDirection().normalize().getX(), 1E-12D);
		Assert.assertEquals(7, edge.getColor());
	}

	@Test
	public void measuresTheDistanceToTheSegment() {
		final Edge edge = new Edge(Arrays.asList(new Vector2(0D, 0D), new Vector2(2D, 0D)));
		final SignedDistance above = new SignedDistance();
		edge.distance(1D, 1D, above);
		final SignedDistance below = new SignedDistance();
		edge.distance(1D, -1D, below);
		Assert.assertEquals(1D, Math.abs(above.getDistance()), 1E-12D);
		Assert.assertEquals(1D, Math.abs(below.getDistance()), 1E-12D);
		Assert.assertNotEquals(Math.signum(above.getDistance()), Math.signum(below.getDistance()), 0D);

		final SignedDistance beyond = new SignedDistance();
		edge.distance(3D, 0D, beyond);
		Assert.assertEquals(1D, Math.abs(beyond.getDistance()), 1E-12D);
	}

	@Test
	public void extendsTheSegmentForThePseudoDistance() {
		final Edge edge = new Edge(Arrays.asList(new Vector2(0D, 0D), new Vector2(2D, 0D)));
		final SignedDistance beyond = new SignedDistance();
		edge.distance(3D, 1D, beyond);
		edge.pseudoDistance(3D, 1D, beyond);
		Assert.assertEquals(1D, Math.abs(beyond.getDistance()), 1E-12D);
	}

}