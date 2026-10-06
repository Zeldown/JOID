package dev.joid.lib.resource.dto;

import java.util.Optional;

import dev.joid.lib.bridge.render.texture.TextureFilter;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class ResourceProperties {

	private boolean       async;
	private Boolean       mipmap;
	private double[]      textureCoords;
	private TextureFilter interpolation = TextureFilter.NEAREST;

	public static @NonNull ResourceProperties create() {
		return new ResourceProperties();
	}

	public final @NonNull ResourceProperties async() {
		this.async = true;
		return this;
	}

	public final @NonNull ResourceProperties blocking() {
		this.async = false;
		return this;
	}

	public final @NonNull Optional<Boolean> getMipmap() {
		return Optional.ofNullable(this.mipmap);
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
		copy.async = this.async;
		copy.mipmap = this.mipmap;
		copy.interpolation = this.interpolation;
		copy.textureCoords = this.textureCoords;
		return copy;
	}

	public final @NonNull ResourceProperties copy(final @NonNull ResourceProperties properties) {
		this.async = properties.async;
		this.mipmap = properties.mipmap;
		this.interpolation = properties.interpolation;
		this.textureCoords = properties.textureCoords;
		return this;
	}

}