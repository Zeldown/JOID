package com.example.further;

import dev.joid.lib.shader.impl.ShaderProgram;

// #region shader
public final class InvertShader extends ShaderProgram {

	private static final InvertShader INSTANCE = new InvertShader();

	private InvertShader() {
		super.load("shaders/invert/invert.vsh", "shaders/invert/invert.fsh");
	}

	public static InvertShader inst() {
		return InvertShader.INSTANCE;
	}

	public void bind(final float amount) {
		super.bind();
		super.getShader().uniform("u_Amount", amount);
	}

}
// #endregion