package be.zeldown.joid.lib.shader.impl;

import java.io.InputStream;

import javax.vecmath.Vector4f;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.shader.GLShader;
import be.zeldown.joid.lib.shader.IGLShader;
import be.zeldown.joid.lib.shader.blend.ShaderBlendState;
import be.zeldown.joid.lib.shader.uniform.Float4Uniform;
import be.zeldown.joid.lib.shader.uniform.FloatUniform;
import lombok.Getter;
import lombok.NonNull;

public class BlurShader {

	@Getter private static IGLShader     shader;
	@Getter private static FloatUniform  radiusUniform;
	@Getter private static Float4Uniform canvasUniform;

	static {
		try {
			final InputStream vert = JOID.class.getResourceAsStream("/assets/shaders/blur/blur.vsh");
			final InputStream frag = JOID.class.getResourceAsStream("/assets/shaders/blur/blur.fsh");
			BlurShader.shader = GLShader.from(vert, frag, ShaderBlendState.NORMAL);
			if (BlurShader.shader.isActive()) {
				BlurShader.radiusUniform = BlurShader.shader.getFloatUniform("radius");
				BlurShader.canvasUniform = BlurShader.shader.getFloat4Uniform("canvas");
			}
		} catch (final Exception e) {
			e.printStackTrace();

			BlurShader.shader        = null;
			BlurShader.radiusUniform = null;
			BlurShader.canvasUniform = null;
		}
	}

	public static void use(final float radius, final @NonNull Runnable runnable, final @NonNull Vector4f canvas) {
		if (!BlurShader.isAvailable()) {
			throw new RuntimeException("GradientShader is not available");
		}

		BlurShader.shader.bind();
		BlurShader.radiusUniform.setValue(radius);
		BlurShader.canvasUniform.setValue(canvas.x, canvas.y, canvas.z, canvas.w);
		runnable.run();
		BlurShader.shader.unbind();
	}

	public static boolean isAvailable() {
		return BlurShader.shader != null;
	}

}