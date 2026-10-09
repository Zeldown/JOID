package dev.joid.lib.font.impl.glyph;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.demo.ui.font.effect.DemoFace;
import dev.joid.lib.font.IFontProvider;
import dev.joid.lib.font.impl.glyph.dto.FontFamily;

public class GlyphFontTest {

	@Test
	public void isNotABitmapByDefault() {
		final Font font = new Font(0);
		Assert.assertFalse(font.isBitmap());
		Assert.assertEquals(0, font.getBitmapSize());
		Assert.assertTrue(new Font(8).isBitmap());
		Assert.assertEquals(8, new Font(8).getBitmapSize());
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesANegativeBitmapSize() {
		new Font(-8);
	}

	@Test
	public void keepsTheSizeOfAVectorFont() {
		final Font font = new Font(0);
		Assert.assertEquals(13F, font.snapSize(13F, 0.37D), 0F);
		Assert.assertEquals(24F, font.snapSize(24F, 0.25D), 0F);
	}

	@Test
	public void drawsATexelSmallerThanAPixelOnOnePixel() {
		final Font font = new Font(8);
		Assert.assertEquals(32D, font.snapSize(8F, 0.25D), 1E-4D);
		Assert.assertEquals(32D, font.snapSize(24F, 0.25D), 1E-4D);
		Assert.assertEquals(8D / 0.26D, font.snapSize(24F, 0.25D * 1.04D), 1E-4D);
		Assert.assertEquals(80D, font.snapSize(1F, 0.1D), 1E-4D);
	}

	@Test
	public void roundsAFractionalTexelToTheNearestPixel() {
		final Font font = new Font(8);
		Assert.assertEquals(8D, font.snapSize(10F, 1D), 1E-4D);
		Assert.assertEquals(16D, font.snapSize(12F, 1D), 1E-4D);
		Assert.assertEquals(16D, font.snapSize(19F, 1D), 1E-4D);
		Assert.assertEquals(24D, font.snapSize(20F, 1D), 1E-4D);
		Assert.assertEquals(40D / 1.5D, font.snapSize(24F, 1.5D), 1E-4D);
	}

	@Test
	public void keepsASizeThatIsAlreadyAWholeNumberOfPixels() {
		final Font font = new Font(8);
		Assert.assertEquals(8F, font.snapSize(8F, 1D), 0F);
		Assert.assertEquals(16F, font.snapSize(16F, 1D), 0F);
		Assert.assertEquals(24F, font.snapSize(24F, 2D), 0F);
		Assert.assertEquals(16F, font.snapSize(16F, 0.5D), 0F);
	}

	@Test
	public void drawsAWholeNumberOfPixelsPerTexelAtEveryScale() {
		final Font font = new Font(8);
		final double[] interfaceScales = {0.25D, 0.5D, 0.75D, 1D, 2D, 3D};
		final double[] fits = {1D, 1280D / 1920D, 1366D / 1920D, 1.04D};
		final double[] zooms = {1D, 1.1D, 1.5D};
		for (final double interfaceScale : interfaceScales) {
			for (final double fit : fits) {
				for (final double zoom : zooms) {
					final double scale = interfaceScale * fit * zoom;
					for (final float size : new float[] {6F, 8F, 9F, 12F, 16F, 24F, 33F}) {
						final double texel = font.snapSize(size, scale) * scale / 8D;
						final String label = "size " + size + " at " + scale;
						Assert.assertEquals(label, Math.rint(texel), texel, 1E-4D);
						Assert.assertTrue(label, texel >= 1D - 1E-4D);
						Assert.assertEquals(label, Math.max(1D, Math.floor(size * scale / 8D + 0.5D + 1E-6D)), texel, 1E-4D);
					}
				}
			}
		}
	}

	@Test
	public void keepsAnEmptySize() {
		Assert.assertEquals(0F, new Font(8).snapSize(0F, 0.25D), 0F);
	}

	@Test
	public void keepsTheSizeWithoutAScale() {
		Assert.assertEquals(12F, new Font(8).snapSize(12F, 0D), 0F);
	}

	private static final class Font extends GlyphFont<DemoFace> {

		private Font(final int bitmapSize) {
			super(FontFamily.of(DemoFace.create(0.5F)), bitmapSize);
		}

		@Override
		public IFontProvider getFontProvider() {
			return null;
		}

	}

}