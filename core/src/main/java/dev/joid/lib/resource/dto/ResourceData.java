package dev.joid.lib.resource.dto;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.resource.dto.decoder.IResourceDecoder;
import dev.joid.lib.utils.thread.ThreadUtils;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class ResourceData {

	private static final ExecutorService     ASYNC_EXECUTOR = Executors.newFixedThreadPool(16, ThreadUtils.daemonFactory("ResourceAsync"));
	private static final Queue<ResourceData> COLLECTED      = new ConcurrentLinkedQueue<>();

	private final List<Thread> tasks = new CopyOnWriteArrayList<>();

	private String           uniqueId;
	private IResourceDecoder decoder;

	private int[][]    data;
	private ITexture[] textures;

	private boolean loaded;
	private boolean uploaded;
	private boolean generated;

	private int width;
	private int height;

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
		if (this.decoder != null) {
			this.generated = true;

			this.decoder.prepare(this);
			final Runnable task = () -> {
				this.decoder.decode(this);
				this.loaded = true;
				this.uploaded = false;
			};

			if (async) {
				ResourceData.ASYNC_EXECUTOR.execute(task);
			} else {
				task.run();
			}
		} else if (this.textures != null && this.textures.length > 0) {
			this.generated = true;
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

	public final void clear() {
		if (this.textures != null) {
			for (final ITexture texture : this.textures) {
				texture.delete();
			}
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

	public final <T extends IResourceDecoder> @NonNull Optional<T> getDecoder(final @NonNull Class<T> clazz) {
		return Optional.ofNullable(this.decoder).filter(clazz::isInstance).map(clazz::cast);
	}

	@Override
	protected void finalize() throws Throwable {
		ResourceData.COLLECTED.add(this);
		super.finalize();
	}

}