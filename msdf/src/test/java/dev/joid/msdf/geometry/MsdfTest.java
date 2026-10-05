package dev.joid.msdf.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class MsdfTest {

	@Test
	public void encodesTheInsideAboveTheMiddleAndTheOutsideBelow() {
		final Shape shape = new Shape();
		final List<Edge> square = new ArrayList<>();
		final Vector2[] points = {new Vector2(0.25D, 0.25D), new Vector2(0.75D, 0.25D), new Vector2(0.75D, 0.75D), new Vector2(0.25D, 0.75D)};
		for (int i = 0; i < points.length; i++) {
			square.add(new Edge(Arrays.asList(points[i], points[(i + 1) % points.length])));
		}
		shape.add(square);
		shape.orient();
		Coloring.apply(shape, 3D);

		final int[] field = Msdf.generate(shape, 16, 16, 16D, 0D, 1D, 4D);
		Assert.assertEquals(256, field.length);
		Assert.assertTrue(MsdfTest.median(field[8 + 8 * 16]) > 127);
		Assert.assertTrue(MsdfTest.median(field[0]) < 127);
		Assert.assertTrue(MsdfTest.median(field[15 + 15 * 16]) < 127);
	}

	private static int median(final int pixel) {
		final int r = pixel >> 16 & 0xFF;
		final int g = pixel >> 8 & 0xFF;
		final int b = pixel & 0xFF;
		return Math.max(Math.min(r, g), Math.min(Math.max(r, g), b));
	}

}