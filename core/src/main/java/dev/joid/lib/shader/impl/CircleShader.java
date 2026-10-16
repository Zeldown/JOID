package dev.joid.lib.shader.impl;

import dev.joid.lib.bridge.render.shader.source.CoreShader;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import dev.joid.lib.color.ColorGradient;
import dev.joid.lib.utils.geom.BoundingBox;
import lombok.NonNull;

public final class CircleShader extends ShaderProgram {

	private static final CircleShader INSTANCE = new CircleShader();

	private CircleShader() {
		this.load(CoreShader.CIRCLE.open(ShaderStage.VERTEX), CoreShader.CIRCLE.open(ShaderStage.FRAGMENT));
	}

	public static @NonNull CircleShader inst() {
		return CircleShader.INSTANCE;
	}

	public static void use(final float radius, final float centerX, final float centerY, final @NonNull Runnable runnable) {
		CircleShader.INSTANCE.use(() -> CircleShader.INSTANCE.bind(radius, centerX, centerY), runnable);
	}

	public void bind(final float radius, final float centerX, final float centerY) {
		this.bind(radius, centerX, centerY, ShaderFillType.AUTO);
	}

	public void bind(final float radius, final float centerX, final float centerY, final @NonNull ShaderFillType type) {
		this.bind();
		this.getShader()
		.uniform("radius", radius)
		.uniform("center", centerX, centerY)
		.uniform("type", type.ordinal())
		.uniform("u_HasGradient", 0);
	}

	public void gradient(final @NonNull ColorGradient gradient, final @NonNull BoundingBox canvas) {
		ShaderProgram.uniformGradient(this.getShader(), gradient, (float) canvas.getMinX(), (float) canvas.getMinY(), (float) canvas.getMaxX(), (float) canvas.getMaxY());
	}

}