package dev.joid.lib.resource;

import dev.joid.lib.bridge.render.texture.TextureFilter;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class ResourceProperties {

	private Boolean       mipmap;
	private double[]      textureCoords;
	private TextureFilter interpolation = TextureFilter.NEAREST;

	public static @NonNull ResourceProperties create() {
		return new ResourceProperties();
	}

	public final @NonNull ResourceProperties interpolation(final @NonNull TextureFilter interpolation) {
		this.interpolation = interpolation;
		return this;
	}

	public final @NonNull ResourceProperties linear() {
		this.interpolation = TextureFilter.LINEAR;
		return this;
	}

	public final @NonNull ResourceProperties nearest() {
		this.interpolation = TextureFilter.NEAREST;
		return this;
	}

	public final @NonNull ResourceProperties mipmap(final boolean mipmap) {
		this.mipmap = mipmap;
		return this;
	}

	public final @NonNull ResourceProperties textureCoords(final double u, final double v, final double width, final double height) {
		this.textureCoords = new double[] {u, v, width, height};
		return this;
	}

	public final @NonNull ResourceProperties copy() {
		final ResourceProperties copy = new ResourceProperties();
		copy.mipmap = this.mipmap;
		copy.interpolation = this.interpolation;
		copy.textureCoords = this.textureCoords;
		return copy;
	}

	public final @NonNull ResourceProperties copy(final @NonNull ResourceProperties properties) {
		this.mipmap = properties.mipmap;
		this.interpolation = properties.interpolation;
		this.textureCoords = properties.textureCoords;
		return this;
	}

}