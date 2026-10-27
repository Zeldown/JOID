package dev.joid.lib.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import dev.joid.internal.JOIDLog;
import dev.joid.lib.JOIDEnv;
import dev.joid.lib.asset.Asset;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.texture.BorrowedTexture;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.resource.decoder.IResourceDecoder;
import dev.joid.lib.resource.format.ResourceFormat;
import dev.joid.lib.utils.thread.ThreadUtils;
import dev.joid.lib.video.VideoNativesException;
import lombok.Getter;
import lombok.NonNull;

public final class ResourceData {

	private static final Queue<ResourceData> COLLECTED      = new ConcurrentLinkedQueue<>();
	private static final ExecutorService     ASYNC_EXECUTOR = Executors.newFixedThreadPool(16, ThreadUtils.daemonFactory("ResourceAsync"));

	private final Set<Resource>     waiting   = new HashSet<>();
	private final Set<Resource>     resources = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
	private final Set<ResourceData> forks     = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

	@Getter private String                    uniqueId;
	@Getter private volatile IResourceDecoder decoder;

	private Supplier<Asset> assetSource;

	private int[][]    data;
	private ITexture[] textures;

	private ITexture missingTexture;

	@Getter
	private volatile Throwable error;

	private volatile Future<?> decoding;
	private volatile Future<?> detecting;

	private volatile int     generation;
	private volatile boolean decodingRunning;

	private Runnable pendingRelease;

	@Getter private boolean          uploaded;
	@Getter private boolean          generated;
	@Getter private volatile boolean loaded;

	private int width;
	private int height;

	private int[] region;

	private ResourceData(final String uniqueId, final IResourceDecoder decoder) {
		this.uniqueId = uniqueId;
		this.decoder(decoder);
	}

	public static @NonNull ResourceData create(final @NonNull String uniqueId, final IResourceDecoder decoder) {
		return new ResourceData(uniqueId, decoder);
	}

	public @NonNull ResourceData uniqueId(final @NonNull String uniqueId) {
		this.uniqueId = uniqueId;
		return this;
	}

	public @NonNull ResourceData decoder(final IResourceDecoder decoder) {
		this.decoder = decoder;
		if (this.decoder != null) {
			this.decoder.init(this);
		}
		return this;
	}

	public @NonNull ResourceData assetSource(final Supplier<Asset> assetSource) {
		this.assetSource = assetSource;
		return this;
	}

	public @NonNull ResourceData texture(final @NonNull ITexture texture) {
		this.textures = new ITexture[] {texture};
		return this;
	}

	public @NonNull ResourceData textures(final ITexture[] textures) {
		this.textures = textures;
		return this;
	}

	public @NonNull ResourceData data(final int[][] data) {
		this.data = data;
		return this;
	}

	public @NonNull ResourceData generated(final boolean generated) {
		this.generated = generated;
		return this;
	}

	public @NonNull ResourceData width(final int width) {
		this.width = width;
		return this;
	}

	public @NonNull ResourceData height(final int height) {
		this.height = height;
		return this;
	}

	public @NonNull ResourceData region(final int x, final int y, final int width, final int height) {
		this.region = new int[] {x, y, width, height};
		return this;
	}

	public @NonNull ResourceData reload(final IResourceDecoder decoder) {
		this.await();
		this.clear();
		this.region = null;
		return this.decoder(decoder);
	}

	public @NonNull ResourceData fork(final @NonNull IResourceDecoder decoder) {
		final ResourceData fork = new ResourceData(this.uniqueId, decoder);
		this.forks.add(fork);
		return fork;
	}

	public void dispatch(final @NonNull Runnable task) {
		this.detecting = CompletableFuture.runAsync(task, runnable -> ThreadUtils.daemonThread(runnable, "ResourceTask/" + this.uniqueId).start());
	}

	public void await() {
		this.awaitDetection();
		ResourceData.await(this.decoding);
	}

	public void awaitDetection() {
		ResourceData.await(this.detecting);
	}

	public void detectDecoder(final boolean async) {
		final Asset asset;
		try {
			asset = this.assetSource.get();
		} catch (final RuntimeException exception) {
			this.fail(exception);
			return;
		}

		if (async && asset.isRemote()) {
			this.dispatch(() -> this.detectDecoder(asset));
		} else {
			this.detectDecoder(asset);
		}
	}

	public void generate(final boolean async) {
		if (this.error == null && this.decoder == null && this.assetSource != null && !this.isDispatching()) {
			this.detectDecoder(async);
		}

		if (this.error != null) {
			this.generated = true;
		} else if (this.decoder != null) {
			if (this.decodingRunning) {
				return;
			}

			this.generated = true;

			try {
				this.decoder.prepare(this);
			} catch (final RuntimeException exception) {
				this.fail(exception);
				return;
			}

			final IResourceDecoder decoder = this.decoder;
			final int generation = this.generation;
			final Runnable task = () -> {
				try {
					decoder.decode(this);
				} catch (final Throwable exception) {
					if (this.isCurrent(decoder, generation)) {
						this.fail(exception);
					}
				}

				if (this.isCurrent(decoder, generation) && this.error == null) {
					this.uploaded = false;
					this.fireLoad();
				}
			};

			if (async) {
				this.decodingRunning = true;
				this.decoding = ResourceData.ASYNC_EXECUTOR.submit(() -> {
					try {
						task.run();
					} finally {
						this.endDecoding();
					}
				});
			} else {
				task.run();
			}
		} else if (this.textures != null && this.textures.length > 0) {
			this.generated = true;
			if (this.textures[0] instanceof BorrowedTexture && !((BorrowedTexture<?>) this.textures[0]).isValid()) {
				this.fail(new IllegalArgumentException("The borrowed texture " + ((BorrowedTexture<?>) this.textures[0]).getHandle() + " is not a texture of the host, borrow a texture that the host created and has not deleted"));
				return;
			}

			this.width = this.textures[0].getWidth();
			this.height = this.textures[0].getHeight();

			this.uploaded = false;
			this.fireLoad();
		}
	}

	public void upload() {
		if (this.data == null) {
			return;
		}

		if (this.decoder == null) {
			this.uploadTextures();
		} else {
			this.decoder.upload(this);
		}

		this.uploaded = true;
		this.data = null;
	}

	public void uploadTextures() {
		for (int i = 0; i < this.textures.length; i++) {
			if (this.data[i] == null) {
				continue;
			}

			this.textures[i].allocate(this.width, this.height).upload(this.data[i]);
		}
	}

	public synchronized void fail(final @NonNull Throwable error) {
		if (this.error != null) {
			return;
		}

		this.error = error;
		this.loaded = false;
		this.data = null;
		if (JOIDEnv.isDevMode()) {
			JOIDLog.warn("The resource " + this.uniqueId + " cannot be read: " + ResourceData.describe(error) + ". It is drawn as a magenta and black checkerboard in dev mode, and nothing is drawn in production.");
		}

		for (final Resource resource : this.getResources()) {
			resource.fireError(error);
		}
		this.waiting.clear();
	}

	public synchronized void listen(final @NonNull Resource resource) {
		this.resources.add(resource);
		if (!this.loaded && this.error == null) {
			this.waiting.add(resource);
		}
	}

	public boolean isFailed() {
		return this.error != null;
	}

	public boolean isDispatching() {
		final Future<?> detecting = this.detecting;
		return detecting != null && !detecting.isDone();
	}

	public @NonNull List<@NonNull ResourceData> getForks() {
		synchronized (this.forks) {
			return new ArrayList<>(this.forks);
		}
	}

	public <T extends IResourceDecoder> T getDecoder(final @NonNull Class<T> clazz) {
		final IResourceDecoder decoder = this.decoder;
		return clazz.isInstance(decoder) ? clazz.cast(decoder) : null;
	}

	public int getWidth() {
		if (this.region != null) {
			return this.region[2];
		}
		return this.isBorrowed() ? this.textures[0].getWidth() : this.width;
	}

	public int getHeight() {
		if (this.region != null) {
			return this.region[3];
		}
		return this.isBorrowed() ? this.textures[0].getHeight() : this.height;
	}

	public int[] getRegion() {
		return this.region == null ? null : this.region.clone();
	}

	public int[][] getData() {
		return this.data == null ? null : this.data.clone();
	}

	public int[] getData(final int index) {
		final int[][] data = this.data;
		return data == null || data.length <= index ? null : data[index];
	}

	public ITexture[] getTextures() {
		return this.textures == null ? null : this.textures.clone();
	}

	public ITexture getTexture(final int index) {
		final ITexture[] textures = this.textures;
		return textures == null || textures.length <= index ? null : textures[index];
	}

	public int getTextureCount() {
		final ITexture[] textures = this.textures;
		return textures == null ? 0 : textures.length;
	}

	public @NonNull ITexture getMissingTexture() {
		if (this.missingTexture == null) {
			final int[] pixels = new int[64];
			for (int i = 0; i < pixels.length; i++) {
				pixels[i] = (i / 8 + i % 8) % 2 == 0 ? 0xFFFF00FF : 0xFF000000;
			}
			this.missingTexture = BridgeHandler.RENDER.get().createTexture().allocate(8, 8).upload(pixels);
		}
		return this.missingTexture;
	}

	public void clear() {
		final IResourceDecoder decoder = this.decoder;
		if (decoder != null) {
			final ITexture[] textures = this.textures;
			this.generation++;
			this.textures = null;
			this.width    = 0;
			this.height   = 0;
			this.release(() -> {
				if (textures != null) {
					for (final ITexture texture : textures) {
						texture.delete();
					}
				}
				decoder.clear(this);
			});
		}

		if (this.missingTexture != null) {
			this.missingTexture.delete();
			this.missingTexture = null;
		}

		this.data      = null;
		this.error     = null;
		this.loaded    = false;
		this.uploaded  = false;
		this.generated = false;
	}

	public static @NonNull ITexture placeholder() {
		return BridgeHandler.RENDER.get().createTexture().allocate(1, 1).upload(new int[] {0});
	}

	public static void releaseCollected() {
		for (ResourceData data = ResourceData.COLLECTED.poll(); data != null; data = ResourceData.COLLECTED.poll()) {
			data.clear();
		}
	}

	private @NonNull List<@NonNull Resource> getResources() {
		synchronized (this.resources) {
			return new ArrayList<>(this.resources);
		}
	}

	private void detectDecoder(final Asset asset) {
		try {
			final IResourceDecoder decoder = ResourceFormat.decoder(asset);
			if (this.decoder == null) {
				this.decoder(decoder);
			}
		} catch (final RuntimeException exception) {
			this.fail(exception);
		}
	}

	private boolean isCurrent(final IResourceDecoder decoder, final int generation) {
		return this.decoder == decoder && this.generation == generation;
	}

	private synchronized void release(final Runnable release) {
		if (!this.decodingRunning) {
			release.run();
			return;
		}

		final Runnable previous = this.pendingRelease;
		this.pendingRelease = previous == null ? release : () -> {
			previous.run();
			release.run();
		};
	}

	private synchronized void endDecoding() {
		if (this.pendingRelease == null) {
			this.decodingRunning = false;
			return;
		}

		ThreadUtils.runOnRenderThread(this::finishDecoding);
	}

	private synchronized void finishDecoding() {
		final Runnable release = this.pendingRelease;
		this.pendingRelease = null;
		this.decodingRunning = false;
		release.run();
	}

	private synchronized void fireLoad() {
		if (this.loaded) {
			return;
		}

		this.loaded = true;
		for (final Resource resource : this.getResources()) {
			resource.fireLoad();
		}
		this.waiting.clear();
	}

	private boolean isBorrowed() {
		return this.decoder == null && this.textures != null && this.textures.length > 0 && this.textures[0] instanceof BorrowedTexture;
	}

	private static void await(final Future<?> future) {
		if (future == null) {
			return;
		}

		try {
			future.get();
		} catch (final InterruptedException silent) {
			Thread.currentThread().interrupt();
		} catch (final ExecutionException silent) {}
	}

	private static @NonNull String describe(final Throwable error) {
		Throwable cause = error;
		while (cause.getCause() != null) {
			cause = cause.getCause();
		}

		final String reason = cause == error ? String.valueOf(error.getMessage()) : error.getMessage() + " (" + cause + ")";
		return !(error instanceof VideoNativesException) && cause instanceof IOException ? reason + ", check that the file or the URL exists and can be read" : reason;
	}

	@Override
	protected void finalize() throws Throwable {
		ResourceData.COLLECTED.add(this);
		super.finalize();
	}

}