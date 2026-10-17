package dev.joid.lib.resource;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingTexture;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.resource.animation.impl.GifResourceAnimationReader;
import dev.joid.lib.resource.cache.MemoryResourceCache;
import dev.joid.lib.resource.decoder.IResourceDecoder;
import dev.joid.lib.resource.decoder.impl.AnimatedResourceDecoder;
import lombok.NonNull;

public class ResourceTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void loadsThroughTheDefaultLinearBuilderInTheBackground() {
		final Resource resource = Resource.of(new BufferedImage(8, 4, BufferedImage.TYPE_INT_ARGB));
		Assert.assertSame(ResourceBuilder.create().getCache(), resource.getBuilder().getCache());
		Assert.assertFalse(resource.isBlocking());
		Assert.assertSame(TextureFilter.LINEAR, resource.getProperties().getInterpolation());
		Assert.assertSame(resource.getResourceData(), resource.getBuilder().getCache().get(resource.getUniqueId()));
	}

	@Test
	public void notifiesItsLoadWithItself() {
		final List<Resource> loads = new ArrayList<>();
		final Resource resource = ResourceBuilder.create().cache(null).of(new BufferedImage(8, 4, BufferedImage.TYPE_INT_ARGB)).blocking().onLoad(loads::add);
		Assert.assertTrue(loads.isEmpty());
		resource.generate();
		Assert.assertTrue(resource.isLoaded());
		Assert.assertEquals(Collections.singletonList(resource), loads);
	}

	@Test
	public void copiesThePropertiesOfItsBuilder() {
		final ResourceBuilder builder = ResourceBuilder.create().cache(null).linear();
		final Resource resource = builder.compute("image", () -> new ResourceData("image", null));
		Assert.assertNotSame(builder.getProperties(), resource.getProperties());
		resource.nearest();
		Assert.assertSame(TextureFilter.LINEAR, builder.getProperties().getInterpolation());
	}

	@Test
	public void changesItsOwnProperties() {
		final Resource resource = ResourceTest.resource(null);
		Assert.assertSame(resource, resource.linear().mipmap(true).textureCoords(1D, 2D, 3D, 4D));
		Assert.assertSame(TextureFilter.LINEAR, resource.getProperties().getInterpolation());
		Assert.assertEquals(Boolean.TRUE, resource.getProperties().getMipmap());
		Assert.assertArrayEquals(new double[] {1D, 2D, 3D, 4D}, resource.getProperties().getTextureCoords(), 0D);
		Assert.assertSame(TextureFilter.NEAREST, resource.nearest().getProperties().getInterpolation());
		Assert.assertSame(TextureFilter.LINEAR, resource.interpolation(TextureFilter.LINEAR).getProperties().getInterpolation());
	}

	@Test
	public void replacesItsPropertiesWithACopy() {
		final ResourceProperties properties = ResourceProperties.create().nearest();
		final Resource resource = ResourceTest.resource(null);
		final Resource other = ResourceTest.resource(null).properties(properties);
		Assert.assertSame(resource, resource.properties(properties));
		Assert.assertSame(TextureFilter.NEAREST, resource.getProperties().getInterpolation());
		resource.linear();
		Assert.assertSame(TextureFilter.NEAREST, other.getProperties().getInterpolation());
		Assert.assertSame(TextureFilter.NEAREST, properties.getInterpolation());
	}

	@Test
	public void resetsItsPropertiesToTheBuilder() {
		final ResourceBuilder builder = ResourceBuilder.create().cache(null).linear();
		final Resource resource = builder.compute("image", () -> new ResourceData("image", null)).nearest().mipmap(false);
		Assert.assertSame(resource, resource.reset());
		Assert.assertNotSame(builder.getProperties(), resource.getProperties());
		Assert.assertSame(TextureFilter.LINEAR, resource.getProperties().getInterpolation());
		Assert.assertNull(resource.getProperties().getMipmap());
	}

	@Test
	public void copiesItselfOverTheSameData() {
		final ResourceBuilder builder = ResourceBuilder.create().cache(null);
		final Resource resource = builder.compute("image", () -> new ResourceData("image", null)).linear().mipmap(true).blocking();
		final Resource copy = resource.copy();
		Assert.assertNotSame(resource, copy);
		Assert.assertTrue(copy.isBlocking());
		Assert.assertSame(resource.getResourceData(), copy.getResourceData());
		Assert.assertSame(builder, copy.getBuilder());
		Assert.assertSame(TextureFilter.LINEAR, copy.getProperties().getInterpolation());
		Assert.assertEquals(Boolean.TRUE, copy.getProperties().getMipmap());
	}

	@Test
	public void copiesItsPropertiesApartFromItself() {
		final Resource resource = ResourceTest.resource(null).linear();
		final Resource copy = resource.copy().nearest();
		Assert.assertNotSame(resource.getProperties(), copy.getProperties());
		Assert.assertSame(TextureFilter.LINEAR, resource.getProperties().getInterpolation());
		Assert.assertSame(TextureFilter.NEAREST, copy.getProperties().getInterpolation());
	}

	@Test
	public void renamesItsData() {
		final Resource resource = ResourceTest.resource(null);
		Assert.assertSame(resource, resource.uniqueId("renamed"));
		Assert.assertEquals("renamed", resource.getUniqueId());
		Assert.assertEquals("renamed", resource.getResourceData().getUniqueId());
	}

	@Test
	public void replacesItsDecoder() {
		final Decoder decoder = new Decoder(true);
		final Resource resource = ResourceTest.resource(null);
		Assert.assertSame(resource, resource.decoder(decoder));
		Assert.assertSame(decoder, resource.getDecoder());
		Assert.assertSame(decoder, resource.getResourceData().getDecoder());
		Assert.assertEquals(Collections.singletonList("init"), decoder.calls);
	}

	@Test
	public void releasesItsLoadedDecoderOnceReplaced() {
		final Decoder previous = new Decoder(true);
		final Decoder decoder = new Decoder(true);
		final Resource resource = ResourceTest.resource(previous);
		resource.bindTextureOnly(TextureWrap.CLAMP_TO_EDGE);
		resource.decoder(decoder);
		resource.bindTextureOnly(TextureWrap.CLAMP_TO_EDGE);
		Assert.assertEquals("clear", previous.calls.get(previous.calls.size() - 1));
		Assert.assertEquals(Arrays.asList("init", "prepare", "decode", "upload", "update"), decoder.calls);
		Assert.assertSame(decoder.texture, this.bridges.getRender().getState().getTexture());
	}

	@Test
	public void readsItsSizeAndPixelsFromItsData() {
		final int[] first = {1, 2};
		final int[] second = {3, 4};
		final Resource resource = ResourceTest.resource(null);
		resource.getResourceData().width(2).height(1).data(new int[][] {first, second});
		Assert.assertEquals(2, resource.getWidth());
		Assert.assertEquals(1, resource.getHeight());
		Assert.assertSame(first, resource.getData());
		Assert.assertSame(second, resource.getData(1));
		Assert.assertNull(resource.getData(2));
	}

	@Test
	public void hasNoPixelsWithoutData() {
		final Resource resource = ResourceTest.resource(null);
		Assert.assertNull(resource.getData());
		Assert.assertNull(resource.getData(0));
		resource.getResourceData().data(new int[0][]);
		Assert.assertNull(resource.getData());
	}

	@Test
	public void readsItsTextures() {
		final RecordingTexture first = new RecordingTexture();
		final RecordingTexture second = new RecordingTexture();
		final Resource resource = ResourceTest.resource(null);
		Assert.assertNull(resource.getTexture());
		Assert.assertNull(resource.getTexture(0));
		resource.getResourceData().textures(new ITexture[0]);
		Assert.assertNull(resource.getTexture());
		resource.getResourceData().textures(new ITexture[] {first, second});
		Assert.assertSame(first, resource.getTexture());
		Assert.assertSame(second, resource.getTexture(1));
		Assert.assertNull(resource.getTexture(2));
	}

	@Test
	public void isMipmappableUnlessItsDecoderRefuses() {
		Assert.assertTrue(ResourceTest.resource(null).isMipmappable());
		Assert.assertTrue(ResourceTest.resource(new Decoder(true)).isMipmappable());
		Assert.assertFalse(ResourceTest.resource(new Decoder(false)).isMipmappable());
	}

	@Test
	public void exposesThePlaybackOfAnAnimation() {
		final AnimatedResourceDecoder decoder = new AnimatedResourceDecoder(Asset.of(ResourceTest.class.getResourceAsStream("/animation/blink.gif")), new GifResourceAnimationReader());
		Assert.assertSame(decoder, ResourceTest.resource(decoder).getPlayback());
		Assert.assertNull(ResourceTest.resource(new Decoder(true)).getPlayback());
		Assert.assertNull(ResourceTest.resource(null).getPlayback());
	}

	@Test
	public void reflectsTheStateOfItsData() {
		final Resource resource = ResourceTest.resource(null);
		Assert.assertFalse(resource.isLoaded());
		Assert.assertFalse(resource.isUploaded());
		Assert.assertFalse(resource.isGenerated());
		ResourceDataTest.upload(ResourceDataTest.load(resource.getResourceData())).generated(true);
		Assert.assertTrue(resource.isLoaded());
		Assert.assertTrue(resource.isUploaded());
		Assert.assertTrue(resource.isGenerated());
	}

	@Test
	public void runsATaskOnItsOwnThread() {
		final List<Thread> threads = new CopyOnWriteArrayList<>();
		final Resource resource = ResourceTest.resource(null);
		resource.getResourceData().dispatch(() -> threads.add(Thread.currentThread()));
		resource.getResourceData().await();
		Assert.assertEquals("ResourceTask/image", threads.get(0).getName());
	}

	@Test
	public void waitsForItsTasksOnlyWhenDrawn() {
		final List<Thread> threads = new CopyOnWriteArrayList<>();
		final AtomicBoolean done = new AtomicBoolean();
		final Resource resource = ResourceBuilder.create().cache(null).compute("image", () -> new ResourceData("image", null));
		resource.getResourceData().dispatch(() -> {
			try {
				Thread.sleep(50L);
			} catch (final InterruptedException ignored) {}
			threads.add(Thread.currentThread());
			done.set(true);
		});
		Assert.assertSame(resource, resource.blocking());
		Assert.assertTrue(resource.isBlocking());
		Assert.assertFalse(done.get());
		resource.prepareBind();
		Assert.assertTrue(done.get());
		Assert.assertEquals("ResourceTask/image", threads.get(0).getName());
	}

	@Test
	public void decodesAtOnceWhenBlocking() {
		final Resource resource = ResourceBuilder.create().cache(null).of(ResourceTest.image()).blocking();
		resource.generate();
		Assert.assertTrue(resource.isGenerated());
		Assert.assertTrue(resource.isLoaded());
		Assert.assertEquals(2, resource.getWidth());
		Assert.assertEquals(1, resource.getHeight());
		Assert.assertArrayEquals(new int[] {0xFF336699, 0xFF336699}, resource.getData());
	}

	@Test
	public void decodesInTheBackgroundByDefault() throws InterruptedException {
		final Resource resource = ResourceBuilder.create().cache(null).of(ResourceTest.image());
		resource.generate();
		Assert.assertTrue(resource.isGenerated());
		ResourceTest.await(resource::isLoaded);
		Assert.assertEquals(2, resource.getWidth());
	}

	@Test
	public void uploadsItsPixelsIntoItsTexture() {
		final Resource resource = ResourceBuilder.create().cache(null).of(ResourceTest.image()).blocking();
		resource.generate();
		resource.upload();
		final RecordingTexture texture = (RecordingTexture) resource.getTexture();
		Assert.assertTrue(resource.isUploaded());
		Assert.assertNull(resource.getData());
		Assert.assertEquals(2, texture.getWidth());
		Assert.assertEquals(1, texture.getHeight());
		Assert.assertArrayEquals(new int[] {0xFF336699, 0xFF336699}, texture.getPixels());
	}

	@Test
	public void bindsItsTextureWhileTheTaskRuns() {
		final RecordingTexture texture = new RecordingTexture();
		final List<Object> bound = new ArrayList<>();
		final Resource resource = ResourceBuilder.create().cache(null).linear().of(texture);
		resource.bind(TextureWrap.CLAMP_TO_EDGE, () -> {
			final RenderState state = this.bridges.getRender().getState();
			bound.addAll(Arrays.asList(state.getTexture(), state.getTextureFilter(), state.getTextureWrap()));
		});
		Assert.assertEquals(Arrays.asList(texture, TextureFilter.LINEAR, TextureWrap.CLAMP_TO_EDGE), bound);
		Assert.assertNull(this.bridges.getRender().getState().getTexture());
	}

	@Test
	public void unbindsEvenWhenTheTaskFails() {
		final Resource resource = ResourceBuilder.create().cache(null).of(new RecordingTexture());
		try {
			resource.bind(TextureWrap.CLAMP_TO_BORDER, () -> {
				throw new IllegalStateException("draw");
			});
			Assert.fail("The task must fail");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("draw", expected.getMessage());
		}
		Assert.assertNull(this.bridges.getRender().getState().getTexture());
	}

	@Test
	public void unbindsItsTexture() {
		final Resource resource = ResourceBuilder.create().cache(null).of(new RecordingTexture());
		resource.bindTextureOnly(TextureWrap.CLAMP_TO_EDGE);
		Assert.assertSame(resource.getTexture(), this.bridges.getRender().getState().getTexture());
		resource.unbind();
		Assert.assertNull(this.bridges.getRender().getState().getTexture());
	}

	@Test
	public void bindsNoTextureWithoutOne() {
		this.bridges.getRender().getState().texture(new RecordingTexture()).textureFilter(TextureFilter.LINEAR).textureWrap(TextureWrap.CLAMP_TO_EDGE);
		ResourceTest.resource(null).bindTextureOnly(TextureWrap.CLAMP_TO_EDGE);
		Assert.assertNull(this.bridges.getRender().getState().getTexture());
	}

	@Test
	public void decodesUploadsAndUpdatesItsDecoderWhenBound() {
		final Decoder decoder = new Decoder(true);
		final Resource resource = ResourceTest.resource(decoder);
		resource.bindTextureOnly(TextureWrap.CLAMP_TO_EDGE);
		Assert.assertEquals(Arrays.asList("init", "prepare", "decode", "upload", "update"), decoder.calls);
		Assert.assertSame(decoder.texture, this.bridges.getRender().getState().getTexture());
		resource.bindTextureOnly(TextureWrap.CLAMP_TO_EDGE);
		Assert.assertEquals(Arrays.asList("init", "prepare", "decode", "upload", "update", "update"), decoder.calls);
	}

	@Test
	public void requestsItsDrawnSizeFromItsDecoder() {
		final Decoder decoder = new Decoder(true);
		final Resource resource = ResourceBuilder.create().cache(null).compute("image", () -> new ResourceData("image", decoder));
		resource.request(0, 10);
		resource.request(10, 0);
		resource.request(30, 20);
		resource.blocking().request(40, 50);
		Assert.assertEquals(Arrays.asList("init", "request 30x20 async", "request 40x50 blocking resizing"), decoder.calls);
		ResourceTest.resource(null).request(30, 20);
	}

	@Test
	public void resizesUntilItsDrawnSizeRests() {
		final Decoder decoder = new Decoder(true);
		final Resource resource = ResourceBuilder.create().cache(null).compute("image", () -> new ResourceData("image", decoder));
		resource.request(30, 20);
		resource.request(40, 50);
		this.bridges.getClock().advance(199L);
		resource.request(40, 50);
		this.bridges.getClock().advance(1L);
		resource.request(40, 50);
		Assert.assertEquals(Arrays.asList("init", "request 30x20 async", "request 40x50 async resizing", "request 40x50 async resizing", "request 40x50 async"), decoder.calls);
	}

	@Test
	public void restsAtTheSizeOfEachOfItsCopies() {
		final Decoder decoder = new Decoder(true);
		final Resource large = ResourceBuilder.create().cache(null).compute("image", () -> new ResourceData("image", decoder));
		final Resource small = large.copy();
		large.request(100, 100);
		small.request(24, 24);
		large.request(100, 100);
		small.request(24, 24);
		Assert.assertEquals(Arrays.asList("init", "request 100x100 async", "request 24x24 async", "request 100x100 async", "request 24x24 async"), decoder.calls);
	}

	@Test
	public void mipmapsItsTexturesWhenAsked() {
		final RecordingTexture texture = new RecordingTexture();
		ResourceBuilder.create().cache(null).mipmap(true).of(texture).prepareBind();
		Assert.assertTrue(texture.isMipmapped());
	}

	@Test
	public void keepsItsTexturesPlainWithoutMipmap() {
		final RecordingTexture unset = new RecordingTexture();
		final RecordingTexture refused = new RecordingTexture();
		ResourceBuilder.create().cache(null).of(unset).prepareBind();
		ResourceBuilder.create().cache(null).mipmap(false).of(refused).prepareBind();
		Assert.assertFalse(unset.isMipmapped());
		Assert.assertFalse(refused.isMipmapped());
	}

	@Test
	public void neverMipmapsTheTexturesOfAnUnmipmappableDecoder() {
		final Decoder decoder = new Decoder(false);
		ResourceTest.resource(decoder).mipmap(true).prepareBind();
		Assert.assertFalse(decoder.texture.isMipmapped());
	}

	@Test
	public void clearsItsDataAndLeavesItsCache() {
		final Decoder decoder = new Decoder(true);
		final MemoryResourceCache cache = MemoryResourceCache.create();
		final Resource resource = ResourceBuilder.create().cache(cache).compute("image", () -> new ResourceData("image", decoder)).blocking();
		resource.prepareBind();
		resource.clear();
		Assert.assertTrue(decoder.texture.isDeleted());
		Assert.assertTrue(decoder.calls.contains("clear"));
		Assert.assertNull(cache.get("image"));
	}

	@Test
	public void leavesItsCacheUnderItsFirstIdOnceRenamed() {
		final MemoryResourceCache cache = MemoryResourceCache.create();
		final Resource resource = ResourceBuilder.create().cache(cache).compute("image", () -> new ResourceData("image", null)).uniqueId("renamed");
		resource.clear();
		Assert.assertNull(cache.get("image"));
		Assert.assertTrue(cache.getDataList().isEmpty());
	}

	@Test
	public void loadsAgainOnceCleared() {
		final Decoder decoder = new Decoder(true);
		final Resource resource = ResourceTest.resource(decoder);
		resource.prepareBind();
		resource.clear();
		Assert.assertFalse(resource.isLoaded());
		resource.prepareBind();
		Assert.assertTrue(resource.isLoaded());
		Assert.assertEquals(Arrays.asList("init", "prepare", "decode", "upload", "clear", "prepare", "decode", "upload"), decoder.calls);
	}

	@Test
	public void reloadsWithoutLeavingItsCache() {
		final Decoder decoder = new Decoder(true);
		final MemoryResourceCache cache = MemoryResourceCache.create();
		final Resource resource = ResourceBuilder.create().cache(cache).compute("image", () -> new ResourceData("image", decoder)).blocking();
		resource.prepareBind();
		Assert.assertSame(resource, resource.reload());
		Assert.assertFalse(resource.isLoaded());
		Assert.assertSame(resource.getResourceData(), cache.get("image"));
		resource.prepareBind();
		Assert.assertTrue(resource.isLoaded());
	}

	@Test
	public void leavesTheCacheEntryOfAnotherData() {
		final MemoryResourceCache cache = MemoryResourceCache.create();
		final ResourceBuilder builder = ResourceBuilder.create().cache(cache);
		final ResourceData cached = builder.compute("image", () -> new ResourceData("image", null)).getResourceData();
		builder.cache(null).compute("image", () -> new ResourceData("image", null)).clear();
		Assert.assertSame(cached, cache.get("image"));
	}

	@Test
	public void neverDeletesAGivenTexture() {
		final ITexture texture = new RecordingTexture().allocate(4, 2);
		final Resource resource = ResourceBuilder.create().cache(null).of(texture);
		resource.prepareBind();
		resource.clear();
		Assert.assertFalse(((RecordingTexture) texture).isDeleted());
		resource.prepareBind();
		Assert.assertTrue(resource.isLoaded());
		Assert.assertSame(texture, resource.getTexture());
	}

	@Test
	public void measuresItsTextureCoordinates() {
		final Resource resource = ResourceTest.resource(null);
		resource.getResourceData().width(40).height(20);
		Assert.assertEquals(40, resource.getWidth());
		Assert.assertEquals(20, resource.getHeight());
		resource.textureCoords(5D, 5D, 10D, 8D);
		Assert.assertEquals(10, resource.getWidth());
		Assert.assertEquals(8, resource.getHeight());
		Assert.assertEquals(40, resource.getSourceWidth());
		Assert.assertEquals(20, resource.getSourceHeight());
	}

	@Test
	public void sharesItselfWhenItsDecoderHasNoPlayback() {
		final Resource resource = ResourceTest.resource(new Decoder(true));
		Assert.assertSame(resource, resource.fork());
		Assert.assertTrue(resource.getResourceData().getForks().isEmpty());
		final Resource empty = ResourceTest.resource(null);
		Assert.assertSame(empty, empty.fork());
	}

	@Test
	public void forksItsPlaybackOverTheSameAsset() {
		final AnimatedResourceDecoder decoder = new AnimatedResourceDecoder(Asset.of(ResourceTest.class.getResourceAsStream("/animation/blink.gif")), new GifResourceAnimationReader());
		final Resource resource = ResourceTest.resource(decoder).linear();
		final Resource fork = resource.fork();
		Assert.assertNotSame(resource, fork);
		Assert.assertNotSame(resource.getResourceData(), fork.getResourceData());
		Assert.assertNotSame(decoder, fork.getDecoder());
		Assert.assertSame(decoder, ResourceTest.field(fork.getDecoder(), "origin"));
		Assert.assertEquals(Collections.singletonList(fork.getResourceData()), resource.getResourceData().getForks());
		Assert.assertTrue(fork.isBlocking());
		Assert.assertSame(TextureFilter.LINEAR, fork.getProperties().getInterpolation());
	}

	private static Resource resource(final IResourceDecoder decoder) {
		return ResourceBuilder.create().cache(null).compute("image", () -> new ResourceData("image", decoder)).blocking();
	}

	private static BufferedImage image() {
		final BufferedImage image = new BufferedImage(2, 1, BufferedImage.TYPE_INT_ARGB);
		image.setRGB(0, 0, 0xFF336699);
		image.setRGB(1, 0, 0xFF336699);
		return image;
	}

	private static void await(final BooleanSupplier condition) throws InterruptedException {
		final long deadline = System.currentTimeMillis() + 5000L;
		while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
			Thread.sleep(1L);
		}
		Assert.assertTrue(condition.getAsBoolean());
	}

	private static Object field(final Object target, final String name) {
		try {
			final Field field = target.getClass().getDeclaredField(name);
			field.setAccessible(true);
			return field.get(target);
		} catch (final ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

	private static final class Decoder implements IResourceDecoder {

		private final List<String>     calls   = new CopyOnWriteArrayList<>();
		private final RecordingTexture texture = new RecordingTexture();

		private final boolean mipmappable;

		private Decoder(final boolean mipmappable) {
			this.mipmappable = mipmappable;
		}

		@Override
		public void init(final @NonNull ResourceData resource) {
			this.calls.add("init");
		}

		@Override
		public void clear(final @NonNull ResourceData resource) {
			this.calls.add("clear");
		}

		@Override
		public void decode(final @NonNull ResourceData resource) {
			this.calls.add("decode");
			resource.width(1).height(1).data(new int[][] {{1}});
		}

		@Override
		public void update(final @NonNull ResourceData resource) {
			this.calls.add("update");
		}

		@Override
		public void upload(final @NonNull ResourceData resource) {
			this.calls.add("upload");
		}

		@Override
		public void prepare(final @NonNull ResourceData resource) {
			this.calls.add("prepare");
			resource.texture(this.texture);
		}

		@Override
		public void request(final @NonNull ResourceData resource, final int width, final int height, final boolean async, final boolean resizing) {
			this.calls.add("request " + width + "x" + height + (async ? " async" : " blocking") + (resizing ? " resizing" : ""));
		}

		@Override
		public boolean isMipmappable() {
			return this.mipmappable;
		}

	}

}