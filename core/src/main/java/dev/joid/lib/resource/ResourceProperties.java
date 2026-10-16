package dev.joid.lib.resource;

import dev.joid.lib.bridge.render.texture.TextureFilter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceProperties {

	private Boolean       mipmap;
	private double[]      textureCoords;
	private TextureFilter interpolation = TextureFilter.LINEAR;

	public static @NonNull ResourceProperties create() {
		return new ResourceProperties();
	}

	public @NonNull ResourceProperties interpolation(final @NonNull TextureFilter interpolation) {
		this.interpolation = interpolation;
		return this;
	}

	public @NonNull ResourceProperties linear() {
		this.interpolation = TextureFilter.LINEAR;
		return this;
	}

	public @NonNull ResourceProperties nearest() {
		this.interpolation = TextureFilter.NEAREST;
		return this;
	}

	public @NonNull ResourceProperties mipmap(final boolean mipmap) {
		this.mipmap = mipmap;
		return this;
	}

	public @NonNull ResourceProperties textureCoords(final double u, final double v, final double width, final double height) {
		this.textureCoords = new double[] {u, v, width, height};
		return this;
	}

	public double[] getTextureCoords() {
		return this.textureCoords == null ? null : this.textureCoords.clone();
	}

	public @NonNull ResourceProperties copy() {
		final ResourceProperties copy = ResourceProperties.create();
		copy.mipmap = this.mipmap;
		copy.interpolation = this.interpolation;
		copy.textureCoords = this.textureCoords;
		return copy;
	}

	public @NonNull ResourceProperties copy(final @NonNull ResourceProperties properties) {
		this.mipmap = properties.mipmap;
		this.interpolation = properties.interpolation;
		this.textureCoords = properties.textureCoords;
		return this;
	}

}