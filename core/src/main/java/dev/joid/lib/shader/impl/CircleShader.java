package dev.joid.lib.shader.impl;

import javax.vecmath.Vector4f;

import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.shader.uniform.Float2Uniform;
import dev.joid.lib.bridge.render.shader.uniform.Float4Uniform;
import dev.joid.lib.bridge.render.shader.uniform.FloatUniform;
import dev.joid.lib.bridge.render.shader.uniform.IntUniform;
import dev.joid.lib.color.ColorGradient;
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
		if (!CircleShader.INSTANCE.canDraw()) {
			return;
		}

		final IRenderBridge render = BridgeHandler.RENDER.get();
		final IShader previousShader = render.getShader();
		CircleShader.INSTANCE.bind(radius, centerX, centerY);
		try {
			if (runnable != null) {
				runnable.run();
			}
		} finally {
			CircleShader.INSTANCE.unbind();
			render.shader(previousShader);
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

		final IntUniform gradientUniform = CircleShader.INSTANCE.shader.getIntUniform("gradient");
		gradientUniform.setValue(0);
	}

	public void gradient(final @NonNull ColorGradient gradient, final @NonNull Vector4f canvas) {
		final IntUniform gradientUniform = CircleShader.INSTANCE.shader.getIntUniform("gradient");
		gradientUniform.setValue(1);

		final Float2Uniform startPosUniform = CircleShader.INSTANCE.shader.getFloat2Uniform("startPos");
		startPosUniform.setValue(gradient.getDirection().x, gradient.getDirection().y);

		final Float2Uniform endPosUniform = CircleShader.INSTANCE.shader.getFloat2Uniform("endPos");
		endPosUniform.setValue(gradient.getDirection().z, gradient.getDirection().w);

		final Float4Uniform startColorUniform = CircleShader.INSTANCE.shader.getFloat4Uniform("startColor");
		startColorUniform.setValue(gradient.getStartColor().r, gradient.getStartColor().g, gradient.getStartColor().b, gradient.getStartColor().a);

		final Float4Uniform endColorUniform = CircleShader.INSTANCE.shader.getFloat4Uniform("endColor");
		endColorUniform.setValue(gradient.getEndColor().r, gradient.getEndColor().g, gradient.getEndColor().b, gradient.getEndColor().a);

		final Float4Uniform canvasUniform = CircleShader.INSTANCE.shader.getFloat4Uniform("canvas");
		canvasUniform.setValue(canvas.x, canvas.y, canvas.z, canvas.w);
	}

}