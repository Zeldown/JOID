package dev.joid.lib.utils.image;

import org.junit.Assert;
import org.junit.Test;

public class ImageUtilsTest {

	@Test
	public void reachesFarAwayPixels() {
		final int[] pixels = new int[64];
		pixels[0] = 0xFF00FF00;
		ImageUtils.bleedAlpha(pixels, 8, 8);
		Assert.assertEquals(0x0000FF00, pixels[63]);
	}

	@Test
	public void neverTouchesVisiblePixels() {
		final int[] pixels = {0xFFFF0000, 0x00000000, 0x01ABCDEF};
		ImageUtils.bleedAlpha(pixels, 3, 1);
		Assert.assertEquals(0xFFFF0000, pixels[0]);
		Assert.assertEquals(0x01ABCDEF, pixels[2]);
		Assert.assertEquals(0, pixels[1] >>> 24);
	}

	@Test
	public void leavesFullyTransparentImagesAlone() {
		final int[] pixels = {0x00000000, 0x00FFFFFF};
		ImageUtils.bleedAlpha(pixels, 2, 1);
		Assert.assertArrayEquals(new int[] {0x00000000, 0x00FFFFFF}, pixels);
	}

	@Test
	public void givesTransparentPixelsTheNearestColor() {
		final int[] pixels = {0xFFFF0000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x80123456};
		ImageUtils.bleedAlpha(pixels, 6, 1);
		Assert.assertEquals(0x00FF0000, pixels[1]);
		Assert.assertEquals(0x00FF0000, pixels[2]);
		Assert.assertEquals(0x00123456, pixels[3]);
		Assert.assertEquals(0x00123456, pixels[4]);
	}

}