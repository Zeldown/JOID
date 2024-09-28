package be.zeldown.joid.lib.shader.impl;

import java.io.InputStream;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.shader.GLShader;
import be.zeldown.joid.lib.shader.IGLShader;
import be.zeldown.joid.lib.shader.blend.ShaderBlendState;
import be.zeldown.joid.lib.shader.uniform.Float4Uniform;
import be.zeldown.joid.lib.shader.uniform.FloatUniform;
import be.zeldown.joid.lib.shader.uniform.IntUniform;
import lombok.Getter;
import lombok.NonNull;

public class RoundedShader {

	@Getter private static IGLShader     shader;
	@Getter private static IntUniform    typeUniform;
	@Getter private static FloatUniform  radiusUniform;
	@Getter private static Float4Uniform innerRectUniform;

	static {
		try {
			final InputStream vert = JOID.class.getResourceAsStream("/assets/shaders/circle/rounded.vsh");
			final InputStream frag = JOID.class.getResourceAsStream("/assets/shaders/circle/rounded.fsh");
			RoundedShader.shader = GLShader.from(vert, frag, ShaderBlendState.NORMAL);
			if (RoundedShader.shader.isActive()) {
				RoundedShader.radiusUniform    = RoundedShader.shader.getFloatUniform("u_Radius");
				RoundedShader.innerRectUniform = RoundedShader.shader.getFloat4Uniform("u_InnerRect");
				RoundedShader.typeUniform      = RoundedShader.shader.getIntUniform("u_Type");
			}
		}catch (final Exception e) {
			e.printStackTrace();

			RoundedShader.shader           = null;
			RoundedShader.typeUniform      = null;
			RoundedShader.radiusUniform    = null;
			RoundedShader.innerRectUniform = null;
		}
	}

	public static void use(final RoundedShaderType type, final float radius, final float innerLeft, final float innerTop, final float innerRight, final float innerBottom, final @NonNull Runnable runnable) {
		if (!RoundedShader.isAvailable()) {
			throw new RuntimeException("RoundedShader is not available");
		}

		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		RoundedShader.shader.bind();
		RoundedShader.typeUniform.setValue(type.ordinal());
		RoundedShader.radiusUniform.setValue(radius);
		RoundedShader.innerRectUniform.setValue(innerLeft, innerTop, innerRight, innerBottom);
		runnable.run();
		RoundedShader.shader.unbind();
		GL11.glPopAttrib();
	}

	public static boolean isAvailable() {
		return RoundedShader.shader != null;
	}

	public enum RoundedShaderType {

		TEXTURE,
		COLOR

	}

}