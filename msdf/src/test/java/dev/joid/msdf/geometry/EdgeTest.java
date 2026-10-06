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

	@Test
	public void measuresAPolylineAlongItsSegments() {
		final Edge edge = new Edge(Arrays.asList(new Vector2(0D, 0D), new Vector2(3D, 0D), new Vector2(3D, 4D)));
		Assert.assertArrayEquals(new double[] {3D, 4D}, edge.getLengths(), 0D);
		Assert.assertEquals(7D, edge.getTotal(), 0D);
		Assert.assertEquals(1D, edge.endDirection().normalize().getY(), 1E-12D);
	}

	@Test
	public void keepsADegenerateEdgeInfinitelyFar() {
		final Edge edge = new Edge(Arrays.asList(new Vector2(1D, 1D), new Vector2(1D, 1D)));
		final SignedDistance distance = new SignedDistance();
		edge.distance(0D, 0D, distance);
		Assert.assertEquals(-Double.MAX_VALUE, distance.getDistance(), 0D);
		Assert.assertEquals(0D, edge.getTotal(), 0D);
	}

	@Test
	public void keepsTheClosestSegmentOfAPolyline() {
		final Edge edge = new Edge(Arrays.asList(new Vector2(0D, 0D), new Vector2(2D, 0D), new Vector2(2D, 2D)));
		final SignedDistance distance = new SignedDistance();
		edge.distance(1D, -1D, distance);
		Assert.assertEquals(1D, distance.getDistance(), 1E-12D);
		Assert.assertEquals(0D, distance.getOrthogonality(), 0D);
		Assert.assertEquals(0, distance.getSide());
	}

	@Test
	public void prefersTheMostOrthogonalSegmentAtAJoint() {
		final Edge edge = new Edge(Arrays.asList(new Vector2(0D, 0D), new Vector2(1D, 0D), new Vector2(1D, 1D)));
		final SignedDistance tilted = new SignedDistance();
		edge.distance(2D, -0.5D, tilted);
		Assert.assertEquals(Math.sqrt(1.25D), Math.abs(tilted.getDistance()), 1E-12D);
		Assert.assertEquals(0.5D / Math.sqrt(1.25D), tilted.getOrthogonality(), 1E-12D);

		final SignedDistance diagonal = new SignedDistance();
		edge.distance(2D, -1D, diagonal);
		Assert.assertEquals(Math.sqrt(0.5D), diagonal.getOrthogonality(), 1E-12D);
	}

	@Test
	public void flagsTheEndsOfAPolyline() {
		final Edge edge = new Edge(Arrays.asList(new Vector2(0D, 0D), new Vector2(1D, 0D), new Vector2(2D, 0D)));
		final SignedDistance before = new SignedDistance();
		edge.distance(-1D, 0.5D, before);
		final SignedDistance between = new SignedDistance();
		edge.distance(1D, 0.5D, between);
		final SignedDistance after = new SignedDistance();
		edge.distance(3D, 0.5D, after);
		Assert.assertEquals(-1, before.getSide());
		Assert.assertEquals(0, between.getSide());
		Assert.assertEquals(1, after.getSide());
	}

	@Test
	public void extendsTheSegmentBeforeItsStart() {
		final Edge edge = new Edge(Arrays.asList(new Vector2(0D, 0D), new Vector2(2D, 0D)));
		final SignedDistance before = new SignedDistance();
		edge.distance(-1D, 1D, before);
		Assert.assertEquals(-Math.sqrt(2D), before.getDistance(), 1E-12D);
		edge.pseudoDistance(-1D, 1D, before);
		Assert.assertEquals(-1D, before.getDistance(), 1E-12D);
		Assert.assertEquals(-1, before.getSide());
	}

	@Test
	public void extendsTheSegmentOnlyOutward() {
		final Edge edge = new Edge(Arrays.asList(new Vector2(0D, 0D), new Vector2(2D, 0D)));
		final SignedDistance start = new SignedDistance();
		start.set(5D, 1D, -1);
		edge.pseudoDistance(1D, 1D, start);
		final SignedDistance end = new SignedDistance();
		end.set(5D, 1D, 1);
		edge.pseudoDistance(1D, 1D, end);
		Assert.assertEquals(5D, start.getDistance(), 0D);
		Assert.assertEquals(5D, end.getDistance(), 0D);
	}

	@Test
	public void neverMovesAwayWithThePseudoDistance() {
		final Edge edge = new Edge(Arrays.asList(new Vector2(0D, 0D), new Vector2(2D, 0D)));
		final SignedDistance distance = new SignedDistance();
		distance.set(0.5D, 1D, 1);
		edge.pseudoDistance(3D, 1D, distance);
		Assert.assertEquals(0.5D, distance.getDistance(), 0D);
	}

}