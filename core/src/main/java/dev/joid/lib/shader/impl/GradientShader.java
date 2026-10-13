package dev.joid.lib.shader.impl;

import dev.joid.lib.bridge.render.shader.source.CoreShader;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.color.Color;
import dev.joid.lib.utils.box.BoundingBox;
import dev.joid.lib.utils.point.Point;
import lombok.NonNull;

public class GradientShader extends ShaderProgram {

	private static final GradientShader INSTANCE = new GradientShader();

	private GradientShader() {
		this.load(CoreShader.GRADIENT.open(ShaderStage.VERTEX), CoreShader.GRADIENT.open(ShaderStage.FRAGMENT));
	}

	public static @NonNull GradientShader inst() {
		return GradientShader.INSTANCE;
	}

	public static void use(final @NonNull Point startPoint, final @NonNull Point endPoint, final @NonNull Color startColor, final @NonNull Color endColor, final Runnable runnable, final @NonNull BoundingBox canvas) {
		GradientShader.use(startPoint, endPoint, startColor, endColor, false, runnable, canvas);
	}

	public static void use(final @NonNull Point startPoint, final @NonNull Point endPoint, final @NonNull Color startColor, final @NonNull Color endColor, final boolean hasTexture, final Runnable runnable, final @NonNull BoundingBox canvas) {
		if (!GradientShader.INSTANCE.canDraw()) {
			return;
		}

		final IRenderBridge render = BridgeHandler.RENDER.get();
		final IShader previousShader = render.getState().getShader();
		GradientShader.INSTANCE.bind();
		GradientShader.INSTANCE.getShader()
		.uniform("startPos", (float) startPoint.getX(), (float) startPoint.getY())
		.uniform("endPos", (float) endPoint.getX(), (float) endPoint.getY())
		.uniform("startColor", startColor.r, startColor.g, startColor.b, startColor.a)
		.uniform("endColor", endColor.r, endColor.g, endColor.b, endColor.a)
		.uniform("hasTexture", hasTexture ? 1 : 0)
		.uniform("canvas", (float) canvas.getMinX(), (float) canvas.getMinY(), (float) canvas.getMaxX(), (float) canvas.getMaxY());

		try {
			if (runnable != null) {
				runnable.run();
			}
		} finally {
			GradientShader.INSTANCE.unbind();
			render.getState().shader(previousShader);
		}
	}

}