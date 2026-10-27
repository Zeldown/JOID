package com.example.further;

import dev.joid.lib.shader.impl.ShaderProgram;

// #region shader
public final class WaveShader extends ShaderProgram {

	private static final WaveShader INSTANCE = new WaveShader();

	private WaveShader() {
		super.load("shaders/wave/wave.vsh", "shaders/wave/wave.fsh");
	}

	public static WaveShader inst() {
		return WaveShader.INSTANCE;
	}

	public static void use(final float time, final Runnable drawing) {
		WaveShader.INSTANCE.use(() -> WaveShader.INSTANCE.bind(time), drawing);
	}

	public void bind(final float time) {
		super.bind();
		super.getShader().uniform("u_Time", time);
	}

}
// #endregion