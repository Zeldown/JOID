package dev.joid.lib.shader.impl;

import dev.joid.internal.JOID;
import lombok.NonNull;

public class BlurShader extends ShaderImpl {

	private static final BlurShader INSTANCE = new BlurShader();

	private BlurShader() {
		this.load(JOID.class.getResourceAsStream("/assets/shaders/blur/blur.vsh"), JOID.class.getResourceAsStream("/assets/shaders/blur/blur.fsh"));
	}

	public static @NonNull BlurShader inst() {
		return BlurShader.INSTANCE;
	}

	public void bind(final float radius, final float dirX, final float dirY, final float texelW, final float texelH) {
		BlurShader.INSTANCE.bind();

		BlurShader.INSTANCE.shader
		.uniform("u_Radius", radius)
		.uniform("u_Direction", dirX, dirY)
		.uniform("u_TexelSize", texelW, texelH);
	}

}