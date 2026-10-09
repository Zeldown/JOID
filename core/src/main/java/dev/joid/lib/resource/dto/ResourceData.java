package dev.joid.lib.resource.dto;

import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.texture.BorrowedTexture;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.resource.dto.decoder.IResourceDecoder;
import dev.joid.lib.utils.thread.ThreadUtils;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class ResourceData {

	private static final ExecutorService     ASYNC_EXECUTOR = Executors.newFixedThreadPool(16, ThreadUtils.daemonFactory("ResourceAsync"));
	private static final Queue<ResourceData> COLLECTED      = new ConcurrentLinkedQueue<>();

	private final List<Thread>              tasks          = new CopyOnWriteArrayList<>();
	private final List<Consumer<Throwable>> errorListeners = new CopyOnWriteArrayList<>();

	private String           uniqueId;
	private IResourceDecoder decoder;

	private int[][]    data;
	private ITexture[] textures;
	private ITexture   missingTexture;

	private volatile Throwable error;

	private boolean loaded;
	private boolean uploaded;
	private boolean generated;

	private int   width;
	private int   height;
	private int[] region;

	public ResourceData(final @NonNull String uniqueId, final IResourceDecoder decoder) {
		this.uniqueId = uniqueId;
		this.decoder  = decoder;
		if (this.decoder != null) {
			this.decoder.init(this);
		}
	}

	public final @NonNull ResourceData uniqueId(final @NonNull String uniqueId) {
		this.uniqueId = uniqueId;
		return this;
	}

	public final @NonNull ResourceData decoder(final IResourceDecoder decoder) {
		this.decoder = decoder;
		if (this.decoder != null) {
			this.decoder.init(this);
		}
		return this;
	}

	public final @NonNull ResourceData texture(final @NonNull ITexture texture) {
		this.textures = new ITexture[] {texture};
		return this;
	}

	public final @NonNull ResourceData textures(final ITexture[] textures) {
		this.textures = textures;
		return this;
	}

	public final @NonNull ResourceData data(final int[][] data) {
		this.data = data;
		return this;
	}

	public final @NonNull ResourceData generated(final boolean generated) {
		this.generated = generated;
		return this;
	}

	public final @NonNull ResourceData loaded(final boolean loaded) {
		this.loaded = loaded;
		return this;
	}

	public final @NonNull ResourceData uploaded(final boolean uploaded) {
		this.uploaded = uploaded;
		return this;
	}

	public final @NonNull ResourceData width(final int width) {
		this.width = width;
		return this;
	}

	public final @NonNull ResourceData height(final int height) {
		this.height = height;
		return this;
	}

	public final @NonNull ResourceData region(final int x, final int y, final int width, final int height) {
		this.region = new int[] {x, y, width, height};
		return this;
	}

	public final @NonNull ResourceData reload(final IResourceDecoder decoder) {
		this.await();
		if (this.decoder != null) {
			if (this.textures != null) {
				for (final ITexture texture : this.textures) {
					texture.delete();
				}
				this.textures = null;
			}
			this.decoder.clear(this);
		}

		if (this.missingTexture != null) {
			this.missingTexture.delete();
			this.missingTexture = null;
		}

		this.error     = null;
		this.data      = null;
		this.width     = 0;
		this.height    = 0;
		this.region    = null;
		this.loaded    = false;
		this.uploaded  = false;
		this.generated = false;
		return this.decoder(decoder);
	}

	public final void dispatch(final @NonNull Runnable task, final boolean async) {
		if (async) {
			final Thread thread = ThreadUtils.daemonThread(task, "ResourceTask/" + this.uniqueId);
			this.tasks.add(thread);
			thread.start();
		} else {
			task.run();
		}
	}

	public final void await() {
		for (final Thread task : this.tasks) {
			if (!task.isAlive()) {
				continue;
			}

			try {
				task.join();
			} catch (final InterruptedException silent) {
				Thread.currentThread().interrupt();
			}
		}
	}

	public final void generate(final boolean async) {
		if (this.error != null) {
			this.generated = true;
		} else if (this.decoder != null) {
			this.generated = true;

			try {
				this.decoder.prepare(this);
			} catch (final RuntimeException exception) {
				this.fail(exception);
				return;
			}

			final IResourceDecoder decoder = this.decoder;
			final Runnable task = () -> {
				try {
					decoder.decode(this);
				} catch (final RuntimeException exception) {
					if (this.decoder == decoder) {
						this.fail(exception);
					}
				}

				if (this.decoder == decoder && this.error == null) {
					this.loaded = true;
					this.uploaded = false;
				}
			};

			if (async) {
				ResourceData.ASYNC_EXECUTOR.execute(task);
			} else {
				task.run();
			}
		} else if (this.textures != null && this.textures.length > 0) {
			this.generated = true;
			if (this.textures[0] instanceof BorrowedTexture && !((BorrowedTexture<?>) this.textures[0]).isValid()) {
				this.fail(new IllegalArgumentException("The borrowed texture " + ((BorrowedTexture<?>) this.textures[0]).getHandle() + " is not a texture of the host"));
				return;
			}

			this.width = this.textures[0].getWidth();
			this.height = this.textures[0].getHeight();

			this.loaded = true;
			this.uploaded = false;
		}
	}

	public final void upload() {
		if (this.data == null) {
			return;
		}

		if (this.decoder == null) {
			for (int i = 0; i < this.textures.length; i++) {
				if (this.data[i] == null) {
					continue;
				}

				this.textures[i].allocate(this.width, this.height).upload(this.data[i], this.width, this.height);
			}
		} else {
			this.decoder.upload(this);
		}

		this.uploaded = true;
		this.data = null;
	}

	public final synchronized void fail(final @NonNull Throwable error) {
		if (this.error != null) {
			return;
		}

		this.error = error;
		this.loaded = false;
		this.data = null;
		if (JOID.inst().isDevMode()) {
			System.err.println("[JOID] The resource " + this.uniqueId + " cannot be read and is drawn empty: " + ResourceData.describe(error, !this.isBorrowed()));
		}

		for (final Consumer<Throwable> listener : this.errorListeners) {
			listener.accept(error);
		}
	}

	public final synchronized @NonNull ResourceData onError(final @NonNull Consumer<@NonNull Throwable> listener) {
		this.errorListeners.add(listener);
		if (this.error != null) {
			listener.accept(this.error);
		}
		return this;
	}

	public final boolean isFailed() {
		return this.error != null;
	}

	public final int getWidth() {
		if (this.region != null) {
			return this.region[2];
		}
		return this.isBorrowed() ? this.textures[0].getWidth() : this.width;
	}

	public final int getHeight() {
		if (this.region != null) {
			return this.region[3];
		}
		return this.isBorrowed() ? this.textures[0].getHeight() : this.height;
	}

	public final @NonNull ITexture getMissingTexture() {
		if (this.missingTexture == null) {
			final int[] pixels = new int[64];
			for (int i = 0; i < pixels.length; i++) {
				pixels[i] = (i / 8 + i % 8) % 2 == 0 ? 0xFFFF00FF : 0xFF000000;
			}
			this.missingTexture = BridgeHandler.RENDER.get().createTexture().allocate(8, 8).upload(pixels, 8, 8);
		}
		return this.missingTexture;
	}

	public final void clear() {
		if (this.textures != null) {
			for (final ITexture texture : this.textures) {
				texture.delete();
			}
		}

		if (this.missingTexture != null) {
			this.missingTexture.delete();
			this.missingTexture = null;
		}

		this.data = null;
		if (this.decoder != null) {
			this.decoder.clear(this);
		}
	}

	public static void releaseCollected() {
		for (ResourceData data = ResourceData.COLLECTED.poll(); data != null; data = ResourceData.COLLECTED.poll()) {
			data.clear();
		}
	}

	public final <T extends IResourceDecoder> T getDecoder(final @NonNull Class<T> clazz) {
		if (this.decoder == null || !clazz.isAssignableFrom(this.decoder.getClass())) {
			return null;
		}
		return clazz.cast(this.decoder);
	}

	private boolean isBorrowed() {
		return this.decoder == null && this.textures != null && this.textures.length > 0 && this.textures[0] instanceof BorrowedTexture;
	}

	private static @NonNull String describe(final @NonNull Throwable error, final boolean decodable) {
		Throwable cause = error;
		while (cause.getCause() != null && cause.getCause() != cause) {
			cause = cause.getCause();
		}

		final String reason = cause == error ? String.valueOf(error.getMessage()) : error.getMessage() + " (" + cause + ")";
		if (cause instanceof IOException) {
			return reason + ", check that the file or the URL exists and can be read";
		}
		return decodable ? reason + ", convert it to PNG, JPEG or WebP" : reason;
	}

	@Override
	protected void finalize() throws Throwable {
		ResourceData.COLLECTED.add(this);
		super.finalize();
	}

}