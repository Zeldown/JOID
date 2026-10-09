package dev.joid.lib.resource.decoder.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingTexture;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.resource.ResourceData;
import lombok.NonNull;

public class VectorResourceDecoderTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void rendersTheDocumentAtItsIntrinsicSize() {
		final VectorResourceDecoder decoder = new VectorResourceDecoder(Asset.of(VectorResourceDecoderTest.class.getResourceAsStream("/vector/icon.svg")));
		final ResourceData data = new ResourceData("icon.svg", null);
		decoder.decode(data);
		Assert.assertEquals(24, data.getWidth());
		Assert.assertEquals(24, data.getHeight());
		final int[] pixels = data.getData()[0];
		Assert.assertEquals(0, pixels[0] >>> 24);
		Assert.assertEquals(255, pixels[12 * 24 + 4] >>> 24);
		Assert.assertEquals(255, pixels[12 * 24 + 12] >>> 24);
	}

	@Test
	public void preparesATransparentPlaceholder() {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = new ResourceData("icon.svg", decoder);
		decoder.prepare(data);
		final RecordingTexture texture = (RecordingTexture) decoder.getTexture();
		Assert.assertSame(texture, data.getTextures()[0]);
		Assert.assertEquals(1, texture.getWidth());
		Assert.assertArrayEquals(new int[] {0}, texture.getPixels());
	}

	@Test
	public void uploadsItsIntrinsicSize() {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = VectorResourceDecoderTest.load(decoder);
		final RecordingTexture texture = (RecordingTexture) data.getTextures()[0];
		Assert.assertSame(decoder.getTexture(), texture);
		Assert.assertEquals(24, texture.getWidth());
		Assert.assertEquals(24, texture.getHeight());
		Assert.assertEquals(24 * 24, texture.getPixels().length);
		Assert.assertEquals(1, decoder.getTextures().size());
	}

	@Test
	public void ignoresRequestsBeforeItsUpload() {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = new ResourceData("icon.svg", decoder);
		decoder.request(data, 48, 48, false);
		Assert.assertNull(data.getTextures());
		decoder.prepare(data);
		decoder.decode(data);
		decoder.request(data, 48, 48, false);
		Assert.assertEquals(1, data.getTextures()[0].getWidth());
		Assert.assertEquals(0, decoder.getRequestedWidth());
	}

	@Test
	public void rendersTheRequestedSizeWhenBlocking() {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = VectorResourceDecoderTest.load(decoder);
		decoder.request(data, 48, 32, false);
		final RecordingTexture texture = (RecordingTexture) data.getTextures()[0];
		Assert.assertEquals(48, texture.getWidth());
		Assert.assertEquals(32, texture.getHeight());
		Assert.assertEquals(48 * 32, texture.getPixels().length);
		Assert.assertEquals(48, decoder.getRequestedWidth());
		Assert.assertEquals(32, decoder.getRequestedHeight());
		Assert.assertEquals(2, decoder.getTextures().size());
	}

	@Test
	public void showsACachedSizeAgain() {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = VectorResourceDecoderTest.load(decoder);
		final ITexture intrinsic = data.getTextures()[0];
		decoder.request(data, 48, 48, false);
		decoder.request(data, 24, 24, false);
		Assert.assertSame(intrinsic, data.getTextures()[0]);
		Assert.assertSame(intrinsic, decoder.getTexture());
		Assert.assertEquals(2, decoder.getTextures().size());
	}

	@Test
	public void clampsTheSizeToTheLargestTexture() {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = VectorResourceDecoderTest.load(decoder);
		decoder.request(data, 8192, 512, false);
		Assert.assertEquals(4096, data.getTextures()[0].getWidth());
		Assert.assertEquals(256, data.getTextures()[0].getHeight());
	}

	@Test
	public void rendersInTheBackgroundWhenAsync() throws InterruptedException {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = VectorResourceDecoderTest.load(decoder);
		decoder.request(data, 48, 48, true);
		VectorResourceDecoderTest.awaitRaster(decoder);
		Assert.assertFalse(decoder.isSettled());
		Assert.assertEquals(24, data.getTextures()[0].getWidth());
		decoder.update(data);
		Assert.assertTrue(decoder.isSettled());
		Assert.assertEquals(48, data.getTextures()[0].getWidth());
		Assert.assertEquals(48 * 48, ((RecordingTexture) data.getTextures()[0]).getPixels().length);
	}

	@Test
	public void keepsACloseTextureWhileTheSizeChanges() throws InterruptedException {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = VectorResourceDecoderTest.load(decoder);
		decoder.request(data, 48, 48, true);
		VectorResourceDecoderTest.awaitRaster(decoder);
		decoder.update(data);
		decoder.request(data, 40, 40, true);
		Assert.assertFalse(decoder.isRendering());
		Assert.assertEquals(40, decoder.getRequestedWidth());
		Assert.assertEquals(48, data.getTextures()[0].getWidth());
	}

	@Test
	public void rendersTheNextStepWhileTheSizeGrows() throws InterruptedException {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = VectorResourceDecoderTest.load(decoder);
		decoder.request(data, 48, 48, true);
		VectorResourceDecoderTest.awaitRaster(decoder);
		decoder.update(data);
		decoder.request(data, 60, 60, true);
		VectorResourceDecoderTest.awaitRaster(decoder);
		decoder.update(data);
		Assert.assertEquals(75, data.getTextures()[0].getWidth());
		Assert.assertEquals(75, data.getTextures()[0].getHeight());
	}

	@Test
	public void showsACachedStepWhileTheSizeGrows() {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = VectorResourceDecoderTest.load(decoder);
		decoder.request(data, 30, 30, false);
		decoder.request(data, 75, 75, false);
		final ITexture step = data.getTextures()[0];
		decoder.request(data, 30, 30, false);
		decoder.request(data, 60, 60, true);
		Assert.assertFalse(decoder.isRendering());
		Assert.assertSame(step, data.getTextures()[0]);
	}

	@Test
	public void rendersTheExactSizeOnceTheSizeRests() throws InterruptedException {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = VectorResourceDecoderTest.load(decoder);
		decoder.request(data, 48, 48, true);
		VectorResourceDecoderTest.awaitRaster(decoder);
		decoder.update(data);
		decoder.request(data, 60, 60, true);
		VectorResourceDecoderTest.awaitRaster(decoder);
		decoder.update(data);
		this.bridges.getClock().advance(200L);
		decoder.request(data, 60, 60, true);
		VectorResourceDecoderTest.awaitRaster(decoder);
		decoder.update(data);
		Assert.assertEquals(60, data.getTextures()[0].getWidth());
	}

	@Test
	public void waitsForItsPendingRasterBeforeAnother() throws InterruptedException {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = VectorResourceDecoderTest.load(decoder);
		decoder.request(data, 48, 48, true);
		VectorResourceDecoderTest.awaitRaster(decoder);
		this.bridges.getClock().advance(300L);
		decoder.request(data, 100, 100, true);
		Assert.assertFalse(decoder.isRendering());
		decoder.update(data);
		Assert.assertEquals(48, data.getTextures()[0].getWidth());
	}

	@Test
	public void keepsTheLastEightSizes() {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = VectorResourceDecoderTest.load(decoder);
		final RecordingTexture intrinsic = (RecordingTexture) data.getTextures()[0];
		for (int size = 10; size < 17; size++) {
			decoder.request(data, size, size, false);
		}
		Assert.assertEquals(8, decoder.getTextures().size());
		Assert.assertFalse(intrinsic.isDeleted());
		decoder.request(data, 17, 17, false);
		Assert.assertEquals(8, decoder.getTextures().size());
		Assert.assertTrue(intrinsic.isDeleted());
	}

	@Test
	public void deletesEveryCachedSizeOnClear() {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = VectorResourceDecoderTest.load(decoder);
		final RecordingTexture intrinsic = (RecordingTexture) data.getTextures()[0];
		decoder.request(data, 48, 48, false);
		final RecordingTexture larger = (RecordingTexture) data.getTextures()[0];
		decoder.clear(data);
		Assert.assertTrue(intrinsic.isDeleted());
		Assert.assertTrue(larger.isDeleted());
		Assert.assertTrue(decoder.getTextures().isEmpty());
		Assert.assertNull(decoder.getDocument());
		decoder.request(data, 64, 64, false);
		Assert.assertSame(larger, data.getTextures()[0]);
	}

	@Test
	public void keepsItsTextureWithoutPendingRaster() {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = VectorResourceDecoderTest.load(decoder);
		final ITexture intrinsic = data.getTextures()[0];
		decoder.init(data);
		decoder.update(data);
		Assert.assertSame(intrinsic, data.getTextures()[0]);
	}

	@Test
	public void isSettledAndNeverMipmappable() {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		Assert.assertTrue(decoder.isSettled());
		Assert.assertFalse(decoder.isMipmappable());
	}

	@Test
	public void takesTheSizeOfItsViewBox() {
		Assert.assertArrayEquals(new int[] {40, 10}, VectorResourceDecoderTest.size("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 40 10\"/>"));
	}

	@Test
	public void neverDecodesAnEmptyImage() {
		Assert.assertArrayEquals(new int[] {1, 1}, VectorResourceDecoderTest.size("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"0\" height=\"0\"/>"));
	}

	@Test
	public void refusesADocumentItCannotParse() {
		final VectorResourceDecoder decoder = new VectorResourceDecoder(new TextAsset("notes.svg", "not a document"));
		try {
			decoder.decode(new ResourceData("notes.svg", decoder));
			Assert.fail("The document must not decode");
		} catch (final RuntimeException expected) {
			Assert.assertEquals("Failed to parse the SVG of notes.svg", expected.getMessage());
		}
	}

	@Test
	public void reportsADocumentThatCannotOpen() {
		final VectorResourceDecoder decoder = new VectorResourceDecoder(new TextAsset("offline.svg", null));
		try {
			decoder.decode(new ResourceData("offline.svg", decoder));
			Assert.fail("The document must not decode");
		} catch (final RuntimeException expected) {
			Assert.assertEquals("Unable to read the SVG of offline.svg", expected.getMessage());
			Assert.assertTrue(expected.getCause() instanceof IOException);
		}
	}

	private static VectorResourceDecoder icon() {
		return new VectorResourceDecoder(Asset.of(VectorResourceDecoderTest.class.getResourceAsStream("/vector/icon.svg")));
	}

	private static ResourceData load(final VectorResourceDecoder decoder) {
		final ResourceData data = new ResourceData("icon.svg", decoder);
		decoder.prepare(data);
		decoder.decode(data);
		decoder.upload(data);
		return data;
	}

	private static int[] size(final String document) {
		final ResourceData data = new ResourceData("document.svg", null);
		new VectorResourceDecoder(new TextAsset("document.svg", document)).decode(data);
		return new int[] {data.getWidth(), data.getHeight()};
	}

	private static void awaitRaster(final VectorResourceDecoder decoder) throws InterruptedException {
		final long deadline = System.currentTimeMillis() + 5000L;
		while (decoder.isRendering() && System.currentTimeMillis() < deadline) {
			Thread.sleep(1L);
		}
		Assert.assertFalse(decoder.isRendering());
	}

	private static final class TextAsset extends Asset {

		private final String text;

		private TextAsset(final String uniqueId, final String text) {
			super(uniqueId);
			this.text = text;
		}

		@Override
		public @NonNull InputStream open() throws IOException {
			if (this.text == null) {
				throw new IOException("offline");
			}
			return new ByteArrayInputStream(this.text.getBytes(StandardCharsets.UTF_8));
		}

	}

}