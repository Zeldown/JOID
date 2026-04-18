package be.zeldown.joid.lib.shader.impl;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.shader.uniform.Float2Uniform;
import be.zeldown.joid.lib.shader.uniform.FloatUniform;
import lombok.NonNull;

public class BlurShader extends GLShaderImpl {

	private static final BlurShader INSTANCE = new BlurShader();

	private BlurShader() {
		this.load(JOID.class.getResourceAsStream("/assets/shaders/blur/blur.vsh"), JOID.class.getResourceAsStream("/assets/shaders/blur/blur.fsh"));
	}

	public void bind(final float radius, final float dirX, final float dirY, final float texelW, final float texelH) {
		BlurShader.INSTANCE.bind();

		final FloatUniform radiusUniform = BlurShader.INSTANCE.shader.getFloatUniform("u_Radius");
		radiusUniform.setValue(radius);

		final Float2Uniform directionUniform = BlurShader.INSTANCE.shader.getFloat2Uniform("u_Direction");
		directionUniform.setValue(dirX, dirY);

		final Float2Uniform texelUniform = BlurShader.INSTANCE.shader.getFloat2Uniform("u_TexelSize");
		texelUniform.setValue(texelW, texelH);
	}

	public static @NonNull BlurShader inst() {
		return BlurShader.INSTANCE;
	}

}