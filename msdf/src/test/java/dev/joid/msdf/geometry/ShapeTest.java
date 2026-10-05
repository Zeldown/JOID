package dev.joid.msdf.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ShapeTest {

	@Test
	public void ignoresEmptyContours() {
		final Shape shape = new Shape();
		shape.add(new ArrayList<>());
		Assert.assertTrue(shape.isEmpty());
	}

	@Test
	public void boundsEveryContour() {
		final Shape shape = new Shape();
		shape.add(ShapeTest.square(0D, 0D, 2D));
		shape.add(ShapeTest.square(-1D, 3D, 1D));
		Assert.assertArrayEquals(new double[] {-1D, 0D, 2D, 4D}, shape.bounds(), 0D);
	}

	@Test
	public void orientsHolesAgainstTheirOutline() {
		final Shape shape = new Shape();
		shape.add(ShapeTest.square(0D, 0D, 4D));
		shape.add(ShapeTest.square(1D, 1D, 2D));
		shape.orient();
		final List<List<Edge>> contours = shape.getContours();
		Assert.assertNotEquals(Math.signum(ShapeTest.area(contours.get(0))), Math.signum(ShapeTest.area(contours.get(1))), 0D);
	}

	private static List<Edge> square(final double x, final double y, final double size) {
		final List<Vector2> points = Arrays.asList(new Vector2(x, y), new Vector2(x + size, y), new Vector2(x + size, y + size), new Vector2(x, y + size));
		final List<Edge> contour = new ArrayList<>();
		for (int i = 0; i < points.size(); i++) {
			contour.add(new Edge(Arrays.asList(points.get(i), points.get((i + 1) % points.size()))));
		}
		return contour;
	}

	private static double area(final List<Edge> contour) {
		double area = 0D;
		for (final Edge edge : contour) {
			area += edge.start().cross(edge.end());
		}
		return area;
	}

}