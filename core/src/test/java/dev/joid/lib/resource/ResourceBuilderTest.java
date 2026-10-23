package dev.joid.lib.resource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.cache.Cache;

import dev.joid.internal.TestUtils;
import dev.joid.lib.asset.Asset;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.thread.QueueThreadBridge;
import dev.joid.lib.resource.cache.IResourceCache;
import dev.joid.lib.resource.cache.MemoryResourceCache;
import dev.joid.lib.resource.decoder.impl.AnimatedResourceDecoder;
import dev.joid.lib.resource.decoder.impl.RasterResourceDecoder;
import dev.joid.lib.resource.decoder.impl.VectorResourceDecoder;
import dev.joid.lib.resource.decoder.impl.VideoResourceDecoder;
import dev.joid.lib.resource.resolver.IResourceResolver;
import dev.joid.lib.resource.resolver.ResourceResolver;
import lombok.Getter;
import lombok.NonNull;

public class ResourceBuilderTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void startsLinearOnTheDefaultCache() {
		final ResourceBuilder builder = ResourceBuilder.create();
		Assert.assertTrue(builder.getCache() instanceof MemoryResourceCache);
		Assert.assertSame(ResourceBuilder.create().getCache(), builder.getCache());
		Assert.assertSame(TextureFilter.LINEAR, builder.getProperties().getInterpolation());
		Assert.assertNull(builder.getProperties().getMipmap());
		Assert.assertNull(builder.getProperties().getTextureCoords());
	}

	@Test
	public void registersEveryBuilder() {
		final ResourceBuilder builder = ResourceBuilder.create();
		final ResourceBuilder linear = builder.linear();
		Assert.assertTrue(ResourceBuilder.getBuilders().contains(builder));
		Assert.assertTrue(ResourceBuilder.getBuilders().contains(linear));
	}

	@Test
	public void forgetsABuilderNoLongerUsed() throws InterruptedException {
		final WeakReference<ResourceBuilder> reference = new WeakReference<>(ResourceBuilder.create());
		final long deadline = System.currentTimeMillis() + 5000L;
		while (reference.get() != null && System.currentTimeMillis() < deadline) {
			System.gc();
			Thread.sleep(10L);
		}
		Assert.assertNull(reference.get());
	}

	@Test
	public void changesEveryPropertyInANewBuilder() {
		final MemoryResourceCache cache = MemoryResourceCache.create();
		final ResourceBuilder builder = ResourceBuilder.create();
		final ResourceBuilder changed = builder.nearest().mipmap(true).textureCoords(1D, 2D, 3D, 4D).cache(cache);
		Assert.assertNotSame(builder, changed);
		Assert.assertSame(TextureFilter.NEAREST, changed.getProperties().getInterpolation());
		Assert.assertEquals(Boolean.TRUE, changed.getProperties().getMipmap());
		Assert.assertArrayEquals(new double[] {1D, 2D, 3D, 4D}, changed.getProperties().getTextureCoords(), 0D);
		Assert.assertSame(cache, changed.getCache());
		Assert.assertSame(TextureFilter.LINEAR, builder.getProperties().getInterpolation());
		Assert.assertNull(builder.getProperties().getMipmap());
		Assert.assertNull(builder.getProperties().getTextureCoords());
		Assert.assertSame(ResourceBuilder.create().getCache(), builder.getCache());
	}

	@Test
	public void turnsBackToNearest() {
		final ResourceBuilder builder = ResourceBuilder.create().linear();
		Assert.assertSame(TextureFilter.NEAREST, builder.nearest().getProperties().getInterpolation());
		Assert.assertSame(TextureFilter.LINEAR, builder.getProperties().getInterpolation());
		Assert.assertSame(TextureFilter.LINEAR, ResourceBuilder.create().interpolation(TextureFilter.LINEAR).getProperties().getInterpolation());
	}

	@Test
	public void givesACopyOfItsProperties() {
		final ResourceBuilder builder = ResourceBuilder.create();
		builder.getProperties().nearest().mipmap(true);
		Assert.assertSame(TextureFilter.LINEAR, builder.getProperties().getInterpolation());
		Assert.assertNull(builder.getProperties().getMipmap());
	}

	@Test
	public void sharesItsCacheWithTheBuildersItDerives() {
		final MemoryResourceCache cache = MemoryResourceCache.create();
		final ResourceBuilder base = ResourceBuilder.create().linear().cache(cache);
		final ResourceBuilder nearest = base.nearest();
		Assert.assertSame(cache, nearest.getCache());
		Assert.assertSame(TextureFilter.NEAREST, nearest.getProperties().getInterpolation());
		Assert.assertSame(TextureFilter.LINEAR, base.getProperties().getInterpolation());
	}

	@Test
	public void resolvesAnInputThroughTheResourceResolvers() {
		final Object input = new Object();
		final IResourceResolver resolver = new IResourceResolver() {

			@Override
			public boolean supports(final @NonNull Object other) {
				return other == input;
			}

			@Override
			public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object other) {
				return builder.compute("resolved", () -> ResourceData.create("resolved", null));
			}

		};
		ResourceResolver.register(resolver);
		try {
			Assert.assertEquals("resolved", ResourceBuilder.create().cache(null).of(input).getUniqueId());
		} finally {
			ResourceResolver.unregister(resolver);
		}
	}

	@Test
	public void decodesAStreamWithTheDecoderOfItsFormat() {
		final ResourceBuilder builder = ResourceBuilder.create().cache(null);
		Assert.assertTrue(builder.of(ResourceBuilderTest.stream("/animation/still.png")).getDecoder() instanceof RasterResourceDecoder);
		Assert.assertTrue(builder.of(ResourceBuilderTest.stream("/animation/blink.gif")).getDecoder() instanceof AnimatedResourceDecoder);
		Assert.assertTrue(builder.of(ResourceBuilderTest.stream("/vector/icon.svg")).getDecoder() instanceof VectorResourceDecoder);
		Assert.assertTrue(builder.of(ResourceBuilderTest.stream("/dev/joid/lib/resource/decoder/impl/frames.mkv")).getDecoder() instanceof VideoResourceDecoder);
	}

	@Test
	public void notifiesTheLoadOfALocalAssetOnceDecoded() {
		final List<Resource> loads = new ArrayList<>();
		final Resource resource = ResourceBuilder.create().cache(null).of(ResourceBuilderTest.stream("/animation/still.png")).blocking().onLoad(loads::add);
		Assert.assertTrue(loads.isEmpty());
		resource.generate();
		Assert.assertTrue(resource.isLoaded());
		Assert.assertEquals(Collections.singletonList(resource), loads);
	}

	@Test
	public void deliversTheLoadOnTheRenderThread() {
		final QueueThreadBridge thread = new QueueThreadBridge();
		final List<Resource> loads = new ArrayList<>();
		BridgeHandler.THREAD.register(thread);
		try {
			final Resource resource = ResourceBuilder.create().cache(null).of(ResourceBuilderTest.stream("/animation/still.png")).blocking().onLoad(loads::add);
			resource.generate();
			Assert.assertTrue(loads.isEmpty());
			thread.run();
			Assert.assertEquals(Collections.singletonList(resource), loads);
		} finally {
			BridgeHandler.THREAD.unregister(thread);
		}
	}

	@Test
	public void sharesTheDataOfTheSameFile() throws IOException {
		final File file = this.folder.newFile("still.png");
		Files.write(file.toPath(), Asset.of(ResourceBuilderTest.stream("/animation/still.png")).read());
		final ResourceBuilder builder = ResourceBuilder.create().cache(MemoryResourceCache.create());
		final Resource first = builder.of(file);
		final Resource second = builder.of(file);
		Assert.assertEquals(file.getAbsolutePath(), first.getUniqueId());
		Assert.assertNotSame(first, second);
		Assert.assertSame(first.getResourceData(), second.getResourceData());
	}

	@Test
	public void sharesTheDataOfAFileStillInUseAfterItsExpiry() throws IOException, ReflectiveOperationException {
		final File file = this.folder.newFile("still.png");
		Files.write(file.toPath(), Asset.of(ResourceBuilderTest.stream("/animation/still.png")).read());
		final MemoryResourceCache cache = MemoryResourceCache.create();
		final ResourceBuilder builder = ResourceBuilder.create().cache(cache);
		final Resource first = builder.of(file);
		final Field field = MemoryResourceCache.class.getDeclaredField("cache");
		field.setAccessible(true);
		((Cache<?, ?>) field.get(cache)).invalidateAll();
		Assert.assertSame(first.getResourceData(), builder.of(file).getResourceData());
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
		final HeldAsset asset = new HeldAsset("remote", true, Asset.of(ResourceBuilderTest.stream("/animation/still.png")));
		final Resource resource = ResourceBuilder.create().cache(null).of(asset);
		Assert.assertNull(resource.getDecoder());
		Assert.assertTrue(asset.readers.isEmpty());
		asset.release.countDown();
		resource.getResourceData().await();
		Assert.assertTrue(resource.getDecoder() instanceof RasterResourceDecoder);
		Assert.assertEquals("ResourceTask/remote", asset.readers.get(0).getName());
	}

	@Test
	public void keepsTheDecoderGivenWhileARemoteAssetIsDetected() throws InterruptedException {
		final HeldAsset asset = new HeldAsset("remote", true, Asset.of(ResourceBuilderTest.stream("/animation/still.png")));
		final RasterResourceDecoder decoder = RasterResourceDecoder.create(Asset.of(ResourceBuilderTest.stream("/animation/still.png")));
		final Resource resource = ResourceBuilder.create().cache(null).of(asset);
		final Thread main = Thread.currentThread();
		final AtomicBoolean waited = new AtomicBoolean();
		final Thread releaser = new Thread(() -> {
			waited.set(TestUtils.awaitWaiting(main));
			asset.release.countDown();
		});
		releaser.start();
		resource.decoder(decoder);
		releaser.join();
		Assert.assertTrue("The decoder was given once the detection ended", waited.get());
		resource.getResourceData().await();
		Assert.assertSame(decoder, resource.getDecoder());
		resource.prepareBind();
		resource.getResourceData().await();
		Assert.assertTrue(resource.isLoaded());
	}

	@Test
	public void detectsTheFormatAgainOnceReloadedAfterAFailedRead() throws IOException {
		final FlakyAsset asset = new FlakyAsset("flaky", false, Asset.of(ResourceBuilderTest.stream("/animation/blink.gif")).read());
		final Resource resource = ResourceBuilder.create().cache(null).of(asset).blocking();
		Assert.assertTrue(resource.isFailed());
		resource.reload();
		resource.prepareBind();
		Assert.assertFalse(resource.isFailed());
		Assert.assertTrue(resource.getDecoder() instanceof AnimatedResourceDecoder);
	}

	@Test
	public void detectsTheFormatOfARemoteAssetAgainOnATaskThreadOnceReloaded() throws IOException {
		final FlakyAsset asset = new FlakyAsset("flaky-remote", true, Asset.of(ResourceBuilderTest.stream("/animation/blink.gif")).read());
		final Resource resource = ResourceBuilder.create().cache(null).of(asset);
		resource.getResourceData().await();
		Assert.assertTrue(resource.isFailed());
		resource.reload();
		resource.prepareBind();
		resource.getResourceData().await();
		Assert.assertTrue(resource.getDecoder() instanceof AnimatedResourceDecoder);
		Assert.assertEquals("ResourceTask/flaky-remote", asset.readers.get(0).getName());
	}

	@Test
	public void readsAStreamAgainOnceReloaded() {
		final Resource resource = ResourceBuilder.create().cache(null).of(ResourceBuilderTest.stream("/animation/still.png")).blocking();
		resource.prepareBind();
		resource.reload();
		resource.prepareBind();
		Assert.assertFalse(resource.isFailed());
		Assert.assertTrue(resource.isLoaded());
	}

	@Test
	public void waitsForABlockingDownloadOnlyWhenDrawn() throws InterruptedException {
		final HeldAsset asset = new HeldAsset("remote", true, Asset.of(ResourceBuilderTest.stream("/animation/blink.gif")));
		final Resource resource = ResourceBuilder.create().cache(null).of(asset).blocking();
		Assert.assertNull(resource.getDecoder());
		Assert.assertFalse(resource.isLoaded());
		final Thread main = Thread.currentThread();
		final AtomicBoolean waited = new AtomicBoolean();
		final Thread releaser = new Thread(() -> {
			waited.set(TestUtils.awaitWaiting(main));
			asset.release.countDown();
		});
		releaser.start();
		resource.prepareBind();
		Assert.assertTrue(resource.getDecoder() instanceof AnimatedResourceDecoder);
		Assert.assertTrue(resource.isLoaded());
		releaser.join();
		Assert.assertTrue("The download ended before the draw", waited.get());
	}

	@Test
	public void downloadsAHeldAssetOnlyOnce() {
		final HeldAsset asset = new HeldAsset("remote", true, Asset.of(ResourceBuilderTest.stream("/animation/still.png")));
		asset.release.countDown();
		final ResourceBuilder builder = ResourceBuilder.create().cache(MemoryResourceCache.create());
		final Resource first = builder.of(asset);
		final Resource second = builder.of(asset);
		first.getResourceData().await();
		Assert.assertSame(first.getResourceData(), second.getResourceData());
		Assert.assertEquals(1, asset.opened.get());
	}

	@Test
	public void computesEveryTimeWithoutCache() {
		final AtomicInteger supplied = new AtomicInteger();
		final List<Resource> created = new ArrayList<>();
		final ResourceBuilder builder = ResourceBuilder.create().cache(null);
		final Resource first = builder.compute("image", () -> ResourceData.create("image" + supplied.incrementAndGet(), null), created::add);
		builder.reload();
		final Resource second = builder.compute("image", () -> ResourceData.create("image" + supplied.incrementAndGet(), null), created::add);
		Assert.assertEquals("image1", first.getUniqueId());
		Assert.assertEquals("image2", second.getUniqueId());
		Assert.assertEquals(2, supplied.get());
		Assert.assertEquals(2, created.size());
	}

	@Test
	public void computesOnceInItsCache() {
		final AtomicInteger supplied = new AtomicInteger();
		final List<Resource> created = new ArrayList<>();
		final MemoryResourceCache cache = MemoryResourceCache.create();
		final ResourceBuilder builder = ResourceBuilder.create().cache(cache);
		final Resource first = builder.compute("image", () -> ResourceData.create("image" + supplied.incrementAndGet(), null), created::add);
		final Resource second = builder.compute("image", () -> ResourceData.create("image" + supplied.incrementAndGet(), null), created::add);
		Assert.assertSame(first.getResourceData(), second.getResourceData());
		Assert.assertSame(first.getResourceData(), cache.get("image"));
		Assert.assertSame(builder, second.getBuilder());
		Assert.assertEquals(1, supplied.get());
		Assert.assertEquals(Collections.singletonList(first), created);
	}

	@Test
	public void reloadsByEmptyingItsCache() {
		final AtomicInteger supplied = new AtomicInteger();
		final MemoryResourceCache cache = MemoryResourceCache.create();
		final ResourceBuilder builder = ResourceBuilder.create().cache(cache);
		builder.compute("image", () -> ResourceData.create("image" + supplied.incrementAndGet(), null));
		Assert.assertSame(builder, builder.reload());
		Assert.assertTrue(cache.getDataList().isEmpty());
		Assert.assertEquals("image2", builder.compute("image", () -> ResourceData.create("image" + supplied.incrementAndGet(), null)).getUniqueId());
	}

	@Test
	public void forgetsTheDataItComputedOnceCollected() throws InterruptedException, ReflectiveOperationException {
		final MemoryResourceCache cache = MemoryResourceCache.create();
		final ResourceBuilder builder = ResourceBuilder.create().cache(cache);
		for (int i = 0; i < 100; i++) {
			final String uniqueId = "image_" + i;
			builder.compute(uniqueId, () -> ResourceData.create(uniqueId, null));
		}
		cache.clear();
		final Field field = ResourceBuilder.class.getDeclaredField("computed");
		field.setAccessible(true);
		final Map<?, ?> computed = (Map<?, ?>) field.get(builder);
		final long deadline = System.currentTimeMillis() + 5000L;
		while (!computed.isEmpty() && System.currentTimeMillis() < deadline) {
			System.gc();
			Thread.sleep(10L);
		}
		Assert.assertTrue(computed.isEmpty());
	}

	@Test
	public void reloadsOnlyWhatItLoaded() {
		final MemoryResourceCache cache = MemoryResourceCache.create();
		final ResourceBuilder first = ResourceBuilder.create().cache(cache);
		final ResourceBuilder second = ResourceBuilder.create().cache(cache);
		first.compute("first", () -> ResourceData.create("first", null));
		first.compute("shared", () -> ResourceData.create("shared", null));
		second.compute("second", () -> ResourceData.create("second", null));
		second.compute("shared", () -> ResourceData.create("shared", null));
		first.reload();
		Assert.assertNull(cache.get("first"));
		Assert.assertNull(cache.get("shared"));
		Assert.assertNotNull(cache.get("second"));
		second.reload();
		Assert.assertTrue(cache.getDataList().isEmpty());
	}

	@Test
	public void notifiesALateListenerOfACachedRemoteAssetAtOnce() throws IOException {
		final List<Resource> first = new ArrayList<>();
		final List<Resource> second = new ArrayList<>();
		final RemoteAsset asset = new RemoteAsset(Asset.of(ResourceBuilderTest.class.getResourceAsStream("/animation/still.png")).read());
		final ResourceBuilder builder = ResourceBuilder.create().cache(MemoryResourceCache.create());
		final Resource loaded = builder.of(asset).blocking().onLoad(first::add);
		loaded.prepareBind();
		final Resource cached = builder.of(asset).onLoad(second::add);
		Assert.assertEquals(Collections.singletonList(loaded), first);
		Assert.assertEquals(Collections.singletonList(cached), second);
	}

	@Test
	public void storesItsDataInACustomCache() {
		final MapResourceCache cache = new MapResourceCache();
		final ResourceBuilder builder = ResourceBuilder.create().cache(cache);
		final Resource first = builder.compute("image", () -> ResourceData.create("image", null));
		final Resource second = builder.compute("image", () -> ResourceData.create("other", null));
		Assert.assertSame(cache, builder.getCache());
		Assert.assertSame(first.getResourceData(), second.getResourceData());
		Assert.assertSame(first.getResourceData(), cache.dataMap.get("image"));
		builder.reload();
		Assert.assertTrue(cache.dataMap.isEmpty());
		builder.compute("image", () -> ResourceData.create("image", null)).clear();
		Assert.assertTrue(cache.dataMap.isEmpty());
	}

	private static InputStream stream(final String path) {
		return ResourceBuilderTest.class.getResourceAsStream(path);
	}

	private static final class MapResourceCache implements IResourceCache {

		private final Map<String, ResourceData> dataMap = new HashMap<>();

		@Override
		public void put(final @NonNull String uniqueId, final @NonNull ResourceData data) {
			this.dataMap.put(uniqueId, data);
		}

		@Override
		public void remove(final @NonNull String uniqueId) {
			this.dataMap.remove(uniqueId);
		}

		@Override
		public void clear() {
			this.dataMap.clear();
		}

		@Override
		public ResourceData get(final @NonNull String uniqueId) {
			return this.dataMap.get(uniqueId);
		}

		@Override
		public @NonNull Collection<@NonNull ResourceData> getDataList() {
			return this.dataMap.values();
		}

	}

	private static final class HeldAsset extends Asset {

		private final AtomicInteger  opened  = new AtomicInteger();
		private final List<Thread>   readers = new CopyOnWriteArrayList<>();
		private final CountDownLatch release = new CountDownLatch(1);

		private final byte[]  bytes;
		@Getter private final boolean remote;

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
			this.readers.add(Thread.currentThread());
			return new ByteArrayInputStream(this.bytes);
		}

	}

	private static final class FlakyAsset extends Asset {

		private final AtomicInteger opened  = new AtomicInteger();
		private final List<Thread>  readers = new CopyOnWriteArrayList<>();

		private final byte[]  bytes;
		@Getter private final boolean remote;

		private FlakyAsset(final String uniqueId, final boolean remote, final byte[] bytes) {
			super(uniqueId);
			this.remote = remote;
			this.bytes = bytes;
		}

		@Override
		public @NonNull InputStream open() throws IOException {
			if (this.opened.getAndIncrement() == 0) {
				throw new IOException("offline");
			}

			this.readers.add(Thread.currentThread());
			return new ByteArrayInputStream(this.bytes);
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