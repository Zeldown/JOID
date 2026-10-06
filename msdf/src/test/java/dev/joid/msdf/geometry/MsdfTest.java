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

	@Test
	public void trustsTheTrueDistanceWhereTheChannelsDisagree() {
		final int[] field = Msdf.generate(MsdfTest.square(7, 0), 16, 16, 16D, 0D, 1D, 4D);
		Assert.assertTrue(MsdfTest.median(field[8 + 8 * 16]) > 127);
		Assert.assertEquals(0xFF202020, field[13 + 8 * 16]);
		Assert.assertEquals(0xFF000000, field[14 + 8 * 16]);
	}

	@Test
	public void saturatesAChannelThatNoEdgeFeeds() {
		final int[] field = Msdf.generate(MsdfTest.square(3, 3), 16, 16, 16D, 0D, 1D, 4D);
		for (final int pixel : field) {
			Assert.assertEquals(255, pixel & 0xFF);
			Assert.assertEquals(pixel >> 16 & 0xFF, pixel >> 8 & 0xFF);
		}
		Assert.assertTrue(MsdfTest.median(field[8 + 8 * 16]) > 127);
		Assert.assertTrue(MsdfTest.median(field[0]) < 127);
	}

	private static Shape square(final int color, final int right) {
		final Vector2[] points = {new Vector2(0.25D, 0.25D), new Vector2(0.75D, 0.25D), new Vector2(0.75D, 0.75D), new Vector2(0.25D, 0.75D)};
		final List<Edge> square = new ArrayList<>();
		for (int i = 0; i < points.length; i++) {
			final Edge edge = new Edge(Arrays.asList(points[i], points[(i + 1) % points.length]));
			edge.setColor(i == 1 ? right : color);
			square.add(edge);
		}

		final Shape shape = new Shape();
		shape.add(square);
		shape.orient();
		return shape;
	}

	private static int median(final int pixel) {
		final int r = pixel >> 16 & 0xFF;
		final int g = pixel >> 8 & 0xFF;
		final int b = pixel & 0xFF;
		return Math.max(Math.min(r, g), Math.min(Math.max(r, g), b));
	}

}