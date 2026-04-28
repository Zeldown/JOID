package be.zeldown.joid.lib.shader.impl;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.shader.uniform.Float2Uniform;
import be.zeldown.joid.lib.shader.uniform.Float4Uniform;
import be.zeldown.joid.lib.shader.uniform.IntUniform;
import lombok.NonNull;

public class GradientShader extends GLShaderImpl {

	private static final GradientShader INSTANCE = new GradientShader();

	private GradientShader() {
		this.load(JOID.class.getResourceAsStream("/assets/shaders/gradient/gradient.vsh"), JOID.class.getResourceAsStream("/assets/shaders/gradient/gradient.fsh"));
	}

	public static void use(final @NonNull Vector2f startPos, final @NonNull Vector2f endPos, final @NonNull Color startColor, final @NonNull Color endColor, final Runnable runnable, final @NonNull Vector4f canvas) {
		GradientShader.use(startPos, endPos, startColor, endColor, false, runnable, canvas);
	}

	public static void use(final @NonNull Vector2f startPos, final @NonNull Vector2f endPos, final @NonNull Color startColor, final @NonNull Color endColor, final boolean hasTexture, final Runnable runnable, final @NonNull Vector4f canvas) {
		if (!GradientShader.INSTANCE.isAvailable()) {
			return;
		}

		GradientShader.INSTANCE.bind();
		final Float2Uniform startPosUniform = GradientShader.INSTANCE.shader.getFloat2Uniform("startPos");
		startPosUniform.setValue(startPos.x, startPos.y);

		final Float2Uniform endPosUniform = GradientShader.INSTANCE.shader.getFloat2Uniform("endPos");
		endPosUniform.setValue(endPos.x, endPos.y);

		final Float4Uniform startColorUniform = GradientShader.INSTANCE.shader.getFloat4Uniform("startColor");
		startColorUniform.setValue(startColor.r, startColor.g, startColor.b, startColor.a);

		final Float4Uniform endColorUniform = GradientShader.INSTANCE.shader.getFloat4Uniform("endColor");
		endColorUniform.setValue(endColor.r, endColor.g, endColor.b, endColor.a);

		final IntUniform hasTextureUniform = GradientShader.INSTANCE.shader.getIntUniform("hasTexture");
		hasTextureUniform.setValue(hasTexture ? 1 : 0);

		final Float4Uniform canvasUniform = GradientShader.INSTANCE.shader.getFloat4Uniform("canvas");
		canvasUniform.setValue(canvas.x, canvas.y, canvas.z, canvas.w);

		if (runnable != null) {
			runnable.run();
		}

		GradientShader.INSTANCE.unbind();
	}

	public static @NonNull GradientShader inst() {
		return GradientShader.INSTANCE;
	}

}