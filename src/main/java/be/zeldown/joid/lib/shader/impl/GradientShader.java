package be.zeldown.joid.lib.shader.impl;

import java.io.InputStream;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.shader.GLShader;
import be.zeldown.joid.lib.shader.IGLShader;
import be.zeldown.joid.lib.shader.blend.ShaderBlendState;
import be.zeldown.joid.lib.shader.uniform.Float2Uniform;
import be.zeldown.joid.lib.shader.uniform.Float4Uniform;
import lombok.Getter;
import lombok.NonNull;

public class GradientShader {

	@Getter private static IGLShader     shader;
	@Getter private static Float2Uniform startPosUniform;
	@Getter private static Float2Uniform endPosUniform;
	@Getter private static Float4Uniform startColorUniform;
	@Getter private static Float4Uniform endColorUniform;
	@Getter private static Float4Uniform canvasUniform;

	static {
		try {
			final InputStream vert = JOID.class.getResourceAsStream("/assets/shaders/gradient/gradient.vsh");
			final InputStream frag = JOID.class.getResourceAsStream("/assets/shaders/gradient/gradient.fsh");
			GradientShader.shader = GLShader.from(vert, frag, ShaderBlendState.NORMAL);
			if (GradientShader.shader.isActive()) {
				GradientShader.startPosUniform   = GradientShader.shader.getFloat2Uniform("startPos");
				GradientShader.endPosUniform     = GradientShader.shader.getFloat2Uniform("endPos");
				GradientShader.startColorUniform = GradientShader.shader.getFloat4Uniform("startColor");
				GradientShader.endColorUniform   = GradientShader.shader.getFloat4Uniform("endColor");
				GradientShader.canvasUniform     = GradientShader.shader.getFloat4Uniform("canvas");
			}
		} catch (final Exception e) {
			e.printStackTrace();

			GradientShader.shader            = null;
			GradientShader.startPosUniform   = null;
			GradientShader.endPosUniform     = null;
			GradientShader.startColorUniform = null;
			GradientShader.endColorUniform   = null;
		}
	}

	public static void use(final @NonNull Vector2f startPos, final @NonNull Vector2f endPos, final @NonNull Color startColor, final @NonNull Color endColor, final @NonNull Vector4f canvas, final @NonNull Runnable runnable) {
		GradientShader.bind(startPos, endPos, startColor, endColor, canvas);
		runnable.run();
		GradientShader.shader.unbind();
	}

	public static void bind(final @NonNull Vector2f startPos, final @NonNull Vector2f endPos, final @NonNull Color startColor, final @NonNull Color endColor, final @NonNull Vector4f canvas) {
		if (!GradientShader.isAvailable()) {
			throw new RuntimeException("GradientShader is not available");
		}

		if (startPos.x < 0 || startPos.x > 1 || startPos.y < 0 || startPos.y > 1) {
			throw new RuntimeException("startPos have to be normalized [0, 1]");
		}

		if (endPos.x < 0 || endPos.x > 1 || endPos.y < 0 || endPos.y > 1) {
			throw new RuntimeException("endPos have to be be normalized [0, 1]");
		}

		GradientShader.shader.bind();
		GradientShader.startPosUniform.setValue(startPos.x, 1F - startPos.y);
		GradientShader.endPosUniform.setValue(endPos.x, 1F - endPos.y);
		GradientShader.startColorUniform.setValue(startColor.r, startColor.g, startColor.b, startColor.a);
		GradientShader.endColorUniform.setValue(endColor.r, endColor.g, endColor.b, endColor.a);
		GradientShader.canvasUniform.setValue(canvas.x, canvas.y, canvas.z, canvas.w);
	}

	public static void unbind() {
		if (!GradientShader.isAvailable()) {
			throw new RuntimeException("GradientShader is not available");
		}

		GradientShader.shader.unbind();
	}

	public static boolean isAvailable() {
		return GradientShader.shader != null;
	}

}