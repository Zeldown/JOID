package dev.joid.tool.msdf.atlas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.tool.msdf.geometry.Edge;
import dev.joid.tool.msdf.geometry.Shape;
import dev.joid.tool.msdf.geometry.Vector2;

public class GlyphEntryTest {

	@Test
	public void isNotDrawableWithoutAShape() {
		final GlyphEntry glyph = new GlyphEntry(' ', 0.25D, null);
		Assert.assertFalse(glyph.isDrawable());
		Assert.assertNull(glyph.getBounds());
		Assert.assertEquals(' ', glyph.getCodepoint());
		Assert.assertEquals(0.25D, glyph.getAdvance(), 0D);
	}

	@Test
	public void boundsItsOutline() {
		final GlyphEntry glyph = new GlyphEntry('A', 1D, GlyphEntryTest.square(0.5D));
		Assert.assertTrue(glyph.isDrawable());
		Assert.assertArrayEquals(new double[] {0D, 0D, 0.5D, 0.5D}, glyph.getBounds(), 0D);
	}

	@Test
	public void measuresItsCellAroundTheFieldMargin() {
		final GlyphEntry glyph = new GlyphEntry('A', 1D, GlyphEntryTest.square(1D));
		glyph.measure(16D, 2D);
		Assert.assertEquals(18, glyph.getWidth());
		Assert.assertEquals(18, glyph.getHeight());
		Assert.assertEquals(-0.0625D, glyph.getLeft(), 0D);
		Assert.assertEquals(-0.0625D, glyph.getBottom(), 0D);
		Assert.assertEquals(1.0625D, glyph.getRight(), 0D);
		Assert.assertEquals(1.0625D, glyph.getTop(), 0D);
	}

	@Test
	public void growsItsCellToWholePixels() {
		final GlyphEntry glyph = new GlyphEntry('A', 1D, GlyphEntryTest.square(0.5D));
		glyph.measure(3D, 0.75D);
		Assert.assertEquals(3, glyph.getWidth());
		Assert.assertEquals(-0.125D, glyph.getLeft(), 0D);
		Assert.assertEquals(0.875D, glyph.getRight(), 0D);
		Assert.assertEquals(0.875D, glyph.getTop(), 0D);
	}

	private static Shape square(final double size) {
		final List<Vector2> points = Arrays.asList(new Vector2(0D, 0D), new Vector2(size, 0D), new Vector2(size, size), new Vector2(0D, size));
		final List<Edge> contour = new ArrayList<>();
		for (int i = 0; i < points.size(); i++) {
			contour.add(new Edge(Arrays.asList(points.get(i), points.get((i + 1) % points.size()))));
		}

		final Shape shape = new Shape();
		shape.add(contour);
		return shape;
	}

}