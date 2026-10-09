package dev.joid.lib.shader.impl;

import dev.joid.lib.bridge.render.shader.source.CoreShader;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.color.Color;
import lombok.NonNull;

public class GradientShader extends ShaderImpl {

	private static final GradientShader INSTANCE = new GradientShader();

	private GradientShader() {
		this.load(CoreShader.GRADIENT.open(ShaderStage.VERTEX), CoreShader.GRADIENT.open(ShaderStage.FRAGMENT));
	}

	public static @NonNull GradientShader inst() {
		return GradientShader.INSTANCE;
	}

	public static void use(final @NonNull Vector2f startPos, final @NonNull Vector2f endPos, final @NonNull Color startColor, final @NonNull Color endColor, final Runnable runnable, final @NonNull Vector4f canvas) {
		GradientShader.use(startPos, endPos, startColor, endColor, false, runnable, canvas);
	}

	public static void use(final @NonNull Vector2f startPos, final @NonNull Vector2f endPos, final @NonNull Color startColor, final @NonNull Color endColor, final boolean hasTexture, final Runnable runnable, final @NonNull Vector4f canvas) {
		if (!GradientShader.INSTANCE.canDraw()) {
			return;
		}

		final IRenderBridge render = BridgeHandler.RENDER.get();
		final IShader previousShader = render.getShader();
		GradientShader.INSTANCE.bind();
		GradientShader.INSTANCE.getShader()
		.uniform("startPos", startPos.x, startPos.y)
		.uniform("endPos", endPos.x, endPos.y)
		.uniform("startColor", startColor.r, startColor.g, startColor.b, startColor.a)
		.uniform("endColor", endColor.r, endColor.g, endColor.b, endColor.a)
		.uniform("hasTexture", hasTexture ? 1 : 0)
		.uniform("canvas", canvas.x, canvas.y, canvas.z, canvas.w);

		try {
			if (runnable != null) {
				runnable.run();
			}
		} finally {
			GradientShader.INSTANCE.unbind();
			render.shader(previousShader);
		}
	}

}