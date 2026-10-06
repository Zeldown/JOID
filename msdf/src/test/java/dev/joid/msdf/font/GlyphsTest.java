package dev.joid.msdf.font;

import java.awt.Font;
import java.awt.FontFormatException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.msdf.MsdfFonts;
import dev.joid.msdf.geometry.Edge;
import dev.joid.msdf.geometry.Shape;

public class GlyphsTest {

	@Test
	public void mapsCodepointsToGlyphs() throws Exception {
		final Font font = Glyphs.load(MsdfFonts.read(MsdfFonts.REGULAR));
		Assert.assertNotEquals(0, Glyphs.code(font, 'A'));
		Assert.assertEquals(0, Glyphs.code(font, 'Z'));
	}

	@Test
	public void measuresAdvancesAndMetricsInEm() throws Exception {
		final Font font = Glyphs.load(MsdfFonts.read(MsdfFonts.REGULAR));
		Assert.assertEquals(0.6D, Glyphs.advance(font, 'A'), 1E-3D);
		final double[] metrics = Glyphs.metrics(font);
		Assert.assertEquals(5, metrics.length);
		Assert.assertEquals(1D, metrics[0], 1E-3D);
		Assert.assertEquals(0.8D, metrics[1], 1E-3D);
		Assert.assertEquals(-0.2D, metrics[2], 1E-3D);
	}

	@Test
	public void outlinesAGlyphInEm() throws Exception {
		final Font font = Glyphs.load(MsdfFonts.read(MsdfFonts.REGULAR));
		final Shape shape = Glyphs.outline(font, 'x');
		Assert.assertFalse(shape.isEmpty());
		final double[] bounds = shape.bounds();
		Assert.assertEquals(0D, bounds[0], 1E-3D);
		Assert.assertEquals(0D, bounds[1], 1E-3D);
		Assert.assertEquals(0.5D, bounds[2], 1E-3D);
		Assert.assertEquals(0.52D, bounds[3], 1E-3D);
		Assert.assertTrue(Glyphs.outline(font, ' ').isEmpty());
	}

	@Test
	public void flattensTheQuadraticCurvesOfATrueTypeFont() throws Exception {
		final Shape shape = Glyphs.outline(Glyphs.load(MsdfFonts.read(MsdfFonts.QUADRATIC)), 'o');
		Assert.assertEquals(2, shape.getContours().size());
		Assert.assertArrayEquals(new double[] {0.05D, 0.05D, 0.55D, 0.55D}, shape.bounds(), 1E-9D);
		GlyphsTest.assertRing(shape, 1D, 1.07D);
	}

	@Test
	public void flattensTheCubicCurvesOfAnOpenTypeFont() throws Exception {
		final Shape shape = Glyphs.outline(Glyphs.load(MsdfFonts.read(MsdfFonts.CUBIC)), 'o');
		Assert.assertEquals(2, shape.getContours().size());
		Assert.assertArrayEquals(new double[] {0.05D, 0.05D, 0.55D, 0.55D}, shape.bounds(), 1E-9D);
		GlyphsTest.assertRing(shape, 0.999D, 1.001D);
	}

	@Test
	public void mergesTheComponentsOfACompositeGlyph() throws Exception {
		final Font font = Glyphs.load(MsdfFonts.read(MsdfFonts.QUADRATIC));
		final Shape shape = Glyphs.outline(font, 'W');
		Assert.assertEquals(1, shape.getContours().size());
		Assert.assertArrayEquals(new double[] {0D, 0D, 0.9D, 0.7D}, shape.bounds(), 1E-9D);
		Assert.assertEquals(0.9D, Glyphs.advance(font, 'W'), 1E-3D);
	}

	@Test(expected = FontFormatException.class)
	public void refusesDataThatIsNotAFont() throws Exception {
		Glyphs.load(new byte[64]);
	}

	private static void assertRing(final Shape shape, final double inner, final double outer) {
		for (final List<Edge> contour : shape.getContours()) {
			final double radius = Math.hypot(contour.get(0).start().getX() - 0.3D, contour.get(0).start().getY() - 0.3D);
			Assert.assertEquals(4, contour.size());
			for (final Edge edge : contour) {
				Assert.assertTrue(edge.getX().length > 8);
				for (int i = 0; i < edge.getX().length; i++) {
					final double distance = Math.hypot(edge.getX()[i] - 0.3D, edge.getY()[i] - 0.3D);
					Assert.assertTrue(distance >= radius * inner - 1E-9D && distance <= radius * outer);
				}
			}
		}
	}

}