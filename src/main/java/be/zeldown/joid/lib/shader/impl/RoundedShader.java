package be.zeldown.joid.lib.shader.impl;

import java.io.InputStream;

import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.shader.GLShader;
import be.zeldown.joid.lib.shader.IGLShader;
import be.zeldown.joid.lib.shader.blend.ShaderBlendState;
import be.zeldown.joid.lib.shader.uniform.Float4Uniform;
import be.zeldown.joid.lib.shader.uniform.FloatUniform;
import lombok.Getter;
import lombok.NonNull;

public class RoundedShader {

	@Getter private static IGLShader     shader;
	@Getter private static FloatUniform  radiusUniform;
	@Getter private static Float4Uniform canvasUniform;

	static {
		try {
			final InputStream vert = JOID.class.getResourceAsStream("/assets/shaders/rounded/rounded.vsh");
			final InputStream frag = JOID.class.getResourceAsStream("/assets/shaders/rounded/rounded.fsh");
			RoundedShader.shader = GLShader.from(vert, frag, ShaderBlendState.NORMAL);
			if (RoundedShader.shader.isActive()) {
				RoundedShader.radiusUniform = RoundedShader.shader.getFloatUniform("radius");
				RoundedShader.canvasUniform = RoundedShader.shader.getFloat4Uniform("canvas");
			}
		} catch (final Exception e) {
			e.printStackTrace();

			RoundedShader.shader        = null;
			RoundedShader.radiusUniform = null;
			RoundedShader.canvasUniform = null;
		}
	}

	public static void use(final float radius, final @NonNull Runnable runnable, final @NonNull Vector4f canvas) {
		if (!RoundedShader.isAvailable()) {
			throw new RuntimeException("RoundedShader is not available");
		}

		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		RoundedShader.shader.bind();
		RoundedShader.radiusUniform.setValue(radius);
		RoundedShader.canvasUniform.setValue(canvas.x + radius, canvas.y + radius, canvas.z - radius, canvas.w - radius);
		runnable.run();
		RoundedShader.shader.unbind();
		GL11.glPopAttrib();
	}

	public static boolean isAvailable() {
		return RoundedShader.shader != null;
	}

}