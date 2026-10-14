package dev.joid.lib.resource;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import dev.joid.internal.JOIDEnv;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.resource.cache.IResourceCache;
import dev.joid.lib.resource.decoder.IResourceDecoder;
import dev.joid.lib.resource.playback.IResourcePlayback;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Resource {

	private static final ResourceBuilder DEFAULT_BUILDER = ResourceBuilder.create().linear();

	private final ResourceData data;
	private final ResourceBuilder builder;

	private boolean            blocking;
	private ResourceProperties properties;

	private int  requestedWidth;
	private int  requestedHeight;
	private long restTime;

	protected Resource(final @NonNull ResourceBuilder builder, final @NonNull ResourceData data) {
		this.builder    = builder;
		this.properties = builder.getProperties();
		this.data       = data;
		this.restTime   = Long.MIN_VALUE;
	}

	public static @NonNull Resource of(final @NonNull Object input) {
		return Resource.DEFAULT_BUILDER.of(input);
	}

	public final @NonNull Resource properties(final @NonNull ResourceProperties properties) {
		this.properties = properties;
		return this;
	}

	public final @NonNull Resource blocking() {
		this.blocking = true;
		return this;
	}

	public final @NonNull Resource interpolation(final @NonNull TextureFilter interpolation) {
		this.properties.interpolation(interpolation);
		return this;
	}

	public final @NonNull Resource linear() {
		this.properties.linear();
		return this;
	}

	public final @NonNull Resource nearest() {
		this.properties.nearest();
		return this;
	}

	public final @NonNull Resource mipmap(final boolean mipmap) {
		this.properties.mipmap(mipmap);
		return this;
	}

	public final @NonNull Resource textureCoords(final double u, final double v, final double width, final double height) {
		this.properties.textureCoords(u, v, width, height);
		return this;
	}

	public final @NonNull Resource uniqueId(final @NonNull String uniqueId) {
		this.data.uniqueId(uniqueId);
		return this;
	}

	public final @NonNull Resource decoder(final IResourceDecoder decoder) {
		this.data.decoder(decoder);
		return this;
	}

	public final @NonNull Resource onLoad(final @NonNull Consumer<@NonNull Resource> callback) {
		this.data.onLoad(() -> callback.accept(this));
		return this;
	}

	public final @NonNull Resource onError(final @NonNull BiConsumer<@NonNull Resource, @NonNull Throwable> callback) {
		this.data.onError(error -> callback.accept(this, error));
		return this;
	}

	public final @NonNull Resource reset() {
		this.properties = this.builder.getProperties();
		return this;
	}

	public final @NonNull Resource copy() {
		final Resource copy = new Resource(this.builder, this.data).properties(this.properties.copy());
		copy.blocking = this.blocking;
		return copy;
	}

	public final @NonNull Resource fork() {
		final IResourceDecoder decoder = this.data.getDecoder();
		final IResourceDecoder forked = decoder == null ? null : decoder.fork();
		if (forked == null) {
			return this;
		}

		final Resource fork = new Resource(this.builder, this.data.fork(forked)).properties(this.properties.copy());
		fork.blocking = this.blocking;
		return fork;
	}

	public final @NonNull Resource reload() {
		this.data.clear();
		return this;
	}

	public final int getWidth() {
		final double[] textureCoords = this.properties.getTextureCoords();
		return textureCoords != null ? (int) Math.round(textureCoords[2]) : this.data.getWidth();
	}

	public final int getHeight() {
		final double[] textureCoords = this.properties.getTextureCoords();
		return textureCoords != null ? (int) Math.round(textureCoords[3]) : this.data.getHeight();
	}

	public final int getSourceWidth() {
		return this.data.getWidth();
	}

	public final int getSourceHeight() {
		return this.data.getHeight();
	}

	public final int[] getData() {
		return this.data.getData() == null || this.data.getData().length == 0 ? null : this.data.getData()[0];
	}

	public final int[] getData(final int index) {
		return this.data.getData() == null || this.data.getData().length <= index ? null : this.data.getData()[index];
	}

	public final ITexture getTexture() {
		return this.data.getTextures() == null || this.data.getTextures().length == 0 ? null : this.data.getTextures()[0];
	}

	public final ITexture getTexture(final int index) {
		return this.data.getTextures() == null || this.data.getTextures().length <= index ? null : this.data.getTextures()[index];
	}

	public final IResourceDecoder getDecoder() {
		return this.data.getDecoder();
	}

	public final boolean isMipmappable() {
		return this.data.getDecoder() == null || this.data.getDecoder().isMipmappable();
	}

	public final @NonNull String getUniqueId() {
		return this.data.getUniqueId();
	}

	public final ResourceData getResourceData() {
		return this.data;
	}

	public final IResourcePlayback getPlayback() {
		return this.data.getDecoder() instanceof IResourcePlayback ? (IResourcePlayback) this.data.getDecoder() : null;
	}

	public final boolean isLoaded() {
		return this.data.isLoaded();
	}

	public final boolean isUploaded() {
		return this.data.isUploaded();
	}

	public final boolean isGenerated() {
		return this.data.isGenerated();
	}

	public final boolean isFailed() {
		return this.data.isFailed();
	}

	public final void dispatch(final @NonNull Runnable task) {
		this.data.dispatch(task);
	}

	public final void generate() {
		this.data.generate(!this.blocking);
	}

	public final void upload() {
		this.data.upload();
	}

	public final void unbind() {
		BridgeHandler.RENDER.get().getState().texture(null);
	}

	public final void bind(final @NonNull TextureWrap wrap, final @NonNull Runnable runnable) {
		this.bindTextureOnly(wrap);
		try {
			runnable.run();
		} finally {
			this.unbind();
		}
	}

	public final void request(final int width, final int height) {
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

	public final void bindTextureOnly(final @NonNull TextureWrap wrap) {
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

	public final void prepareBind() {
		if (this.blocking) {
			this.data.await();
		}

		if (!this.isGenerated()) {
			this.generate();
		}

		if (this.blocking) {
			this.data.await();
		}

		if (Boolean.TRUE.equals(this.properties.getMipmap()) && this.isMipmappable() && this.data.getTextures() != null) {
			for (final ITexture texture : this.data.getTextures()) {
				texture.mipmap(true);
			}
		}

		if (this.isLoaded() && !this.isUploaded() && this.getData() != null) {
			this.upload();
		}
	}

	public final void clear() {
		this.data.clear();

		final IResourceCache cache = this.builder.getCache();
		if (cache != null && cache.get(this.data.getUniqueId()) == this.data) {
			cache.remove(this.data.getUniqueId());
		}
	}

}