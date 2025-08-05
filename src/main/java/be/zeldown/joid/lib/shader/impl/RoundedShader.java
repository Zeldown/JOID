package be.zeldown.joid.lib.shader.impl;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.shader.uniform.Float4Uniform;
import be.zeldown.joid.lib.shader.uniform.FloatUniform;
import lombok.NonNull;

public class RoundedShader extends GLShaderImpl {

	private static final RoundedShader INSTANCE = new RoundedShader();

	private RoundedShader() {
		this.load(JOID.class.getResourceAsStream("/assets/shaders/rounded/rounded.vsh"), JOID.class.getResourceAsStream("/assets/shaders/rounded/rounded.fsh"));
	}
	public static void use(final float radius, final float x1, final float y1, final float x2, final float y2, final @NonNull Runnable runnable) {
		if (!RoundedShader.INSTANCE.isAvailable()) {
			return;
		}

		RoundedShader.INSTANCE.bind(radius, x1, y1, x2, y2);
		runnable.run();
		RoundedShader.INSTANCE.unbind();
	}

	public void bind(final float radius, final float x1, final float y1, final float x2, final float y2) {
		RoundedShader.INSTANCE.bind();
		final FloatUniform radiusUniform = RoundedShader.INSTANCE.shader.getFloatUniform("u_Radius");
		radiusUniform.setValue(radius);

		final Float4Uniform rectUniform = RoundedShader.INSTANCE.shader.getFloat4Uniform("u_InnerRect");
		rectUniform.setValue(x1, y1, x2, y2);
	}

	public static @NonNull RoundedShader inst() {
		return RoundedShader.INSTANCE;
	}

}