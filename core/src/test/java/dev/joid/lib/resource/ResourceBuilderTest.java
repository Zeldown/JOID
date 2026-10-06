package dev.joid.lib.resource;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.bridge.render.RecordingTexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.resource.dto.ResourceData;
import dev.joid.lib.resource.dto.decoder.impl.AnimatedResourceDecoder;
import dev.joid.lib.resource.dto.decoder.impl.RasterResourceDecoder;
import dev.joid.lib.resource.dto.decoder.impl.VectorResourceDecoder;
import dev.joid.lib.resource.dto.decoder.impl.VideoResourceDecoder;

import lombok.NonNull;

public class ResourceBuilderTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void startsBlockingAndNearestOnTheDefaultCache() {
		final ResourceBuilder builder = ResourceBuilder.create();
		Assert.assertSame(ResourceBuilder.DEFAULT_CACHE, builder.getCache());
		Assert.assertFalse(builder.getProperties().isAsync());
		Assert.assertSame(TextureFilter.NEAREST, builder.getProperties().getInterpolation());
		Assert.assertFalse(builder.getProperties().getMipmap().isPresent());
		Assert.assertNull(builder.getProperties().getTextureCoords());
	}

	@Test
	public void registersEveryBuilder() {
		final ResourceBuilder builder = ResourceBuilder.create();
		final ResourceBuilder copy = builder.copy();
		Assert.assertTrue(ResourceBuilder.getBuilders().contains(builder));
		Assert.assertTrue(ResourceBuilder.getBuilders().contains(copy));
	}

	@Test
	public void changesEveryProperty() {
		final Cache<String, ResourceData> cache = CacheBuilder.newBuilder().build();
		final ResourceBuilder builder = ResourceBuilder.create();
		Assert.assertSame(builder, builder.async().linear().mipmap(true).textureCoords(1D, 2D, 3D, 4D).cache(cache));
		Assert.assertTrue(builder.getProperties().isAsync());
		Assert.assertSame(TextureFilter.LINEAR, builder.getProperties().getInterpolation());
		Assert.assertEquals(Optional.of(true), builder.getProperties().getMipmap());
		Assert.assertArrayEquals(new double[] {1D, 2D, 3D, 4D}, builder.getProperties().getTextureCoords(), 0D);
		Assert.assertSame(cache, builder.getCache());
	}

	@Test
	public void turnsBackToBlockingAndNearest() {
		final ResourceBuilder builder = ResourceBuilder.create().async().linear();
		Assert.assertSame(builder, builder.blocking().nearest());
		Assert.assertFalse(builder.getProperties().isAsync());
		Assert.assertSame(TextureFilter.NEAREST, builder.getProperties().getInterpolation());
		Assert.assertSame(TextureFilter.LINEAR, builder.interpolation(TextureFilter.LINEAR).getProperties().getInterpolation());
	}

	@Test
	public void copiesItsPropertiesAndSharesItsCache() {
		final Cache<String, ResourceData> cache = CacheBuilder.newBuilder().build();
		final ResourceBuilder base = ResourceBuilder.create().async().linear().cache(cache);
		final ResourceBuilder copy = base.copy().nearest();
		Assert.assertNotSame(base, copy);
		Assert.assertSame(cache, copy.getCache());
		Assert.assertTrue(copy.getProperties().isAsync());
		Assert.assertSame(TextureFilter.NEAREST, copy.getProperties().getInterpolation());
		Assert.assertSame(TextureFilter.LINEAR, base.getProperties().getInterpolation());
	}

	@Test
	public void resolvesABufferedImage() {
		final List<Resource> received = new ArrayList<>();
		final BufferedImage image = new BufferedImage(8, 4, BufferedImage.TYPE_INT_ARGB);
		final Resource resource = ResourceBuilder.create().cache(null).of(image, received::add);
		Assert.assertTrue(resource.getDecoder() instanceof RasterResourceDecoder);
		Assert.assertEquals(image.toString(), resource.getUniqueId());
		Assert.assertEquals(Collections.singletonList(resource), received);
	}

	@Test
	public void resolvesATexture() {
		final RecordingTexture texture = new RecordingTexture();
		final Resource resource = ResourceBuilder.create().cache(null).of(texture);
		Assert.assertNull(resource.getDecoder());
		Assert.assertSame(texture, resource.getTexture());
		Assert.assertEquals("texture_" + System.identityHashCode(texture), resource.getUniqueId());
	}

	@Test
	public void decodesAStreamWithTheDecoderOfItsFormat() {
		final ResourceBuilder builder = ResourceBuilder.create().cache(null);
		Assert.assertTrue(builder.of(ResourceBuilderTest.stream("/animation/still.png")).getDecoder() instanceof RasterResourceDecoder);
		Assert.assertTrue(builder.of(ResourceBuilderTest.stream("/animation/blink.gif")).getDecoder() instanceof AnimatedResourceDecoder);
		Assert.assertTrue(builder.of(ResourceBuilderTest.stream("/vector/icon.svg")).getDecoder() instanceof VectorResourceDecoder);
		Assert.assertTrue(builder.of(ResourceBuilderTest.stream("/dev/joid/lib/resource/dto/decoder/impl/frames.mkv")).getDecoder() instanceof VideoResourceDecoder);
	}

	@Test
	public void notifiesTheCallbackOfALocalAssetAtOnce() {
		final List<Resource> received = new ArrayList<>();
		final Resource resource = ResourceBuilder.create().cache(null).of(ResourceBuilderTest.stream("/animation/still.png"), received::add);
		Assert.assertEquals(Collections.singletonList(resource), received);
	}

	@Test
	public void sharesTheDataOfTheSameFile() throws IOException {
		final File file = this.folder.newFile("still.png");
		Files.write(file.toPath(), Asset.of(ResourceBuilderTest.stream("/animation/still.png")).read());
		final ResourceBuilder builder = ResourceBuilder.create().cache(CacheBuilder.newBuilder().build());
		final Resource first = builder.of(file);
		final Resource second = builder.of(file);
		Assert.assertEquals(file.getAbsolutePath(), first.getUniqueId());
		Assert.assertNotSame(first, second);
		Assert.assertSame(first.getResourceData(), second.getResourceData());
	}

	@Test
	public void decodesAnAssetAsIs() {
		final HeldAsset asset = new HeldAsset("local", false, Asset.of(ResourceBuilderTest.stream("/vector/icon.svg")));
		asset.release.countDown();
		final Resource resource = ResourceBuilder.create().cache(null).of(asset);
		Assert.assertEquals("local", resource.getUniqueId());
		Assert.assertTrue(resource.getDecoder() instanceof VectorResourceDecoder);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAnUnknownInput() {
		ResourceBuilder.create().cache(null).of(new Object());
	}

	@Test
	public void picksTheDecoderOfAHeldAssetOnATaskThread() throws InterruptedException {
		final List<Thread> threads = new CopyOnWriteArrayList<>();
		final HeldAsset asset = new HeldAsset("remote", true, Asset.of(ResourceBuilderTest.stream("/animation/still.png")));
		final Resource resource = ResourceBuilder.create().async().cache(null).of(asset, received -> threads.add(Thread.currentThread()));
		Assert.assertNull(resource.getDecoder());
		Assert.assertTrue(threads.isEmpty());
		asset.release.countDown();
		resource.blocking();
		Assert.assertTrue(resource.getDecoder() instanceof RasterResourceDecoder);
		Assert.assertEquals("ResourceTask/remote", threads.get(0).getName());
	}

	@Test
	public void picksTheDecoderOfAHeldAssetAtOnceWhenBlocking() {
		final List<Resource> received = new ArrayList<>();
		final HeldAsset asset = new HeldAsset("remote", true, Asset.of(ResourceBuilderTest.stream("/animation/blink.gif")));
		asset.release.countDown();
		final Resource resource = ResourceBuilder.create().cache(null).of(asset, received::add);
		Assert.assertTrue(resource.getDecoder() instanceof AnimatedResourceDecoder);
		Assert.assertEquals(Collections.singletonList(resource), received);
	}

	@Test
	public void downloadsAHeldAssetOnlyOnce() {
		final HeldAsset asset = new HeldAsset("remote", true, Asset.of(ResourceBuilderTest.stream("/animation/still.png")));
		asset.release.countDown();
		final ResourceBuilder builder = ResourceBuilder.create().cache(CacheBuilder.newBuilder().build());
		final Resource first = builder.of(asset);
		final Resource second = builder.of(asset);
		Assert.assertSame(first.getResourceData(), second.getResourceData());
		Assert.assertEquals(1, asset.opened.get());
	}

	@Test
	public void computesEveryTimeWithoutCache() {
		final AtomicInteger supplied = new AtomicInteger();
		final List<Resource> created = new ArrayList<>();
		final ResourceBuilder builder = ResourceBuilder.create().cache(null);
		final Resource first = builder.compute("image", () -> new ResourceData("image" + supplied.incrementAndGet(), null), created::add);
		builder.reload();
		final Resource second = builder.compute("image", () -> new ResourceData("image" + supplied.incrementAndGet(), null), created::add);
		Assert.assertEquals("image1", first.getUniqueId());
		Assert.assertEquals("image2", second.getUniqueId());
		Assert.assertEquals(2, supplied.get());
		Assert.assertEquals(2, created.size());
	}

	@Test
	public void computesOnceInItsCache() {
		final AtomicInteger supplied = new AtomicInteger();
		final List<Resource> created = new ArrayList<>();
		final Cache<String, ResourceData> cache = CacheBuilder.newBuilder().build();
		final ResourceBuilder builder = ResourceBuilder.create().cache(cache);
		final Resource first = builder.compute("image", () -> new ResourceData("image" + supplied.incrementAndGet(), null), created::add);
		final Resource second = builder.compute("image", () -> new ResourceData("image" + supplied.incrementAndGet(), null), created::add);
		Assert.assertSame(first.getResourceData(), second.getResourceData());
		Assert.assertSame(first.getResourceData(), cache.getIfPresent("image"));
		Assert.assertSame(builder, second.getBuilder());
		Assert.assertEquals(1, supplied.get());
		Assert.assertEquals(Collections.singletonList(first), created);
	}

	@Test
	public void computesWithoutCreationCallback() {
		final Resource resource = ResourceBuilder.create().cache(CacheBuilder.newBuilder().build()).compute("image", () -> new ResourceData("image", null));
		Assert.assertEquals("image", resource.getUniqueId());
	}

	@Test
	public void reloadsByEmptyingItsCache() {
		final AtomicInteger supplied = new AtomicInteger();
		final Cache<String, ResourceData> cache = CacheBuilder.newBuilder().build();
		final ResourceBuilder builder = ResourceBuilder.create().cache(cache);
		builder.compute("image", () -> new ResourceData("image" + supplied.incrementAndGet(), null));
		builder.reload();
		Assert.assertEquals(0L, cache.size());
		Assert.assertEquals("image2", builder.compute("image", () -> new ResourceData("image" + supplied.incrementAndGet(), null)).getUniqueId());
	}

	@Test
	public void notifiesTheCallbackOfACachedRemoteAsset() throws IOException {
		final List<Resource> first = new ArrayList<>();
		final List<Resource> second = new ArrayList<>();
		final RemoteAsset asset = new RemoteAsset(Asset.of(ResourceBuilderTest.class.getResourceAsStream("/animation/still.png")).read());
		final ResourceBuilder builder = ResourceBuilder.create().cache(CacheBuilder.newBuilder().build());
		builder.of(asset, first::add);
		final Resource cached = builder.of(asset, second::add);
		Assert.assertEquals(1, first.size());
		Assert.assertEquals(Collections.singletonList(cached), second);
	}

	private static InputStream stream(final String path) {
		return ResourceBuilderTest.class.getResourceAsStream(path);
	}

	private static final class HeldAsset extends Asset {

		private final CountDownLatch release = new CountDownLatch(1);
		private final AtomicInteger  opened  = new AtomicInteger();

		private final boolean remote;
		private final byte[]  bytes;

		private HeldAsset(final String uniqueId, final boolean remote, final Asset source) {
			super(uniqueId);
			this.remote = remote;
			try {
				this.bytes = source.read();
			} catch (final IOException exception) {
				throw new IllegalStateException(exception);
			}
		}

		@Override
		public @NonNull InputStream open() throws IOException {
			try {
				this.release.await();
			} catch (final InterruptedException exception) {
				throw new IOException(exception);
			}
			this.opened.incrementAndGet();
			return new ByteArrayInputStream(this.bytes);
		}

		@Override
		public boolean isRemote() {
			return this.remote;
		}

	}

	private static final class RemoteAsset extends Asset {

		private final byte[] bytes;

		private RemoteAsset(final byte[] bytes) {
			super("https://joid.invalid/still.png");
			this.bytes = bytes;
		}

		@Override
		public @NonNull InputStream open() {
			return new ByteArrayInputStream(this.bytes);
		}

		@Override
		public boolean isRemote() {
			return true;
		}

	}

}