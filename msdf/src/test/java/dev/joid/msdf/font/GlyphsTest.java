package dev.joid.msdf.font;

import java.awt.Font;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.msdf.MsdfFonts;
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

}