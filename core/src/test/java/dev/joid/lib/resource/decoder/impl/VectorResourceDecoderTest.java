package dev.joid.lib.resource.decoder.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.internal.TestUtils;
import dev.joid.lib.asset.Asset;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingTexture;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.ResourceData;
import lombok.NonNull;

public class VectorResourceDecoderTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void rendersTheDocumentAtItsIntrinsicSize() {
		final VectorResourceDecoder decoder = VectorResourceDecoder.create(Asset.of(VectorResourceDecoderTest.class.getResourceAsStream("/vector/icon.svg")));
		final ResourceData data = ResourceData.create("icon.svg", null);
		decoder.decode(data);
		Assert.assertEquals(24, data.getWidth());
		Assert.assertEquals(24, data.getHeight());
		final int[] pixels = data.getData()[0];
		Assert.assertEquals(0, pixels[0] >>> 24);
		Assert.assertEquals(255, pixels[12 * 24 + 4] >>> 24);
		Assert.assertEquals(255, pixels[12 * 24 + 12] >>> 24);
	}

	@Test
	public void rendersTheSameBytesOnEveryJdk() throws IOException, NoSuchAlgorithmException {
		Assert.assertEquals("185000c32466a8e97a39eaa61738b3085bb04c09158ee0ff3d9733654042c4f2", VectorResourceDecoderTest.hash("icon.svg", 24));
		Assert.assertEquals("7ee8be61b557274ee6a28eb405f6882f8ce6277ba78785a628e68765c49c7719", VectorResourceDecoderTest.hash("icon.svg", 100));
		Assert.assertEquals("e39fb101f65a58e357f32c0be4e8c5b281659c72cf17a00f74524af727dcd1c1", VectorResourceDecoderTest.hash("strokes.svg", 64));
		Assert.assertEquals("9b236f67189accb8b416a35b2cfd708273478859fd3ce581394af4b929c11b32", VectorResourceDecoderTest.hash("strokes.svg", 161));
		Assert.assertEquals("963bec2164d2d58f36f8c86f94ab9f243c220321a01487e30820f4709cba15fb", VectorResourceDecoderTest.hash("strokes.svg", 900));
	}

	@Test
	public void preparesATransparentPlaceholder() {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = ResourceData.create("icon.svg", decoder);
		decoder.prepare(data);
		final RecordingTexture texture = (RecordingTexture) TestUtils.field(decoder, "texture");
		Assert.assertSame(texture, data.getTextures()[0]);
		Assert.assertEquals(1, texture.getWidth());
		Assert.assertArrayEquals(new int[] {0}, texture.getPixels());
	}

	@Test
	public void uploadsItsIntrinsicSize() {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = VectorResourceDecoderTest.load(decoder);
		final RecordingTexture texture = (RecordingTexture) data.getTextures()[0];
		Assert.assertSame(TestUtils.field(decoder, "texture"), texture);
		Assert.assertEquals(24, texture.getWidth());
		Assert.assertEquals(24, texture.getHeight());
		Assert.assertEquals(24 * 24, texture.getPixels().length);
		Assert.assertEquals(1, VectorResourceDecoderTest.textures(decoder).size());
	}

	@Test
	public void uploadsTheWholeDocumentOfARegion() {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = ResourceData.create("icon.svg", decoder).region(4, 4, 8, 6);
		decoder.prepare(data);
		decoder.decode(data);
		decoder.upload(data);
		final RecordingTexture texture = (RecordingTexture) data.getTextures()[0];
		Assert.assertEquals(24, texture.getWidth());
		Assert.assertEquals(24, texture.getHeight());
		Assert.assertEquals(24 * 24, texture.getPixels().length);
		Assert.assertEquals(255, texture.getPixels()[12 * 24 + 12] >>> 24);
		Assert.assertEquals(8, data.getWidth());
	}

	@Test
	public void ignoresRequestsBeforeItsUpload() {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = ResourceData.create("icon.svg", decoder);
		decoder.request(data, 48, 48, false, false);
		Assert.assertNull(data.getTextures());
		decoder.prepare(data);
		decoder.decode(data);
		decoder.request(data, 48, 48, false, false);
		Assert.assertEquals(1, data.getTextures()[0].getWidth());
	}

	@Test
	public void rendersTheRequestedSizeWhenBlocking() {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = VectorResourceDecoderTest.load(decoder);
		decoder.request(data, 48, 32, false, false);
		final RecordingTexture texture = (RecordingTexture) data.getTextures()[0];
		Assert.assertEquals(48, texture.getWidth());
		Assert.assertEquals(32, texture.getHeight());
		Assert.assertEquals(48 * 32, texture.getPixels().length);
		Assert.assertEquals(2, VectorResourceDecoderTest.textures(decoder).size());
	}

	@Test
	public void showsACachedSizeAgain() {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = VectorResourceDecoderTest.load(decoder);
		final ITexture intrinsic = data.getTextures()[0];
		decoder.request(data, 48, 48, false, false);
		decoder.request(data, 24, 24, false, false);
		Assert.assertSame(intrinsic, data.getTextures()[0]);
		Assert.assertSame(intrinsic, TestUtils.field(decoder, "texture"));
		Assert.assertEquals(2, VectorResourceDecoderTest.textures(decoder).size());
	}

	@Test
	public void clampsTheSizeToTheLargestTexture() {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = VectorResourceDecoderTest.load(decoder);
		decoder.request(data, 8192, 512, false, false);
		Assert.assertEquals(4096, data.getTextures()[0].getWidth());
		Assert.assertEquals(256, data.getTextures()[0].getHeight());
	}

	@Test
	public void rendersInTheBackgroundWhenAsync() throws InterruptedException {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = VectorResourceDecoderTest.load(decoder);
		decoder.request(data, 48, 48, true, false);
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
		decoder.request(data, 48, 48, true, false);
		VectorResourceDecoderTest.awaitRaster(decoder);
		decoder.update(data);
		decoder.request(data, 40, 40, true, true);
		Assert.assertFalse(((boolean) TestUtils.field(decoder, "rendering")));
		Assert.assertEquals(48, data.getTextures()[0].getWidth());
	}

	@Test
	public void rendersTheNextStepWhileTheSizeGrows() throws InterruptedException {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = VectorResourceDecoderTest.load(decoder);
		decoder.request(data, 48, 48, true, false);
		VectorResourceDecoderTest.awaitRaster(decoder);
		decoder.update(data);
		decoder.request(data, 60, 60, true, true);
		VectorResourceDecoderTest.awaitRaster(decoder);
		decoder.update(data);
		Assert.assertEquals(75, data.getTextures()[0].getWidth());
		Assert.assertEquals(75, data.getTextures()[0].getHeight());
	}

	@Test
	public void showsACachedStepWhileTheSizeGrows() {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = VectorResourceDecoderTest.load(decoder);
		decoder.request(data, 30, 30, false, false);
		decoder.request(data, 75, 75, false, false);
		final ITexture step = data.getTextures()[0];
		decoder.request(data, 30, 30, false, false);
		decoder.request(data, 60, 60, true, true);
		Assert.assertFalse(((boolean) TestUtils.field(decoder, "rendering")));
		Assert.assertSame(step, data.getTextures()[0]);
	}

	@Test
	public void rendersTheExactSizeOnceTheSizeRests() throws InterruptedException {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = VectorResourceDecoderTest.load(decoder);
		decoder.request(data, 48, 48, true, false);
		VectorResourceDecoderTest.awaitRaster(decoder);
		decoder.update(data);
		decoder.request(data, 60, 60, true, true);
		VectorResourceDecoderTest.awaitRaster(decoder);
		decoder.update(data);
		decoder.request(data, 60, 60, true, false);
		VectorResourceDecoderTest.awaitRaster(decoder);
		decoder.update(data);
		Assert.assertEquals(60, data.getTextures()[0].getWidth());
	}

	@Test
	public void waitsForItsPendingRasterBeforeAnother() throws InterruptedException {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = VectorResourceDecoderTest.load(decoder);
		decoder.request(data, 48, 48, true, false);
		VectorResourceDecoderTest.awaitRaster(decoder);
		decoder.request(data, 100, 100, true, false);
		Assert.assertFalse(((boolean) TestUtils.field(decoder, "rendering")));
		decoder.update(data);
		Assert.assertEquals(48, data.getTextures()[0].getWidth());
	}

	@Test
	public void rastersEverySizeWhicheverDrawComesFirst() throws InterruptedException {
		Assert.assertArrayEquals(new int[] {48, 20}, VectorResourceDecoderTest.draw(48, 20));
		Assert.assertArrayEquals(new int[] {20, 48}, VectorResourceDecoderTest.draw(20, 48));
	}

	@Test
	public void keepsTheLastEightSizes() {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = VectorResourceDecoderTest.load(decoder);
		final RecordingTexture intrinsic = (RecordingTexture) data.getTextures()[0];
		for (int size = 10; size < 17; size++) {
			decoder.request(data, size, size, false, false);
		}
		Assert.assertEquals(8, VectorResourceDecoderTest.textures(decoder).size());
		Assert.assertFalse(intrinsic.isDeleted());
		decoder.request(data, 17, 17, false, false);
		Assert.assertEquals(8, VectorResourceDecoderTest.textures(decoder).size());
		Assert.assertTrue(intrinsic.isDeleted());
	}

	@Test
	public void deletesEveryCachedSizeOnClear() {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = VectorResourceDecoderTest.load(decoder);
		final RecordingTexture intrinsic = (RecordingTexture) data.getTextures()[0];
		decoder.request(data, 48, 48, false, false);
		final RecordingTexture larger = (RecordingTexture) data.getTextures()[0];
		decoder.clear(data);
		Assert.assertTrue(intrinsic.isDeleted());
		Assert.assertTrue(larger.isDeleted());
		Assert.assertTrue(VectorResourceDecoderTest.textures(decoder).isEmpty());
		Assert.assertNull(TestUtils.field(decoder, "document"));
		decoder.request(data, 64, 64, false, false);
		Assert.assertSame(larger, data.getTextures()[0]);
	}

	@Test
	public void dropsARasterOfTheDocumentItReplaced() throws InterruptedException {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = VectorResourceDecoderTest.load(decoder);
		decoder.request(data, 48, 48, true, false);
		VectorResourceDecoderTest.awaitRaster(decoder);
		final AtomicReference<Object> pending = (AtomicReference<Object>) TestUtils.field(decoder, "pending");
		final Object raster = pending.get();
		decoder.clear(data);
		pending.set(raster);
		decoder.update(data);
		Assert.assertTrue(VectorResourceDecoderTest.textures(decoder).isEmpty());
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
		final VectorResourceDecoder decoder = VectorResourceDecoder.create(new TextAsset("notes.svg", "not a document"));
		final Logger logger = Logger.getLogger("com.github.weisj.jsvg");
		final List<LogRecord> records = new ArrayList<>();
		final Handler handler = new Handler() {

			@Override
			public void publish(final LogRecord record) {
				records.add(record);
			}

			@Override
			public void flush() {}

			@Override
			public void close() {}

		};
		logger.addHandler(handler);
		logger.setUseParentHandlers(false);
		try {
			decoder.decode(ResourceData.create("notes.svg", decoder));
			Assert.fail("The document must not decode");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("notes.svg is not an SVG document that can be parsed", expected.getMessage());
		} finally {
			logger.removeHandler(handler);
			logger.setUseParentHandlers(true);
		}
		Assert.assertEquals("Error while parsing SVG.", records.get(0).getMessage());
	}

	@Test
	public void reportsADocumentThatCannotOpen() {
		final VectorResourceDecoder decoder = VectorResourceDecoder.create(new TextAsset("offline.svg", null));
		try {
			decoder.decode(ResourceData.create("offline.svg", decoder));
			Assert.fail("The document must not decode");
		} catch (final UncheckedIOException expected) {
			Assert.assertEquals("The SVG offline.svg cannot be read", expected.getMessage());
			Assert.assertTrue(expected.getCause() instanceof IOException);
		}
	}

	private static VectorResourceDecoder icon() {
		return VectorResourceDecoder.create(Asset.of(VectorResourceDecoderTest.class.getResourceAsStream("/vector/icon.svg")));
	}

	private static ResourceData load(final VectorResourceDecoder decoder) {
		final ResourceData data = ResourceData.create("icon.svg", decoder);
		decoder.prepare(data);
		decoder.decode(data);
		decoder.upload(data);
		return data;
	}

	private static int[] draw(final int... sizes) throws InterruptedException {
		final VectorResourceDecoder decoder = VectorResourceDecoderTest.icon();
		final ResourceData data = VectorResourceDecoderTest.load(decoder);
		final Resource resource = ResourceBuilder.create().cache(null).compute("icon.svg", () -> data);
		final Resource[] consumers = new Resource[sizes.length];
		for (int index = 0; index < sizes.length; index++) {
			consumers[index] = resource.copy();
		}

		final int[] widths = new int[sizes.length];
		for (int frame = 0; frame < 8; frame++) {
			for (int index = 0; index < sizes.length; index++) {
				consumers[index].request(sizes[index], sizes[index]);
				decoder.update(data);
				widths[index] = data.getTextures()[0].getWidth();
			}
			VectorResourceDecoderTest.awaitRaster(decoder);
		}
		return widths;
	}

	private static int[] size(final String document) {
		final ResourceData data = ResourceData.create("document.svg", null);
		VectorResourceDecoder.create(new TextAsset("document.svg", document)).decode(data);
		return new int[] {data.getWidth(), data.getHeight()};
	}

	private static String hash(final String name, final int size) throws IOException, NoSuchAlgorithmException {
		final VectorResourceDecoder decoder = VectorResourceDecoder.create(Asset.of(VectorResourceDecoderTest.class.getResourceAsStream("/vector/" + name)));
		final ResourceData data = ResourceData.create(name, decoder);
		decoder.prepare(data);
		decoder.decode(data);
		decoder.upload(data);
		decoder.request(data, size, size, false, false);

		final int[] pixels = ((RecordingTexture) data.getTextures()[0]).getPixels();
		final ByteBuffer bytes = ByteBuffer.allocate(pixels.length * 4);
		bytes.asIntBuffer().put(pixels);

		final StringBuilder builder = new StringBuilder();
		for (final byte value : MessageDigest.getInstance("SHA-256").digest(bytes.array())) {
			builder.append(String.format("%02x", value));
		}
		return builder.toString();
	}

	private static void awaitRaster(final VectorResourceDecoder decoder) throws InterruptedException {
		TestUtils.await("The raster did not end", () -> !((boolean) TestUtils.field(decoder, "rendering")));
	}

	private static Map<?, ?> textures(final VectorResourceDecoder decoder) {
		return (Map<?, ?>) TestUtils.field(decoder, "textures");
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