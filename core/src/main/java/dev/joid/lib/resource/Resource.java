package dev.joid.lib.resource;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import dev.joid.internal.JOIDEnv;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.resource.decoder.IResourceDecoder;
import dev.joid.lib.resource.playback.IResourcePlayback;
import dev.joid.lib.utils.thread.ThreadUtils;
import lombok.Getter;
import lombok.NonNull;

public final class Resource {

	private static final ResourceBuilder DEFAULT_BUILDER = ResourceBuilder.create().linear();

	private final ResourceData            data;
	@Getter private final ResourceBuilder builder;

	private final List<Consumer<Resource>>              loadListeners  = new CopyOnWriteArrayList<>();
	private final List<BiConsumer<Resource, Throwable>> errorListeners = new CopyOnWriteArrayList<>();

	@Getter private boolean            blocking;
	@Getter private ResourceProperties properties;

	private long restTime;
	private int  requestedWidth;
	private int  requestedHeight;

	public Resource(final @NonNull ResourceBuilder builder, final @NonNull ResourceData data) {
		this.builder    = builder;
		this.properties = builder.getProperties();
		this.data       = data;
		this.restTime   = Long.MIN_VALUE;
	}

	public static @NonNull Resource of(final @NonNull Object input) {
		return Resource.DEFAULT_BUILDER.of(input);
	}

	public @NonNull Resource properties(final @NonNull ResourceProperties properties) {
		this.properties = properties;
		return this;
	}

	public @NonNull Resource blocking() {
		this.blocking = true;
		return this;
	}

	public @NonNull Resource interpolation(final @NonNull TextureFilter interpolation) {
		this.properties.interpolation(interpolation);
		return this;
	}

	public @NonNull Resource linear() {
		this.properties.linear();
		return this;
	}

	public @NonNull Resource nearest() {
		this.properties.nearest();
		return this;
	}

	public @NonNull Resource mipmap(final boolean mipmap) {
		this.properties.mipmap(mipmap);
		return this;
	}

	public @NonNull Resource textureCoords(final double u, final double v, final double width, final double height) {
		this.properties.textureCoords(u, v, width, height);
		return this;
	}

	public @NonNull Resource uniqueId(final @NonNull String uniqueId) {
		this.data.uniqueId(uniqueId);
		return this;
	}

	public @NonNull Resource decoder(final IResourceDecoder decoder) {
		this.data.decoder(decoder);
		return this;
	}

	public @NonNull Resource onLoad(final @NonNull Consumer<@NonNull Resource> callback) {
		synchronized (this.data) {
			this.loadListeners.add(callback);
			this.data.listen(this);
			if (this.data.isLoaded()) {
				ThreadUtils.runOnRenderThread(() -> callback.accept(this));
			}
		}
		return this;
	}

	public @NonNull Resource onError(final @NonNull BiConsumer<@NonNull Resource, @NonNull Throwable> callback) {
		synchronized (this.data) {
			this.errorListeners.add(callback);
			this.data.listen(this);
			final Throwable error = this.data.getError();
			if (error != null) {
				ThreadUtils.runOnRenderThread(() -> callback.accept(this, error));
			}
		}
		return this;
	}

	public void fireLoad() {
		for (final Consumer<Resource> listener : this.loadListeners) {
			ThreadUtils.runOnRenderThread(() -> listener.accept(this));
		}
	}

	public void fireError(final @NonNull Throwable error) {
		for (final BiConsumer<Resource, Throwable> listener : this.errorListeners) {
			ThreadUtils.runOnRenderThread(() -> listener.accept(this, error));
		}
	}

	public @NonNull Resource reset() {
		this.properties = this.builder.getProperties();
		return this;
	}

	public @NonNull Resource copy() {
		return this.derive(this.data);
	}

	public @NonNull Resource fork() {
		final IResourceDecoder decoder = this.data.getDecoder();
		final IResourceDecoder forked = decoder == null ? null : decoder.fork();
		if (forked == null) {
			return this;
		}

		return this.derive(this.data.fork(forked));
	}

	public @NonNull Resource reload() {
		this.data.clear();
		return this;
	}

	public int getWidth() {
		final double[] textureCoords = this.properties.getTextureCoords();
		return textureCoords != null ? (int) Math.round(textureCoords[2]) : this.data.getWidth();
	}

	public int getHeight() {
		final double[] textureCoords = this.properties.getTextureCoords();
		return textureCoords != null ? (int) Math.round(textureCoords[3]) : this.data.getHeight();
	}

	public int getSourceWidth() {
		return this.data.getWidth();
	}

	public int getSourceHeight() {
		return this.data.getHeight();
	}

	public int[] getData() {
		return this.data.getData(0);
	}

	public int[] getData(final int index) {
		return this.data.getData(index);
	}

	public ITexture getTexture() {
		return this.data.getTexture(0);
	}

	public ITexture getTexture(final int index) {
		return this.data.getTexture(index);
	}

	public IResourceDecoder getDecoder() {
		return this.data.getDecoder();
	}

	public boolean isMipmappable() {
		return this.data.getDecoder() == null || this.data.getDecoder().isMipmappable();
	}

	public @NonNull String getUniqueId() {
		return this.data.getUniqueId();
	}

	public ResourceData getResourceData() {
		return this.data;
	}

	public IResourcePlayback getPlayback() {
		return this.data.getDecoder() instanceof IResourcePlayback ? (IResourcePlayback) this.data.getDecoder() : null;
	}

	public boolean isLoaded() {
		return this.data.isLoaded();
	}

	public boolean isUploaded() {
		return this.data.isUploaded();
	}

	public boolean isGenerated() {
		return this.data.isGenerated();
	}

	public boolean isFailed() {
		return this.data.isFailed();
	}

	public void generate() {
		this.data.generate(!this.blocking);
	}

	public void upload() {
		this.data.upload();
	}

	public void unbind() {
		BridgeHandler.RENDER.get().getState().texture(null);
	}

	public void bind(final @NonNull TextureWrap wrap, final @NonNull Runnable runnable) {
		this.bindTextureOnly(wrap);
		try {
			runnable.run();
		} finally {
			this.unbind();
		}
	}

	public void request(final int width, final int height) {
		final IResourceDecoder decoder = this.data.getDecoder();
		if (decoder == null || width <= 0 || height <= 0) {
			return;
		}

		final long now = BridgeHandler.CLOCK.get().nanoTime();
		if (this.requestedWidth != 0 && (width != this.requestedWidth || height != this.requestedHeight)) {
			this.restTime = now + 200000000L;
		}

		this.requestedWidth = width;
		this.requestedHeight = height;
		decoder.request(this.data, width, height, !this.blocking, now < this.restTime);
	}

	public void bindTextureOnly(final @NonNull TextureWrap wrap) {
		this.prepareBind();
		if (this.isFailed()) {
			if (JOIDEnv.isDevMode()) {
				BridgeHandler.RENDER.get().getState().texture(this.data.getMissingTexture()).textureFilter(TextureFilter.NEAREST).textureWrap(wrap);
			} else {
				BridgeHandler.RENDER.get().getState().texture(null);
			}
			return;
		}

		final IResourceDecoder decoder = this.data.getDecoder();
		if (decoder != null) {
			decoder.update(this.data);
		}

		final ITexture texture = this.getTexture();
		if (texture == null) {
			BridgeHandler.RENDER.get().getState().texture(null);
			return;
		}

		BridgeHandler.RENDER.get().getState().texture(texture).textureFilter(this.properties.getInterpolation()).textureWrap(wrap);
	}

	public void prepareBind() {
		if (this.blocking) {
			this.data.await();
		}

		if (!this.isGenerated()) {
			this.generate();
		}

		if (this.blocking) {
			this.data.await();
		}

		if (Boolean.TRUE.equals(this.properties.getMipmap()) && this.isMipmappable()) {
			for (int i = 0; i < this.data.getTextureCount(); i++) {
				this.data.getTexture(i).mipmap(true);
			}
		}

		if (this.isLoaded() && !this.isUploaded() && this.getData() != null) {
			this.upload();
		}
	}

	public void clear() {
		this.data.clear();
		this.builder.remove(this.data);
	}

	private @NonNull Resource derive(final @NonNull ResourceData data) {
		final Resource derived = new Resource(this.builder, data).properties(this.properties.copy());
		derived.blocking = this.blocking;
		return derived;
	}

}