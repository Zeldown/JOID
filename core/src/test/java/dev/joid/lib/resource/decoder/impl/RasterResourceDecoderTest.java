package dev.joid.lib.resource.decoder.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingTexture;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.resource.ResourceData;
import lombok.NonNull;

public class RasterResourceDecoderTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void decodesAnImageAlreadyInMemory() {
		final BufferedImage image = new BufferedImage(2, 1, BufferedImage.TYPE_INT_ARGB);
		image.setRGB(0, 0, 0xFF336699);
		final ResourceData data = ResourceData.create("image", null);
		RasterResourceDecoder.create(image).decode(data);
		Assert.assertEquals(2, data.getWidth());
		Assert.assertEquals(1, data.getHeight());
		Assert.assertArrayEquals(new int[] {0xFF336699, 0x00336699}, data.getData()[0]);
	}

	@Test
	public void readsAnAssetWithImageIo() {
		final ResourceData data = ResourceData.create("still.png", null);
		RasterResourceDecoder.create(Asset.of(RasterResourceDecoderTest.class.getResourceAsStream("/animation/still.png"))).decode(data);
		Assert.assertEquals(8, data.getWidth());
		Assert.assertEquals(8, data.getHeight());
		Assert.assertEquals(0xFFFF0000, data.getData()[0][0]);
		Assert.assertEquals(0x00FF0000, data.getData()[0][63]);
	}

	@Test
	public void readsAnAssetWithTheGivenReader() {
		final ResourceData data = ResourceData.create("still.webp", null);
		RasterResourceDecoder.create(Asset.of(RasterResourceDecoderTest.class.getResourceAsStream("/animation/still.webp")), new WebPImageReaderSpi()).decode(data);
		Assert.assertEquals(8, data.getWidth());
		Assert.assertEquals(8, data.getHeight());
		Assert.assertEquals(64, data.getData()[0].length);
	}

	@Test
	public void refusesAnAssetImageIoCannotRead() {
		try {
			RasterResourceDecoder.create(new BytesAsset("notes.txt", "not an image".getBytes(StandardCharsets.UTF_8))).decode(ResourceData.create("notes.txt", null));
			Assert.fail("The image must not decode");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("notes.txt is not an image that ImageIO can decode, convert it to PNG, JPEG or WebP", expected.getMessage());
		}
	}

	@Test
	public void reportsAnAssetThatCannotOpen() {
		try {
			RasterResourceDecoder.create(new BytesAsset("offline.png", null)).decode(ResourceData.create("offline.png", null));
			Assert.fail("The image must not decode");
		} catch (final UncheckedIOException expected) {
			Assert.assertEquals("The image offline.png cannot be read", expected.getMessage());
			Assert.assertTrue(expected.getCause() instanceof IOException);
		}
	}

	@Test
	public void preparesATransparentPlaceholder() {
		final ResourceData data = ResourceData.create("image", null);
		RasterResourceDecoder.create(new BufferedImage(2, 1, BufferedImage.TYPE_INT_ARGB)).prepare(data);
		final RecordingTexture texture = (RecordingTexture) data.getTextures()[0];
		Assert.assertEquals(1, texture.getWidth());
		Assert.assertEquals(1, texture.getHeight());
		Assert.assertArrayEquals(new int[] {0}, texture.getPixels());
	}

	@Test
	public void uploadsEveryDecodedFrame() {
		final RecordingTexture first = new RecordingTexture();
		final RecordingTexture second = new RecordingTexture();
		final ResourceData data = ResourceData.create("image", null).textures(new ITexture[] {first, second}).data(new int[][] {{1, 2}, null}).width(2).height(1);
		RasterResourceDecoder.create(new BufferedImage(2, 1, BufferedImage.TYPE_INT_ARGB)).upload(data);
		Assert.assertArrayEquals(new int[] {1, 2}, first.getPixels());
		Assert.assertEquals(2, first.getWidth());
		Assert.assertEquals(1, first.getHeight());
		Assert.assertNull(second.getPixels());
	}

	@Test
	public void readsItsAssetAgainOnceUploaded() {
		final BytesAsset asset = new BytesAsset("still.png", RasterResourceDecoderTest.bytes("/animation/still.png"));
		final RasterResourceDecoder decoder = RasterResourceDecoder.create(asset);
		final ResourceData data = ResourceData.create("still.png", decoder);
		decoder.prepare(data);
		decoder.decode(data);
		decoder.upload(data);
		decoder.decode(data);
		Assert.assertEquals(2, asset.opened.get());
	}

	@Test
	public void leavesTheResourceUntouchedOnInitAndUpdate() {
		final RasterResourceDecoder decoder = RasterResourceDecoder.create(new BufferedImage(2, 1, BufferedImage.TYPE_INT_ARGB));
		final ResourceData data = ResourceData.create("image", null);
		decoder.init(data);
		decoder.update(data);
		Assert.assertNull(data.getTextures());
		Assert.assertNull(data.getData());
		Assert.assertEquals(0, data.getWidth());
	}

	private static byte[] bytes(final String path) {
		try {
			return Asset.of(RasterResourceDecoderTest.class.getResourceAsStream(path)).read();
		} catch (final IOException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private static final class BytesAsset extends Asset {

		private final AtomicInteger opened = new AtomicInteger();

		private final byte[] bytes;

		private BytesAsset(final String uniqueId, final byte[] bytes) {
			super(uniqueId);
			this.bytes = bytes;
		}

		@Override
		public @NonNull InputStream open() throws IOException {
			if (this.bytes == null) {
				throw new IOException("offline");
			}
			this.opened.incrementAndGet();
			return new ByteArrayInputStream(this.bytes);
		}

	}

}