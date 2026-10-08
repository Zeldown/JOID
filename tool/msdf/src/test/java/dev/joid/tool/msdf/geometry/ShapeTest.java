package dev.joid.tool.msdf.geometry;

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

	@Test
	public void turnsAClockwiseOutlineCounterClockwise() {
		final Shape shape = new Shape();
		final List<Edge> outline = ShapeTest.clockwise(0D, 0D, 2D);
		outline.forEach(edge -> edge.setColor(3));
		shape.add(outline);
		shape.orient();
		final List<Edge> oriented = shape.getContours().get(0);
		Assert.assertNotSame(outline, oriented);
		Assert.assertTrue(ShapeTest.area(oriented) > 0D);
		for (final Edge edge : oriented) {
			Assert.assertEquals(3, edge.getColor());
		}
	}

	@Test
	public void keepsAClockwiseHoleInsideItsOutline() {
		final Shape shape = new Shape();
		final List<Edge> outline = ShapeTest.square(0D, 0D, 4D);
		final List<Edge> hole = ShapeTest.clockwise(1D, 1D, 2D);
		shape.add(outline);
		shape.add(hole);
		shape.orient();
		Assert.assertSame(outline, shape.getContours().get(0));
		Assert.assertSame(hole, shape.getContours().get(1));
	}

	@Test
	public void keepsSideBySideOutlinesApart() {
		final Shape shape = new Shape();
		final List<Edge> left = ShapeTest.square(0D, 0D, 2D);
		final List<Edge> right = ShapeTest.square(3D, -1D, 2D);
		shape.add(left);
		shape.add(right);
		shape.orient();
		Assert.assertSame(left, shape.getContours().get(0));
		Assert.assertSame(right, shape.getContours().get(1));
	}

	private static List<Edge> square(final double x, final double y, final double size) {
		final List<Vector2> points = Arrays.asList(new Vector2(x, y), new Vector2(x + size, y), new Vector2(x + size, y + size), new Vector2(x, y + size));
		final List<Edge> contour = new ArrayList<>();
		for (int i = 0; i < points.size(); i++) {
			contour.add(new Edge(Arrays.asList(points.get(i), points.get((i + 1) % points.size()))));
		}
		return contour;
	}

	private static List<Edge> clockwise(final double x, final double y, final double size) {
		final List<Vector2> points = Arrays.asList(new Vector2(x, y), new Vector2(x, y + size), new Vector2(x + size, y + size), new Vector2(x + size, y));
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