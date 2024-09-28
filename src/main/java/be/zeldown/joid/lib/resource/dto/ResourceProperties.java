package be.zeldown.joid.lib.resource.dto;

import org.lwjgl.opengl.GL11;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class ResourceProperties {

	private boolean  async;
	private int      interpolation;
	private double[] textureCoords;

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

	public final @NonNull ResourceProperties interpolation(final int interpolation) {
		this.interpolation = interpolation;
		return this;
	}

	public final @NonNull ResourceProperties linear() {
		this.interpolation = GL11.GL_LINEAR;
		return this;
	}

	public final @NonNull ResourceProperties nearest() {
		this.interpolation = GL11.GL_NEAREST;
		return this;
	}

	public final @NonNull ResourceProperties textureCoords(final double u, final double v, final double u2, final double v2) {
		this.textureCoords = new double[] {u, v, u2, v2};
		return this;
	}

	public final @NonNull ResourceProperties copy() {
		final ResourceProperties copy = new ResourceProperties();
		copy.async = this.async;
		copy.interpolation = this.interpolation;
		copy.textureCoords = this.textureCoords;
		return copy;
	}

	public final @NonNull ResourceProperties copy(final @NonNull ResourceProperties properties) {
		this.async = properties.async;
		this.interpolation = properties.interpolation;
		this.textureCoords = properties.textureCoords;
		return this;
	}

}