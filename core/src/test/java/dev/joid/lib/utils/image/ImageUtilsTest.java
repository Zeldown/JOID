package dev.joid.lib.utils.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.spi.ImageReaderSpi;

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

	@Test
	public void leavesFullyVisibleImagesAlone() {
		final int[] pixels = {0xFFFF0000, 0x80123456};
		ImageUtils.bleedAlpha(pixels, 2, 1);
		Assert.assertArrayEquals(new int[] {0xFFFF0000, 0x80123456}, pixels);
	}

	@Test
	public void bleedsAcrossRows() {
		final int[] pixels = {0x00000000, 0xFF00FF00, 0x00000000};
		ImageUtils.bleedAlpha(pixels, 1, 3);
		Assert.assertArrayEquals(new int[] {0x0000FF00, 0xFF00FF00, 0x0000FF00}, pixels);
	}

	@Test
	public void readsAnImageWithTheGivenReader() throws IOException {
		final BufferedImage image = new BufferedImage(2, 1, BufferedImage.TYPE_INT_ARGB);
		image.setRGB(0, 0, 0xFFFF0000);
		image.setRGB(1, 0, 0x800000FF);
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		Assert.assertTrue(ImageIO.write(image, "png", output));

		final BufferedImage read = ImageUtils.read(new ByteArrayInputStream(output.toByteArray()), ImageUtilsTest.png());
		Assert.assertEquals(2, read.getWidth());
		Assert.assertEquals(1, read.getHeight());
		Assert.assertEquals(0xFFFF0000, read.getRGB(0, 0));
		Assert.assertEquals(0x800000FF, read.getRGB(1, 0));
	}

	@Test(expected = IOException.class)
	public void failsOnUnreadableData() throws IOException {
		ImageUtils.read(new ByteArrayInputStream(new byte[] {1, 2, 3}), ImageUtilsTest.png());
	}

	private static ImageReaderSpi png() {
		return ImageIO.getImageReadersByFormatName("png").next().getOriginatingProvider();
	}

}