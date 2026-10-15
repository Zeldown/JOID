package dev.joid.lib.shader.impl;

import dev.joid.lib.bridge.render.shader.source.CoreShader;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import dev.joid.lib.color.Color;
import dev.joid.lib.utils.box.BoundingBox;
import dev.joid.lib.utils.point.Point;
import lombok.NonNull;

public final class GradientShader extends ShaderProgram {

	private static final GradientShader INSTANCE = new GradientShader();

	private GradientShader() {
		this.load(CoreShader.GRADIENT.open(ShaderStage.VERTEX), CoreShader.GRADIENT.open(ShaderStage.FRAGMENT));
	}

	public static @NonNull GradientShader inst() {
		return GradientShader.INSTANCE;
	}

	public static void use(final @NonNull Point startPoint, final @NonNull Point endPoint, final @NonNull Color startColor, final @NonNull Color endColor, final @NonNull Runnable runnable, final @NonNull BoundingBox canvas) {
		GradientShader.use(startPoint, endPoint, startColor, endColor, false, runnable, canvas);
	}

	public static void use(final @NonNull Point startPoint, final @NonNull Point endPoint, final @NonNull Color startColor, final @NonNull Color endColor, final boolean hasTexture, final @NonNull Runnable runnable, final @NonNull BoundingBox canvas) {
		GradientShader.INSTANCE.use(() -> {
			GradientShader.INSTANCE.bind();
			ShaderProgram.uniformGradient(GradientShader.INSTANCE.getShader(), startPoint, endPoint, startColor, endColor, (float) canvas.getMinX(), (float) canvas.getMinY(), (float) canvas.getMaxX(), (float) canvas.getMaxY());
			GradientShader.INSTANCE.getShader().uniform("hasTexture", hasTexture ? 1 : 0);
		}, runnable);
	}

}