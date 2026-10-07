package dev.joid.lib.resource.dto.format;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.resource.dto.ResourceData;
import dev.joid.lib.resource.dto.decoder.IResourceDecoder;
import dev.joid.lib.resource.dto.decoder.impl.AnimatedResourceDecoder;
import dev.joid.lib.resource.dto.decoder.impl.RasterResourceDecoder;
import dev.joid.lib.resource.dto.decoder.impl.VideoResourceDecoder;

public class ResourceFormatTest {

	private static final byte[] PNG  = {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10, 0, 0, 0, 13, 'I', 'H', 'D', 'R'};
	private static final byte[] MP4  = {0, 0, 0, 32, 'f', 't', 'y', 'p', 'i', 's', 'o', 'm', 0, 0, 2, 0};
	private static final byte[] MOV  = {0, 0, 0, 20, 'f', 't', 'y', 'p', 'q', 't', ' ', ' ', 0, 0, 2, 0};
	private static final byte[] HEIC = {0, 0, 0, 24, 'f', 't', 'y', 'p', 'h', 'e', 'i', 'c', 0, 0, 0, 0, 'm', 'i', 'f', '1', 'h', 'e', 'i', 'c'};
	private static final byte[] AVIF = {0, 0, 0, 28, 'f', 't', 'y', 'p', 'a', 'v', 'i', 'f', 0, 0, 0, 0, 'a', 'v', 'i', 'f', 'm', 'i', 'f', '1', 'm', 'i', 'a', 'f'};
	private static final byte[] HEIF = {0, 0, 0, 24, 'f', 't', 'y', 'p', 'm', 'i', 'f', '1', 0, 0, 0, 0, 'm', 'i', 'f', '1', 'h', 'e', 'i', 'c'};
	private static final byte[] WEBM = {(byte) 0x1A, (byte) 0x45, (byte) 0xDF, (byte) 0xA3, 1, 0, 0, 0, 0, 0, 0, 31};

	@Test
	public void picksTheRasterDecoderOtherwise() {
		Assert.assertTrue(ResourceFormat.decoder(Asset.of(new ByteArrayInputStream(ResourceFormatTest.PNG))) instanceof RasterResourceDecoder);
	}

	@Test
	public void picksTheVideoDecoderOnAVideoHeader() {
		for (final byte[] header : new byte[][] {ResourceFormatTest.MP4, ResourceFormatTest.MOV, ResourceFormatTest.WEBM}) {
			final IResourceDecoder decoder = ResourceFormat.decoder(Asset.of(new ByteArrayInputStream(header)));
			Assert.assertTrue(decoder instanceof VideoResourceDecoder);
			Assert.assertFalse(((VideoResourceDecoder) decoder).isLoop());
		}
	}

	@Test
	public void refusesAHeifOrAvifImage() {
		for (final byte[] header : new byte[][] {ResourceFormatTest.HEIC, ResourceFormatTest.AVIF, ResourceFormatTest.HEIF}) {
			final Asset asset = Asset.of(new ByteArrayInputStream(header));
			try {
				ResourceFormat.decoder(asset);
				Assert.fail("A HEIF or AVIF image must be refused");
			} catch (final IllegalArgumentException expected) {
				Assert.assertEquals(asset.getUniqueId() + " is a HEIF or AVIF image, which JOID cannot decode: convert it to PNG, JPEG or WebP", expected.getMessage());
			}
		}
	}

	@Test
	public void picksTheAnimatedDecoderForGifAndApng() {
		Assert.assertTrue(ResourceFormat.decoder(Asset.of(ResourceFormatTest.class.getResourceAsStream("/animation/blink.gif"))) instanceof AnimatedResourceDecoder);
		Assert.assertTrue(ResourceFormat.decoder(Asset.of(ResourceFormatTest.class.getResourceAsStream("/animation/blink.png"))) instanceof AnimatedResourceDecoder);
		Assert.assertTrue(ResourceFormat.decoder(Asset.of(ResourceFormatTest.class.getResourceAsStream("/animation/still.png"))) instanceof RasterResourceDecoder);
	}

	@Test
	public void decodesAnimatedAndStillWebp() {
		Assert.assertTrue(ResourceFormat.decoder(Asset.of(ResourceFormatTest.class.getResourceAsStream("/animation/blink.webp"))) instanceof AnimatedResourceDecoder);

		final IResourceDecoder still = ResourceFormat.decoder(Asset.of(ResourceFormatTest.class.getResourceAsStream("/animation/still.webp")));
		Assert.assertTrue(still instanceof RasterResourceDecoder);
		final ResourceData data = new ResourceData("still.webp", null);
		still.decode(data);
		Assert.assertEquals(8, data.getWidth());
		Assert.assertEquals(0xFFFF0000, data.getData()[0][0]);
		Assert.assertEquals(0, data.getData()[0][63] >>> 24);
	}

	@Test
	public void leavesTheAssetUntouched() throws IOException {
		final Asset asset = Asset.of(new ByteArrayInputStream(ResourceFormatTest.PNG));
		ResourceFormat.decoder(asset);
		Assert.assertArrayEquals(ResourceFormatTest.PNG, asset.read());
	}

	@Test
	public void fallsBackToTheRasterDecoderForAnUnknownHeader() throws IOException {
		final BufferedImage image = new BufferedImage(2, 1, BufferedImage.TYPE_INT_RGB);
		image.setRGB(0, 0, 0xFF0000);
		image.setRGB(1, 0, 0x0000FF);
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(image, "bmp", output);
		final IResourceDecoder decoder = ResourceFormat.decoder(Asset.of(new ByteArrayInputStream(output.toByteArray())));
		Assert.assertTrue(decoder instanceof RasterResourceDecoder);
		final ResourceData data = new ResourceData("image.bmp", null);
		decoder.decode(data);
		Assert.assertEquals(2, data.getWidth());
		Assert.assertEquals(1, data.getHeight());
		Assert.assertArrayEquals(new int[] {0xFFFF0000, 0xFF0000FF}, data.getData()[0]);
	}

}