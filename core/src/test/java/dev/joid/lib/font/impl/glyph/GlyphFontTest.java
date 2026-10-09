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