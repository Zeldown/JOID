package be.zeldown.joid.lib.shader.impl;

import java.io.InputStream;

import javax.vecmath.Vector2f;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.shader.GLShader;
import be.zeldown.joid.lib.shader.IGLShader;
import be.zeldown.joid.lib.shader.blend.ShaderBlendState;
import be.zeldown.joid.lib.shader.uniform.Float2Uniform;
import be.zeldown.joid.lib.shader.uniform.Float4Uniform;
import be.zeldown.joid.lib.shader.uniform.FloatUniform;
import lombok.Getter;
import lombok.NonNull;

public class GlowShader {

	@Getter private static IGLShader     shader;
	@Getter private static FloatUniform  radiusUniform;
	@Getter private static Float2Uniform centerUniform;
	@Getter private static Float4Uniform colorUniform;

	static {
		try {
			final InputStream vert = JOID.class.getResourceAsStream("/assets/shaders/glow/glow.vsh");
			final InputStream frag = JOID.class.getResourceAsStream("/assets/shaders/glow/glow.fsh");
			GlowShader.shader = GLShader.from(vert, frag, ShaderBlendState.NORMAL);
			if (GlowShader.shader.isActive()) {
				GlowShader.radiusUniform = GlowShader.shader.getFloatUniform("radius");
				GlowShader.centerUniform = GlowShader.shader.getFloat2Uniform("center");
				GlowShader.colorUniform  = GlowShader.shader.getFloat4Uniform("color");
			}
		} catch (final Exception e) {
			e.printStackTrace();

			GlowShader.shader        = null;
			GlowShader.radiusUniform = null;
			GlowShader.colorUniform  = null;
			GlowShader.centerUniform = null;
		}
	}

	public static void use(final @NonNull Vector2f center, final @NonNull Color color, final float radius) {
		if (!GlowShader.isAvailable()) {
			throw new RuntimeException("GlowShader is not available");
		}

		if (radius <= 0F) {
			throw new IllegalArgumentException("Radius must be greater than 0");
		}

		final double x = center.x - radius;
		final double y = center.y - radius;
		final double width = radius * 2;
		final double height = radius * 2;

		GlowShader.shader.bind();
		GlowShader.radiusUniform.setValue(radius);
		GlowShader.centerUniform.setValue(center.x, center.y);
		GlowShader.colorUniform.setValue(color.r, color.g, color.b, color.a);
		DrawUtils.SHAPE.drawRect(x, y, width, height, Color.WHITE);
		GlowShader.shader.unbind();
	}

	public static boolean isAvailable() {
		return GlowShader.shader != null;
	}

}