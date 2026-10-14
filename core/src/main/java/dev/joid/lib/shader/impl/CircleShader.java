package dev.joid.lib.shader.impl;

import dev.joid.lib.bridge.render.shader.source.CoreShader;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.color.ColorGradient;
import dev.joid.lib.utils.box.BoundingBox;
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

	public void gradient(final @NonNull ColorGradient gradient, final @NonNull BoundingBox canvas) {
		CircleShader.INSTANCE.getShader()
		.uniform("gradient", 1)
		.uniform("startPos", (float) gradient.getStartPoint().getX(), (float) gradient.getStartPoint().getY())
		.uniform("endPos", (float) gradient.getEndPoint().getX(), (float) gradient.getEndPoint().getY())
		.uniform("startColor", gradient.getStartColor().getRedF(), gradient.getStartColor().getGreenF(), gradient.getStartColor().getBlueF(), gradient.getStartColor().getAlphaF())
		.uniform("endColor", gradient.getEndColor().getRedF(), gradient.getEndColor().getGreenF(), gradient.getEndColor().getBlueF(), gradient.getEndColor().getAlphaF())
		.uniform("canvas", (float) canvas.getMinX(), (float) canvas.getMinY(), (float) canvas.getMaxX(), (float) canvas.getMaxY());
	}

}