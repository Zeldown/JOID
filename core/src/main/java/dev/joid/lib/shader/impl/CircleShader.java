package dev.joid.lib.shader.impl;

import dev.joid.internal.JOID;
import dev.joid.lib.bridge.render.shader.uniform.Float2Uniform;
import dev.joid.lib.bridge.render.shader.uniform.FloatUniform;
import dev.joid.lib.bridge.render.shader.uniform.IntUniform;
import lombok.NonNull;

public class CircleShader extends ShaderImpl {

	private static final CircleShader INSTANCE = new CircleShader();

	private CircleShader() {
		this.load(JOID.class.getResourceAsStream("/assets/shaders/circle/circle.vsh"), JOID.class.getResourceAsStream("/assets/shaders/circle/circle.fsh"));
	}

	public static @NonNull CircleShader inst() {
		return CircleShader.INSTANCE;
	}

	public static void use(final float radius, final float centerX, final float centerY, final Runnable runnable) {
		if (!CircleShader.INSTANCE.isAvailable()) {
			return;
		}

		CircleShader.INSTANCE.bind(radius, centerX, centerY);
		try {
			if (runnable != null) {
				runnable.run();
			}
		} finally {
			CircleShader.INSTANCE.unbind();
		}
	}

	public void bind(final float radius, final float centerX, final float centerY) {
		this.bind(radius, centerX, centerY, RoundedShaderType.AUTO);
	}

	public void bind(final float radius, final float centerX, final float centerY, final @NonNull RoundedShaderType type) {
		CircleShader.INSTANCE.bind();
		final FloatUniform radiusUniform = CircleShader.INSTANCE.shader.getFloatUniform("radius");
		radiusUniform.setValue(radius);

		final Float2Uniform centerUniform = CircleShader.INSTANCE.shader.getFloat2Uniform("center");
		centerUniform.setValue(centerX, centerY);

		final IntUniform typeUniform = CircleShader.INSTANCE.shader.getIntUniform("type");
		typeUniform.setValue(type.ordinal());
	}

	public enum RoundedShaderType {

		AUTO,
		TEXTURE,
		COLOR;

	}

}