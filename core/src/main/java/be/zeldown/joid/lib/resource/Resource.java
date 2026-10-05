package be.zeldown.joid.lib.resource;

import java.util.function.Consumer;

import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.bridge.render.texture.ITexture;
import be.zeldown.joid.lib.bridge.render.texture.TextureFilter;
import be.zeldown.joid.lib.bridge.render.texture.TextureWrap;
import be.zeldown.joid.lib.resource.dto.ResourceData;
import be.zeldown.joid.lib.resource.dto.ResourceProperties;
import be.zeldown.joid.lib.resource.dto.decoder.IResourceDecoder;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Resource {

	private static final ResourceBuilder DEFAULT_BUILDER = ResourceBuilder.create().async().linear().cache(ResourceBuilder.DEFAULT_CACHE);

	private final ResourceData data;
	private final ResourceBuilder builder;

	private ResourceProperties properties;

	public static @NonNull Resource of(final @NonNull Object input) {
		return Resource.DEFAULT_BUILDER.of(input);
	}

	protected Resource(final @NonNull ResourceBuilder builder, final @NonNull ResourceData data) {
		this.builder    = builder;
		this.properties = builder.getProperties().copy();
		this.data       = data;
	}

	public static @NonNull Resource of(final @NonNull Object input, final Consumer<Resource> callback) {
		return Resource.DEFAULT_BUILDER.of(input, callback);
	}

	public final void clear() {
		if (this.data != null) {
			this.data.clear();
		}

		if (this.builder != null && this.builder.getCache() != null) {
			this.builder.getCache().invalidate(this.data.getUniqueId());
		}
	}

	public final void upload() {
		if (this.data == null) {
			return;
		}
		this.data.upload();
	}

	public final void unbind() {
		BridgeHandler.RENDER.get().resetTexture();
	}

	public final int getWidth() {
		return this.data.getWidth();
	}

	public final int[] getData() {
		return this.data.getData() == null || this.data.getData().length == 0 ? null : this.data.getData()[0];
	}

	public final int getHeight() {
		return this.data.getHeight();
	}

	public final void generate() {
		if (this.data == null) {
			return;
		}
		this.data.generate(this.properties.isAsync());
	}

	public final boolean isLoaded() {
		return this.data.isLoaded();
	}

	public final void prepareBind() {
		if (!this.isGenerated()) {
			this.generate();
		}

		if (this.properties.isMipmap() && this.data.getTextures() != null) {
			for (final ITexture texture : this.data.getTextures()) {
				texture.mipmap(true);
			}
		}

		if (this.isLoaded() && !this.isUploaded() && this.getData() != null) {
			this.upload();
		}
	}

	public final boolean isUploaded() {
		return this.data.isUploaded();
	}

	public final ITexture getTexture() {
		return this.data.getTextures() == null || this.data.getTextures().length == 0 ? null : this.data.getTextures()[0];
	}

	public final boolean isGenerated() {
		return this.data.isGenerated();
	}

	public final @NonNull Resource copy() {
		return new Resource(this.builder, this.data);
	}

	public final @NonNull Resource async() {
		this.properties.async();
		return this;
	}

	public final @NonNull Resource reset() {
		this.properties = this.builder.getProperties().copy();
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

	public final @NonNull Resource blocking() {
		this.properties.blocking();
		this.data.await();
		return this;
	}

	public final @NonNull String getUniqueId() {
		return this.data.getUniqueId();
	}

	public final IResourceDecoder getDecoder() {
		return this.data.getDecoder();
	}

	public final int[] getData(final int index) {
		return this.data.getData() == null || this.data.getData().length <= index ? null : this.data.getData()[index];
	}

	public final ResourceData getResourceData() {
		return this.data;
	}

	public final ITexture getTexture(final int index) {
		return this.data.getTextures() == null || this.data.getTextures().length <= index ? null : this.data.getTextures()[index];
	}

	public final void dispatch(final @NonNull Runnable task) {
		this.data.dispatch(task, this.properties.isAsync());
	}

	public final @NonNull Resource mipmap(final boolean mipmap) {
		this.properties.mipmap(mipmap);
		return this;
	}

	public final void bindTextureOnly(final @NonNull TextureWrap wrap) {
		this.prepareBind();

		final IResourceDecoder decoder = this.data.getDecoder();
		if (decoder != null) {
			if (!this.isGenerated()) {
				this.generate();
			}
			decoder.update(this.data);
		}

		final ITexture texture = this.getTexture();
		if (texture == null) {
			BridgeHandler.RENDER.get().resetTexture();
			return;
		}

		BridgeHandler.RENDER.get().texture(texture, this.properties.getInterpolation(), wrap);
	}

	public final @NonNull Resource decoder(final IResourceDecoder decoder) {
		this.data.decoder(decoder);
		return this;
	}

	public final @NonNull Resource uniqueId(final @NonNull String uniqueId) {
		this.data.uniqueId(uniqueId);
		return this;
	}

	public final @NonNull Resource properties(final @NonNull ResourceProperties properties) {
		this.properties = properties;
		return this;
	}

	public final @NonNull Resource interpolation(final @NonNull TextureFilter interpolation) {
		this.properties.interpolation(interpolation);
		return this;
	}

	public final void bind(final @NonNull TextureWrap wrap, final @NonNull Runnable runnable) {
		this.bindTextureOnly(wrap);
		runnable.run();
		this.unbind();
	}

	public final @NonNull Resource textureCoords(final double u, final double v, final double u2, final double v2) {
		this.properties.textureCoords(u, v, u2, v2);
		return this;
	}

}