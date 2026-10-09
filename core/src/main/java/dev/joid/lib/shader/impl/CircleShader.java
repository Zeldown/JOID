package dev.joid.lib.shader.impl;

import dev.joid.lib.bridge.render.shader.source.CoreShader;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import javax.vecmath.Vector4f;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.color.ColorGradient;
import lombok.NonNull;

public class CircleShader extends ShaderProgram {

	private static final CircleShader INSTANCE = new CircleShader();

	private CircleShader() {
		this.load(CoreShader.CIRCLE.open(ShaderStage.VERTEX), CoreShader.CIRCLE.open(ShaderStage.FRAGMENT));
	}

	public static @NonNull CircleShader inst() {
		return CircleShader.INSTANCE;
	}

	public static void use(final float radius, final float centerX, final float centerY, final Runnable runnable) {
		if (!CircleShader.INSTANCE.canDraw()) {
			return;
		}

		final IRenderBridge render = BridgeHandler.RENDER.get();
		final IShader previousShader = render.getState().getShader();
		CircleShader.INSTANCE.bind(radius, centerX, centerY);
		try {
			if (runnable != null) {
				runnable.run();
			}
		} finally {
			CircleShader.INSTANCE.unbind();
			render.getState().shader(previousShader);
		}
	}

	public void bind(final float radius, final float centerX, final float centerY) {
		this.bind(radius, centerX, centerY, RoundedShaderType.AUTO);
	}

	public void bind(final float radius, final float centerX, final float centerY, final @NonNull RoundedShaderType type) {
		CircleShader.INSTANCE.bind();
		CircleShader.INSTANCE.getShader()
		.uniform("radius", radius)
		.uniform("center", centerX, centerY)
		.uniform("type", type.ordinal())
		.uniform("gradient", 0);
	}

	public void gradient(final @NonNull ColorGradient gradient, final @NonNull Vector4f canvas) {
		CircleShader.INSTANCE.getShader()
		.uniform("gradient", 1)
		.uniform("startPos", gradient.getDirection().x, gradient.getDirection().y)
		.uniform("endPos", gradient.getDirection().z, gradient.getDirection().w)
		.uniform("startColor", gradient.getStartColor().r, gradient.getStartColor().g, gradient.getStartColor().b, gradient.getStartColor().a)
		.uniform("endColor", gradient.getEndColor().r, gradient.getEndColor().g, gradient.getEndColor().b, gradient.getEndColor().a)
		.uniform("canvas", canvas.x, canvas.y, canvas.z, canvas.w);
	}

}