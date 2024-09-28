package be.zeldown.joid.lib.shader.impl;

import java.io.InputStream;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.shader.GLShader;
import be.zeldown.joid.lib.shader.IGLShader;
import be.zeldown.joid.lib.shader.blend.ShaderBlendState;
import be.zeldown.joid.lib.shader.uniform.Float2Uniform;
import be.zeldown.joid.lib.shader.uniform.FloatUniform;
import lombok.Getter;
import lombok.NonNull;

public class CircleShader {

	@Getter private static IGLShader     shader;
	@Getter private static FloatUniform  radiusUniform;
	@Getter private static Float2Uniform centerPosUniform;

	static {
		try {
			final InputStream vert = JOID.class.getResourceAsStream("/assets/shaders/circle/circle.vsh");
			final InputStream frag = JOID.class.getResourceAsStream("/assets/shaders/circle/circle.fsh");
			CircleShader.shader = GLShader.from(vert, frag, ShaderBlendState.NORMAL);
			if (CircleShader.shader.isActive()) {
				CircleShader.radiusUniform    = CircleShader.shader.getFloatUniform("u_Radius");
				CircleShader.centerPosUniform = CircleShader.shader.getFloat2Uniform("u_CenterPos");
			}
		}catch (final Exception e) {
			e.printStackTrace();

			CircleShader.shader           = null;
			CircleShader.radiusUniform    = null;
			CircleShader.centerPosUniform = null;
		}
	}

	public static void use(final float radius, final float centerX, final float centerY, final @NonNull Runnable runnable) {
		if (!CircleShader.isAvailable()) {
			throw new RuntimeException("CircleShader is not available");
		}

		CircleShader.shader.bind();
		CircleShader.radiusUniform.setValue(radius);
		CircleShader.centerPosUniform.setValue(centerX, centerY);
		runnable.run();
		CircleShader.shader.unbind();
	}

	public static boolean isAvailable() {
		return CircleShader.shader != null;
	}

}