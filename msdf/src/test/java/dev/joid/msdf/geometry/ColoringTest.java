package dev.joid.msdf.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ColoringTest {

	@Test
	public void givesAdjacentEdgesOfACornerDifferentColors() {
		final Shape shape = new Shape();
		final List<Edge> square = new ArrayList<>();
		final Vector2[] points = {new Vector2(0D, 0D), new Vector2(1D, 0D), new Vector2(1D, 1D), new Vector2(0D, 1D)};
		for (int i = 0; i < points.length; i++) {
			square.add(new Edge(Arrays.asList(points[i], points[(i + 1) % points.length])));
		}
		shape.add(square);
		Coloring.apply(shape, 3D);

		for (int i = 0; i < square.size(); i++) {
			final int color = square.get(i).getColor();
			Assert.assertTrue(color == 3 || color == 5 || color == 6);
			Assert.assertNotEquals(color, square.get((i + 1) % square.size()).getColor());
		}
	}

	@Test
	public void keepsSmoothContoursWhite() {
		final Shape shape = new Shape();
		final List<Edge> circle = new ArrayList<>();
		final int segments = 128;
		for (int i = 0; i < segments; i++) {
			final double from = Math.PI * 2D * i / segments;
			final double to = Math.PI * 2D * (i + 1) / segments;
			circle.add(new Edge(Arrays.asList(new Vector2(Math.cos(from), Math.sin(from)), new Vector2(Math.cos(to), Math.sin(to)))));
		}
		shape.add(circle);
		Coloring.apply(shape, 3D);

		for (final Edge edge : circle) {
			Assert.assertEquals(7, edge.getColor());
		}
	}

}