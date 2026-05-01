package be.zeldown.joid.lib.resource;

import java.util.function.Consumer;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.resource.dto.ResourceData;
import be.zeldown.joid.lib.resource.dto.ResourceProperties;
import be.zeldown.joid.lib.resource.dto.decoder.IResourceDecoder;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Resource {

	private static final ResourceBuilder DEFAULT_BUILDER = ResourceBuilder.create().async().linear().cache(ResourceBuilder.DEFAULT_CACHE);

	private final ResourceBuilder builder;
	private final ResourceData data;

	private ResourceProperties properties;

	/* [ Constructor ] */
	protected Resource(final @NonNull ResourceBuilder builder, final @NonNull ResourceData data) {
		this.builder    = builder;
		this.properties = builder.getProperties().copy();
		this.data       = data;
	}

	public static @NonNull Resource of(final @NonNull Object input) {
		return Resource.DEFAULT_BUILDER.of(input);
	}

	public static @NonNull Resource of(final @NonNull Object input, final Consumer<Resource> callback) {
		return Resource.DEFAULT_BUILDER.of(input, callback);
	}

	/* [ Query Section ] */
	public final @NonNull Resource properties(final @NonNull ResourceProperties properties) {
		this.properties = properties;
		return this;
	}

	public final @NonNull Resource async() {
		this.properties.async();
		return this;
	}

	public final @NonNull Resource blocking() {
		this.properties.blocking();
		return this;
	}

	public final @NonNull Resource interpolation(final int interpolation) {
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

	public final @NonNull Resource textureCoords(final double u, final double v, final double u2, final double v2) {
		this.properties.textureCoords(u, v, u2, v2);
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

	public final @NonNull Resource reset() {
		this.properties = this.builder.getProperties().copy();
		return this;
	}

	public final @NonNull Resource copy() {
		return new Resource(this.builder, this.data);
	}

	/* [ Getter Section ] */
	public final @NonNull String getUniqueId() {
		return this.data.getUniqueId();
	}

	public final IResourceDecoder getDecoder() {
		return this.data.getDecoder();
	}

	public final int getTextureId() {
		return this.data.getTextureId() == null || this.data.getTextureId().length == 0 ? -1 : this.data.getTextureId()[0];
	}

	public final int getTextureId(final int index) {
		return this.data.getTextureId() == null || this.data.getTextureId().length <= index ? -1 : this.data.getTextureId()[index];
	}

	public final int[] getData() {
		return this.data.getData() == null || this.data.getData().length == 0 ? null : this.data.getData()[0];
	}

	public final int[] getData(final int index) {
		return this.data.getData() == null || this.data.getData().length <= index ? null : this.data.getData()[index];
	}

	public final boolean isGenerated() {
		return this.data.isGenerated();
	}

	public final boolean isLoaded() {
		return this.data.isLoaded();
	}

	public final boolean isUploaded() {
		return this.data.isUploaded();
	}

	public final int getWidth() {
		return this.data.getWidth();
	}

	public final int getHeight() {
		return this.data.getHeight();
	}

	public final ResourceData getResourceData() {
		return this.data;
	}

	/* [ Internal Section ] */
	public final void generate() {
		if (this.data == null || this.properties == null) {
			return;
		}

		this.data.generate(this.properties.isAsync());
	}

	public final void upload() {
		if (this.data == null) {
			return;
		}

		this.data.upload();
	}

	public final void bind(final @NonNull Runnable runnable) {
		this.bindTextureOnly();
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, this.properties.getInterpolation());
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, this.properties.getInterpolation());
		runnable.run();
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		this.unbind();
	}

	public final void bindTextureOnly() {
		this.prepareBind();
		if (!this.data.isUploaded()) {
			this.unbind();
		}

		final IResourceDecoder decoder = this.data.getDecoder();
		if (decoder == null) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.getTextureId(0));
		} else {
			if (!this.isGenerated()) {
				this.generate();
			}
			decoder.bind(this.data);
		}
	}

	public final void prepareBind() {
		if (!this.isGenerated()) {
			this.generate();
		}

		if (this.isLoaded() && !this.isUploaded() && this.getData() != null) {
			this.upload();
		}
	}

	public final void unbind() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	public final void clear() {
		if (this.data != null) {
			this.data.clear();
		}

		if (this.builder != null && this.builder.getCache() != null) {
			this.builder.getCache().invalidate(this.data.getUniqueId());
		}
	}

}