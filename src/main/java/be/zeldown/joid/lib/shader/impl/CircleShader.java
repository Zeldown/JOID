package be.zeldown.joid.lib.shader.impl;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.shader.uniform.Float2Uniform;
import be.zeldown.joid.lib.shader.uniform.FloatUniform;
import lombok.NonNull;

public class CircleShader extends GLShaderImpl {

	private static final CircleShader INSTANCE = new CircleShader();

	private CircleShader() {
		this.load(JOID.class.getResourceAsStream("/assets/shaders/circle/circle.vsh"), JOID.class.getResourceAsStream("/assets/shaders/circle/circle.fsh"));
	}

	public static void use(final float radius, final float centerX, final float centerY, final Runnable runnable) {
		if (!CircleShader.INSTANCE.isAvailable()) {
			return;
		}

		CircleShader.INSTANCE.bind(radius, centerX, centerY);
		if (runnable != null) {
			runnable.run();
		}
		CircleShader.INSTANCE.unbind();
	}

	public void bind(final float radius, final float centerX, final float centerY) {
		CircleShader.INSTANCE.bind();
		final FloatUniform radiusUniform = CircleShader.INSTANCE.shader.getFloatUniform("radius");
		radiusUniform.setValue(radius);

		final Float2Uniform centerUniform = CircleShader.INSTANCE.shader.getFloat2Uniform("center");
		centerUniform.setValue(centerX, centerY);
	}

	public static @NonNull CircleShader inst() {
		return CircleShader.INSTANCE;
	}

}