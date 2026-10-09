package dev.joid.lib.font.impl.bitmap;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.demo.ui.font.effect.DemoFace;
import dev.joid.lib.font.IFontProvider;
import dev.joid.lib.font.impl.glyph.FontFamily;

public class BitmapFontTest {

	@Test
	public void keepsItsBitmapSize() {
		Assert.assertEquals(8, new Font(8).getBitmapSize());
		Assert.assertEquals(16, new Font(16).getBitmapSize());
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAnEmptyBitmapSize() {
		new Font(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesANegativeBitmapSize() {
		new Font(-8);
	}

	private static final class Font extends BitmapFont<DemoFace> {

		private Font(final int bitmapSize) {
			super(FontFamily.of(DemoFace.create(0.5F)), bitmapSize);
		}

		@Override
		public IFontProvider getFontProvider() {
			return null;
		}

	}

}