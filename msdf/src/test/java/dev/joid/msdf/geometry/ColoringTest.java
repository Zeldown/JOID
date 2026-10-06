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

	@Test
	public void spreadsTheThreeColorsAlongATeardrop() {
		final Shape shape = new Shape();
		final List<Vector2> arc = new ArrayList<>();
		for (int i = 0; i <= 64; i++) {
			final double angle = -Math.PI / 4D + Math.PI * 1.5D * i / 64D;
			arc.add(new Vector2(Math.cos(angle), Math.sqrt(2D) + Math.sin(angle)));
		}
		final List<Edge> teardrop = new ArrayList<>(Arrays.asList(new Edge(Arrays.asList(new Vector2(0D, 0D), arc.get(0))), new Edge(arc), new Edge(Arrays.asList(arc.get(64), new Vector2(0D, 0D)))));
		shape.add(teardrop);
		Coloring.apply(shape, 3D);

		Assert.assertEquals(3, teardrop.get(0).getColor());
		Assert.assertEquals(5, teardrop.get(1).getColor());
		Assert.assertEquals(6, teardrop.get(2).getColor());
	}

	@Test
	public void colorsEachSplineOfAHalfDisc() {
		final Shape shape = new Shape();
		final List<Edge> half = new ArrayList<>();
		half.add(new Edge(Arrays.asList(new Vector2(-1D, 0D), new Vector2(1D, 0D))));
		for (int i = 0; i < 32; i++) {
			final double from = Math.PI * i / 32D;
			final double to = Math.PI * (i + 1) / 32D;
			half.add(new Edge(Arrays.asList(new Vector2(Math.cos(from), Math.sin(from)), new Vector2(Math.cos(to), Math.sin(to)))));
		}
		shape.add(half);
		Coloring.apply(shape, 3D);

		Assert.assertEquals(3, half.get(0).getColor());
		for (int i = 1; i < half.size(); i++) {
			Assert.assertEquals(5, half.get(i).getColor());
		}
	}

}