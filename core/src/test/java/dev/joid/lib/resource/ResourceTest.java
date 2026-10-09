package dev.joid.lib.resource;

import java.awt.image.BufferedImage;
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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingTexture;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.resource.animation.impl.GifResourceAnimationReader;
import dev.joid.lib.resource.decoder.IResourceDecoder;
import dev.joid.lib.resource.decoder.impl.AnimatedResourceDecoder;
import lombok.NonNull;

public class ResourceTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void loadsThroughTheDefaultAsyncLinearBuilder() {
		final Resource resource = Resource.of(new BufferedImage(8, 4, BufferedImage.TYPE_INT_ARGB));
		Assert.assertSame(ResourceBuilder.DEFAULT_CACHE, resource.getBuilder().getCache());
		Assert.assertTrue(resource.getProperties().isAsync());
		Assert.assertSame(TextureFilter.LINEAR, resource.getProperties().getInterpolation());
		Assert.assertSame(resource.getResourceData(), ResourceBuilder.DEFAULT_CACHE.getIfPresent(resource.getUniqueId()));
	}

	@Test
	public void notifiesTheCallbackOfTheDefaultBuilder() {
		final List<Resource> received = new ArrayList<>();
		final Resource resource = Resource.of(new BufferedImage(8, 4, BufferedImage.TYPE_INT_ARGB), received::add);
		Assert.assertEquals(Collections.singletonList(resource), received);
	}

	@Test
	public void copiesThePropertiesOfItsBuilder() {
		final ResourceBuilder builder = ResourceBuilder.create().cache(null).linear();
		final Resource resource = builder.compute("image", () -> new ResourceData("image", null));
		Assert.assertNotSame(builder.getProperties(), resource.getProperties());
		resource.nearest();
		builder.async();
		Assert.assertSame(TextureFilter.LINEAR, builder.getProperties().getInterpolation());
		Assert.assertFalse(resource.getProperties().isAsync());
	}

	@Test
	public void changesItsOwnProperties() {
		final Resource resource = ResourceTest.resource(null);
		Assert.assertSame(resource, resource.async().linear().mipmap(true).textureCoords(1D, 2D, 3D, 4D));
		Assert.assertTrue(resource.getProperties().isAsync());
		Assert.assertSame(TextureFilter.LINEAR, resource.getProperties().getInterpolation());
		Assert.assertEquals(Boolean.TRUE, resource.getProperties().getMipmap());
		Assert.assertArrayEquals(new double[] {1D, 2D, 3D, 4D}, resource.getProperties().getTextureCoords(), 0D);
		Assert.assertSame(TextureFilter.NEAREST, resource.nearest().getProperties().getInterpolation());
		Assert.assertSame(TextureFilter.LINEAR, resource.interpolation(TextureFilter.LINEAR).getProperties().getInterpolation());
	}

	@Test
	public void replacesItsProperties() {
		final ResourceProperties properties = ResourceProperties.create().linear();
		final Resource resource = ResourceTest.resource(null);
		Assert.assertSame(resource, resource.properties(properties));
		Assert.assertSame(properties, resource.getProperties());
	}

	@Test
	public void resetsItsPropertiesToTheBuilder() {
		final ResourceBuilder builder = ResourceBuilder.create().cache(null).linear();
		final Resource resource = builder.compute("image", () -> new ResourceData("image", null)).async().nearest().mipmap(false);
		Assert.assertSame(resource, resource.reset());
		Assert.assertNotSame(builder.getProperties(), resource.getProperties());
		Assert.assertFalse(resource.getProperties().isAsync());
		Assert.assertSame(TextureFilter.LINEAR, resource.getProperties().getInterpolation());
		Assert.assertNull(resource.getProperties().getMipmap());
	}

	@Test
	public void copiesItselfOverTheSameData() {
		final ResourceBuilder builder = ResourceBuilder.create().cache(null);
		final Resource resource = builder.compute("image", () -> new ResourceData("image", null)).linear().mipmap(true);
		final Resource copy = resource.copy();
		Assert.assertNotSame(resource, copy);
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
		resource.getResourceData().loaded(true).uploaded(true).generated(true);
		Assert.assertTrue(resource.isLoaded());
		Assert.assertTrue(resource.isUploaded());
		Assert.assertTrue(resource.isGenerated());
	}

	@Test
	public void runsATaskAtOnceWhenBlocking() {
		final List<Thread> threads = new ArrayList<>();
		ResourceTest.resource(null).dispatch(() -> threads.add(Thread.currentThread()));
		Assert.assertEquals(Collections.singletonList(Thread.currentThread()), threads);
	}

	@Test
	public void waitsForItsTasksWhenItTurnsBlocking() {
		final List<Thread> threads = new CopyOnWriteArrayList<>();
		final AtomicBoolean done = new AtomicBoolean();
		final Resource resource = ResourceTest.resource(null).async();
		resource.dispatch(() -> {
			try {
				Thread.sleep(50L);
			} catch (final InterruptedException ignored) {}
			threads.add(Thread.currentThread());
			done.set(true);
		});
		Assert.assertSame(resource, resource.blocking());
		Assert.assertTrue(done.get());
		Assert.assertFalse(resource.getProperties().isAsync());
		Assert.assertEquals("ResourceTask/image", threads.get(0).getName());
	}

	@Test
	public void decodesAtOnceWhenBlocking() {
		final Resource resource = ResourceBuilder.create().cache(null).of(ResourceTest.image());
		resource.generate();
		Assert.assertTrue(resource.isGenerated());
		Assert.assertTrue(resource.isLoaded());
		Assert.assertEquals(2, resource.getWidth());
		Assert.assertEquals(1, resource.getHeight());
		Assert.assertArrayEquals(new int[] {0xFF336699, 0xFF336699}, resource.getData());
	}

	@Test
	public void decodesInTheBackgroundWhenAsync() throws InterruptedException {
		final Resource resource = ResourceBuilder.create().async().cache(null).of(ResourceTest.image());
		resource.generate();
		Assert.assertTrue(resource.isGenerated());
		ResourceTest.await(resource::isLoaded);
		Assert.assertEquals(2, resource.getWidth());
	}

	@Test
	public void uploadsItsPixelsIntoItsTexture() {
		final Resource resource = ResourceBuilder.create().cache(null).of(ResourceTest.image());
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
		final Resource resource = ResourceTest.resource(decoder);
		resource.request(0, 10);
		resource.request(10, 0);
		resource.request(30, 20);
		resource.async().request(40, 50);
		Assert.assertEquals(Arrays.asList("init", "request 30x20 blocking", "request 40x50 async"), decoder.calls);
		ResourceTest.resource(null).request(30, 20);
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
		final Cache<String, ResourceData> cache = CacheBuilder.newBuilder().build();
		final Resource resource = ResourceBuilder.create().cache(cache).compute("image", () -> new ResourceData("image", decoder));
		resource.prepareBind();
		resource.clear();
		Assert.assertTrue(decoder.texture.isDeleted());
		Assert.assertTrue(decoder.calls.contains("clear"));
		Assert.assertNull(cache.getIfPresent("image"));
	}

	@Test
	public void clearsWithoutCache() {
		final RecordingTexture texture = new RecordingTexture();
		ResourceBuilder.create().cache(null).of(texture).clear();
		Assert.assertTrue(texture.isDeleted());
	}

	private static Resource resource(final IResourceDecoder decoder) {
		return ResourceBuilder.create().cache(null).compute("image", () -> new ResourceData("image", decoder));
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
		public void request(final @NonNull ResourceData resource, final int width, final int height, final boolean async) {
			this.calls.add("request " + width + "x" + height + (async ? " async" : " blocking"));
		}

		@Override
		public boolean isMipmappable() {
			return this.mipmappable;
		}

	}

}