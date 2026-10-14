package dev.joid.demo.shader.impl;

import dev.joid.lib.shader.impl.ShaderProgram;
import lombok.NonNull;

public final class DemoInvertShader extends ShaderProgram {

	private static final DemoInvertShader INSTANCE = new DemoInvertShader();

	private DemoInvertShader() {
		super.load("assets/demo/shaders/invert/invert.vsh", "assets/demo/shaders/invert/invert.fsh");
	}

	public static @NonNull DemoInvertShader inst() {
		return DemoInvertShader.INSTANCE;
	}

	public void bind(final float amount) {
		super.bind();
		super.getShader().uniform("u_Amount", amount);
	}

}