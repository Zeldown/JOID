package dev.joid.lib.resource;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.thread.QueueThreadBridge;
import dev.joid.lib.resource.decoder.IResourceDecoder;
import lombok.Getter;
import lombok.NonNull;

public class ResourceDataTest {

	@Test
	public void initializesItsDecoder() {
		final Decoder decoder = new Decoder();
		final ResourceData data = new ResourceData("image", decoder);
		Assert.assertEquals("image", data.getUniqueId());
		Assert.assertSame(decoder, data.getDecoder());
		Assert.assertEquals(Collections.singletonList("init"), decoder.calls);
	}

	@Test
	public void startsEmpty() {
		final ResourceData data = new ResourceData("image", null);
		Assert.assertNull(data.getDecoder());
		Assert.assertNull(data.getData());
		Assert.assertNull(data.getTextures());
		Assert.assertFalse(data.isLoaded());
		Assert.assertFalse(data.isUploaded());
		Assert.assertFalse(data.isGenerated());
		Assert.assertEquals(0, data.getWidth());
		Assert.assertEquals(0, data.getHeight());
		Assert.assertTrue(data.getTasks().isEmpty());
	}

	@Test
	public void changesEveryProperty() {
		final Decoder decoder = new Decoder();
		final int[][] pixels = {{1, 2}};
		final ITexture[] textures = {new Texture(2, 1)};
		final ResourceData data = ResourceDataTest.upload(ResourceDataTest.load(new ResourceData("image", null).uniqueId("renamed").decoder(decoder).data(pixels).textures(textures).width(2).height(1).generated(true)));
		Assert.assertEquals("renamed", data.getUniqueId());
		Assert.assertSame(decoder, data.getDecoder());
		Assert.assertArrayEquals(pixels, data.getData());
		Assert.assertArrayEquals(textures, data.getTextures());
		Assert.assertNotSame(textures, data.getTextures());
		Assert.assertEquals(2, data.getWidth());
		Assert.assertEquals(1, data.getHeight());
		Assert.assertTrue(data.isLoaded());
		Assert.assertTrue(data.isUploaded());
		Assert.assertTrue(data.isGenerated());
		Assert.assertEquals(Collections.singletonList("init"), decoder.calls);
	}

	@Test
	public void initializesTheDecoderItIsGivenLater() {
		final Decoder first = new Decoder();
		final Decoder second = new Decoder();
		final ResourceData data = new ResourceData("image", first);
		Assert.assertSame(data, data.decoder(second));
		Assert.assertSame(second, data.getDecoder());
		Assert.assertEquals(Collections.singletonList("init"), second.calls);
		Assert.assertSame(data, data.decoder(null));
		Assert.assertNull(data.getDecoder());
	}

	@Test
	public void wrapsASingleTexture() {
		final Texture texture = new Texture(2, 1);
		Assert.assertArrayEquals(new ITexture[] {texture}, new ResourceData("image", null).texture(texture).getTextures());
	}

	@Test
	public void runsAnAsyncTaskOnItsOwnDaemonThread() {
		final List<Thread> threads = new CopyOnWriteArrayList<>();
		final ResourceData data = new ResourceData("image", null);
		data.dispatch(() -> threads.add(Thread.currentThread()));
		data.await();
		Assert.assertEquals(data.getTasks(), threads);
		Assert.assertEquals("ResourceTask/image", threads.get(0).getName());
		Assert.assertTrue(threads.get(0).isDaemon());
		Assert.assertFalse(threads.get(0).isAlive());
		data.await();
	}

	@Test
	public void keepsTheInterruptionWhileAwaiting() {
		final CountDownLatch release = new CountDownLatch(1);
		final ResourceData data = new ResourceData("image", null);
		data.dispatch(() -> {
			try {
				release.await();
			} catch (final InterruptedException ignored) {}
		});

		Thread.currentThread().interrupt();
		data.await();
		Assert.assertTrue(Thread.interrupted());
		release.countDown();
		data.await();
	}

	@Test
	public void generatesThroughItsDecoder() {
		final Decoder decoder = new Decoder();
		final ResourceData data = ResourceDataTest.upload(new ResourceData("image", decoder));
		data.generate(false);
		Assert.assertEquals(Arrays.asList("init", "prepare", "decode"), decoder.calls);
		Assert.assertTrue(data.isGenerated());
		Assert.assertTrue(data.isLoaded());
		Assert.assertFalse(data.isUploaded());
	}

	@Test
	public void decodesInTheBackgroundWhenAsync() throws InterruptedException {
		final Decoder decoder = new Decoder();
		final ResourceData data = new ResourceData("image", decoder);
		data.generate(true);
		Assert.assertTrue(data.isGenerated());
		Assert.assertTrue(decoder.decoded.await(5L, TimeUnit.SECONDS));
		Assert.assertEquals(Arrays.asList("init", "prepare", "decode"), decoder.calls);
		Assert.assertTrue(decoder.thread.getName(), decoder.thread.getName().startsWith("ResourceAsync/"));
	}

	@Test
	public void takesTheSizeOfItsTextureWithoutDecoder() {
		final ResourceData data = ResourceDataTest.upload(new ResourceData("image", null).texture(new Texture(4, 2)));
		data.generate(false);
		Assert.assertTrue(data.isGenerated());
		Assert.assertTrue(data.isLoaded());
		Assert.assertFalse(data.isUploaded());
		Assert.assertEquals(4, data.getWidth());
		Assert.assertEquals(2, data.getHeight());
	}

	@Test
	public void staysUngeneratedWithoutDecoderNorTexture() {
		final ResourceData data = new ResourceData("image", null);
		data.generate(false);
		Assert.assertFalse(data.isGenerated());
		data.textures(new ITexture[0]).generate(false);
		Assert.assertFalse(data.isGenerated());
		Assert.assertFalse(data.isLoaded());
	}

	@Test
	public void uploadsNothingWithoutData() {
		final Texture texture = new Texture(2, 1);
		final ResourceData data = new ResourceData("image", null).texture(texture);
		data.upload();
		Assert.assertFalse(data.isUploaded());
		Assert.assertNull(texture.getPixels());
	}

	@Test
	public void uploadsEveryFrameToItsTexture() {
		final int[] pixels = {1, 2};
		final Texture first = new Texture(0, 0);
		final Texture second = new Texture(0, 0);
		final ResourceData data = new ResourceData("image", null).textures(new ITexture[] {first, second}).data(new int[][] {pixels, null}).width(2).height(1);
		data.upload();
		Assert.assertSame(pixels, first.getPixels());
		Assert.assertEquals(2, first.getWidth());
		Assert.assertEquals(1, first.getHeight());
		Assert.assertNull(second.getPixels());
		Assert.assertEquals(0, second.getWidth());
		Assert.assertTrue(data.isUploaded());
		Assert.assertNull(data.getData());
	}

	@Test
	public void uploadsThroughItsDecoder() {
		final Decoder decoder = new Decoder();
		final ResourceData data = new ResourceData("image", decoder).data(new int[][] {{1}});
		data.upload();
		Assert.assertEquals(Arrays.asList("init", "upload"), decoder.calls);
		Assert.assertTrue(data.isUploaded());
		Assert.assertNull(data.getData());
	}

	@Test
	public void clearsItsTexturesDataAndDecoder() {
		final Decoder decoder = new Decoder();
		final Texture texture = new Texture(1, 1);
		final ResourceData data = new ResourceData("image", decoder).texture(texture).data(new int[][] {{1}});
		data.clear();
		Assert.assertTrue(texture.isDeleted());
		Assert.assertNull(data.getData());
		Assert.assertEquals(Arrays.asList("init", "clear"), decoder.calls);
	}

	@Test
	public void loadsAgainOnceCleared() {
		final Decoder decoder = new Decoder();
		final ResourceData data = new ResourceData("image", decoder).texture(new Texture(1, 1));
		data.generate(false);
		Assert.assertTrue(data.isLoaded());
		data.clear();
		Assert.assertFalse(data.isGenerated());
		Assert.assertFalse(data.isLoaded());
		Assert.assertFalse(data.isUploaded());
		Assert.assertNull(data.getTextures());
		data.generate(false);
		Assert.assertTrue(data.isLoaded());
		Assert.assertEquals(Arrays.asList("init", "prepare", "decode", "clear", "prepare", "decode"), decoder.calls);
	}

	@Test
	public void keepsAGivenTextureOnClear() {
		final Texture texture = new Texture(4, 2);
		final ResourceData data = new ResourceData("image", null).texture(texture);
		data.generate(false);
		data.clear();
		Assert.assertFalse(texture.isDeleted());
		Assert.assertArrayEquals(new ITexture[] {texture}, data.getTextures());
		data.generate(false);
		Assert.assertTrue(data.isLoaded());
		Assert.assertEquals(4, data.getWidth());
	}

	@Test
	public void waitsForItsBackgroundDecode() {
		final CountDownLatch release = new CountDownLatch(1);
		final ResourceData data = new ResourceData("image", new Decoder() {

			@Override
			public void decode(final @NonNull ResourceData resource) {
				try {
					release.await();
				} catch (final InterruptedException ignored) {}
			}

		});
		data.generate(true);
		Assert.assertFalse(data.isLoaded());
		release.countDown();
		data.await();
		Assert.assertTrue(data.isLoaded());
	}

	@Test
	public void clearsWithoutWaitingForItsDecode() throws InterruptedException {
		final CountDownLatch release = new CountDownLatch(1);
		final Decoder decoder = new Decoder() {

			@Override
			public void decode(final @NonNull ResourceData resource) {
				try {
					release.await();
				} catch (final InterruptedException ignored) {}
				super.decode(resource);
			}

		};
		final Texture texture = new Texture(1, 1);
		final ResourceData data = new ResourceData("image", decoder).texture(texture);
		data.generate(true);
		final Thread clearing = new Thread(data::clear);
		clearing.start();
		clearing.join(1000L);
		Assert.assertFalse(clearing.isAlive());
		Assert.assertFalse(data.isGenerated());
		Assert.assertNull(data.getTextures());
		Assert.assertFalse(decoder.calls.contains("clear"));
		data.generate(true);
		Assert.assertFalse(data.isGenerated());
		release.countDown();
		data.await();
		Assert.assertFalse(data.isLoaded());
		Assert.assertTrue(texture.isDeleted());
		Assert.assertTrue(decoder.calls.contains("clear"));
		data.generate(false);
		Assert.assertTrue(data.isLoaded());
	}

	@Test
	public void listsTheForksOfItsDecoder() {
		final ResourceData data = new ResourceData("video", null);
		final Decoder decoder = new Decoder();
		final ResourceData fork = data.fork(decoder);
		Assert.assertEquals("video", fork.getUniqueId());
		Assert.assertSame(decoder, fork.getDecoder());
		Assert.assertEquals(Collections.singletonList(fork), data.getForks());
		Assert.assertTrue(fork.getForks().isEmpty());
	}

	@Test
	public void reportsItsFailureOnTheRenderThread() {
		final QueueThreadBridge thread = new QueueThreadBridge();
		final List<String> errors = new ArrayList<>();
		final ResourceData data = new ResourceData("image", null);
		final Resource resource = ResourceDataTest.resource(data).onError((failed, error) -> errors.add(error.getMessage()));
		BridgeHandler.THREAD.register(thread);
		try {
			data.fail(new IllegalStateException("missing"));
			resource.onError((failed, error) -> errors.add("late " + error.getMessage()));
			Assert.assertTrue(errors.isEmpty());
			thread.run();
			Assert.assertEquals(Arrays.asList("missing", "late missing"), errors);
		} finally {
			BridgeHandler.THREAD.unregister(thread);
		}
	}

	@Test
	public void notifiesItsLoadOnceDecoded() {
		final List<String> loads = new ArrayList<>();
		final ResourceData data = new ResourceData("image", new Decoder());
		final Resource resource = ResourceDataTest.resource(data).onLoad(loaded -> loads.add("load"));
		Assert.assertTrue(loads.isEmpty());
		data.generate(false);
		Assert.assertEquals(Collections.singletonList("load"), loads);
		resource.onLoad(loaded -> loads.add("late"));
		Assert.assertEquals(Arrays.asList("load", "late"), loads);
	}

	@Test
	public void notifiesItsLoadOnTheRenderThread() {
		final QueueThreadBridge thread = new QueueThreadBridge();
		final List<String> loads = new ArrayList<>();
		final ResourceData data = new ResourceData("image", new Decoder());
		final Resource resource = ResourceDataTest.resource(data).onLoad(loaded -> loads.add("load"));
		BridgeHandler.THREAD.register(thread);
		try {
			data.generate(false);
			resource.onLoad(loaded -> loads.add("late"));
			Assert.assertTrue(loads.isEmpty());
			thread.run();
			Assert.assertEquals(Arrays.asList("load", "late"), loads);
		} finally {
			BridgeHandler.THREAD.unregister(thread);
		}
	}

	@Test
	public void notifiesTheLoadOfAGivenTexture() {
		final List<String> loads = new ArrayList<>();
		final ResourceData data = new ResourceData("image", null).texture(new Texture(4, 2));
		final Resource resource = ResourceDataTest.resource(data).onLoad(loaded -> loads.add("load"));
		data.generate(false);
		Assert.assertTrue(resource.isLoaded());
		Assert.assertEquals(Collections.singletonList("load"), loads);
	}

	@Test
	public void notifiesItsLoadOncePerLoad() {
		final List<String> loads = new ArrayList<>();
		final ResourceData data = new ResourceData("image", new Decoder());
		final Resource resource = ResourceDataTest.resource(data).onLoad(loaded -> loads.add("load"));
		ResourceDataTest.load(ResourceDataTest.load(data));
		Assert.assertTrue(resource.isLoaded());
		Assert.assertEquals(1, loads.size());
		data.reload(new Decoder());
		Assert.assertEquals(1, loads.size());
		data.generate(false);
		Assert.assertEquals(2, loads.size());
	}

	@Test
	public void notifiesOnlyTheResourcesThatListen() {
		final List<String> loads = new ArrayList<>();
		final ResourceData data = new ResourceData("image", new Decoder());
		final Resource listening = ResourceDataTest.resource(data).onLoad(loaded -> loads.add("listening"));
		final Resource copy = listening.copy();
		data.generate(false);
		Assert.assertEquals(Collections.singletonList("listening"), loads);
		Assert.assertTrue(copy.isLoaded());
	}

	@Test
	public void forgetsTheListenersOfAResourceNoLongerUsed() throws InterruptedException {
		final List<String> loads = new ArrayList<>();
		final ResourceData data = new ResourceData("image", new Decoder());
		final WeakReference<Resource> reference = new WeakReference<>(ResourceDataTest.resource(data).onLoad(loaded -> loads.add("load")));
		final long deadline = System.currentTimeMillis() + 5000L;
		while (reference.get() != null && System.currentTimeMillis() < deadline) {
			System.gc();
			Thread.sleep(10L);
		}
		data.generate(false);
		Assert.assertNull(reference.get());
		Assert.assertTrue(loads.isEmpty());
	}

	@Test
	public void notifiesNoLoadOfAFailedDecode() {
		final List<String> loads = new ArrayList<>();
		final ResourceData data = new ResourceData("image", new Decoder() {

			@Override
			public void decode(final @NonNull ResourceData resource) {
				throw new IllegalStateException("broken");
			}

		});
		final Resource resource = ResourceDataTest.resource(data).onLoad(loaded -> loads.add("load"));
		data.generate(false);
		Assert.assertTrue(resource.isFailed());
		Assert.assertTrue(loads.isEmpty());
	}

	@Test
	public void failsOnAnErrorOfItsAsyncDecode() {
		final ResourceData data = new ResourceData("image", new Decoder() {

			@Override
			public void decode(final @NonNull ResourceData resource) {
				throw new LinkageError("The native library is missing");
			}

		});
		data.generate(true);
		data.await();
		Assert.assertTrue(data.isFailed());
	}

	@Test
	public void reloadsAFailedResourceWithANewDecoder() {
		final ResourceData data = new ResourceData("image", null);
		data.fail(new IllegalStateException("missing"));
		final Decoder decoder = new Decoder();
		Assert.assertSame(data, data.reload(decoder));
		Assert.assertFalse(data.isFailed());
		Assert.assertFalse(data.isGenerated());
		Assert.assertSame(decoder, data.getDecoder());
		data.generate(false);
		Assert.assertTrue(data.isLoaded());
		Assert.assertEquals(Arrays.asList("init", "prepare", "decode"), decoder.calls);
	}

	@Test
	public void releasesWhatItsDecoderMadeOnReload() {
		final Decoder previous = new Decoder();
		final Texture texture = new Texture(4, 2);
		final ResourceData data = ResourceDataTest.upload(ResourceDataTest.load(new ResourceData("image", previous).texture(texture).width(4).height(2).generated(true)));
		data.reload(new Decoder());
		Assert.assertTrue(texture.isDeleted());
		Assert.assertNull(data.getTextures());
		Assert.assertEquals(Arrays.asList("init", "clear"), previous.calls);
		Assert.assertEquals(0, data.getWidth());
		Assert.assertFalse(data.isLoaded());
		Assert.assertFalse(data.isUploaded());
	}

	@Test
	public void keepsAGivenTextureOnReload() {
		final Texture texture = new Texture(4, 2);
		final ResourceData data = new ResourceData("image", null).texture(texture);
		data.generate(false);
		data.reload(null);
		Assert.assertFalse(texture.isDeleted());
		Assert.assertFalse(data.isGenerated());
		data.generate(false);
		Assert.assertTrue(data.isLoaded());
		Assert.assertEquals(4, data.getWidth());
	}

	@Test
	public void ignoresTheDecodeOfAReplacedDecoder() throws InterruptedException {
		final CountDownLatch release = new CountDownLatch(1);
		final CountDownLatch finished = new CountDownLatch(1);
		final ResourceData data = new ResourceData("image", new Decoder() {

			@Override
			public void decode(final @NonNull ResourceData resource) {
				try {
					release.await(5L, TimeUnit.SECONDS);
				} catch (final InterruptedException exception) {
					Thread.currentThread().interrupt();
				}
				finished.countDown();
				throw new IllegalStateException("late");
			}

		});
		data.generate(true);
		data.reload(new Decoder());
		release.countDown();
		Assert.assertTrue(finished.await(5L, TimeUnit.SECONDS));
		Thread.sleep(50L);
		Assert.assertFalse(data.isFailed());
		Assert.assertFalse(data.isLoaded());
	}

	@Test
	public void takesTheSizeOfItsRegion() {
		final ResourceData data = new ResourceData("atlas", null).texture(new Texture(64, 32)).region(16, 8, 32, 16);
		data.generate(false);
		Assert.assertArrayEquals(new int[] {16, 8, 32, 16}, data.getRegion());
		Assert.assertEquals(32, data.getWidth());
		Assert.assertEquals(16, data.getHeight());
		data.reload(null);
		Assert.assertNull(data.getRegion());
	}

	@Test
	public void clearsWithoutTextureNorDecoder() {
		final ResourceData data = new ResourceData("image", null).data(new int[][] {{1}});
		data.clear();
		Assert.assertNull(data.getData());
	}

	@Test
	public void releasesItsTexturesOnTheRenderThreadOnceCollected() throws Throwable {
		final Texture texture = new Texture(1, 1);
		new ResourceData("image", new Decoder()).texture(texture).finalize();
		Assert.assertFalse(texture.isDeleted());
		ResourceData.releaseCollected();
		Assert.assertTrue(texture.isDeleted());
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullId() {
		new ResourceData(null, null);
	}

	private static Resource resource(final ResourceData data) {
		return new Resource(ResourceBuilder.create().cache(null), data);
	}

	public static ResourceData load(final ResourceData data) {
		try {
			final Method method = ResourceData.class.getDeclaredMethod("fireLoad");
			method.setAccessible(true);
			method.invoke(data);
			return data;
		} catch (final ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

	public static ResourceData upload(final ResourceData data) {
		try {
			final Field field = ResourceData.class.getDeclaredField("uploaded");
			field.setAccessible(true);
			field.setBoolean(data, true);
			return data;
		} catch (final ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

	private static class Decoder implements IResourceDecoder {

		private final List<String>   calls   = new CopyOnWriteArrayList<>();
		private final CountDownLatch decoded = new CountDownLatch(1);

		private volatile Thread thread;

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
			this.thread = Thread.currentThread();
			this.calls.add("decode");
			this.decoded.countDown();
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
		}

	}

	@Getter
	private static final class Texture implements ITexture {

		private int     width;
		private int     height;
		private int[]   pixels;
		private boolean deleted;

		private Texture(final int width, final int height) {
			this.width  = width;
			this.height = height;
		}

		@Override
		public @NonNull ITexture mipmap(final boolean mipmap) {
			return this;
		}

		@Override
		public @NonNull ITexture allocate(final int width, final int height) {
			this.width  = width;
			this.height = height;
			return this;
		}

		@Override
		public @NonNull ITexture upload(final @NonNull int[] pixels) {
			this.pixels = pixels;
			return this;
		}

		@Override
		public boolean isMipmapped() {
			return false;
		}

		@Override
		public void delete() {
			this.deleted = true;
		}

	}

}