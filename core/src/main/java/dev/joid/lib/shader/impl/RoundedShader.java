package dev.joid.lib.shader.impl;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.shader.source.CoreShader;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import lombok.NonNull;

public final class RoundedShader extends ShaderProgram {

	private static final RoundedShader INSTANCE = new RoundedShader();

	private RoundedShader() {
		super.load(CoreShader.ROUNDED.open(ShaderStage.VERTEX), CoreShader.ROUNDED.open(ShaderStage.FRAGMENT));
	}

	public static @NonNull RoundedShader inst() {
		return RoundedShader.INSTANCE;
	}

	public static void use(final float radius, final float x1, final float y1, final float x2, final float y2, final @NonNull Runnable runnable) {
		RoundedShader.INSTANCE.use(() -> RoundedShader.INSTANCE.bind(radius, x1, y1, x2, y2), runnable);
	}

	public void bind(final float radius, final float x1, final float y1, final float x2, final float y2) {
		this.bind(radius, x1, y1, x2, y2, ShaderFillType.AUTO);
	}

	public void bind(final float radius, final float x1, final float y1, final float x2, final float y2, final @NonNull ShaderFillType type) {
		super.bind();
		super.getShader()
		.uniform("u_Radius", radius)
		.uniform("u_InnerRect", x1, y1, x2, y2)
		.uniform("u_Type", type.ordinal())
		.uniform("u_HasGradient", 0)
		.uniform("u_Stroke", 0F)
		.uniform("u_Aligned", 1)
		.uniform("u_Pixel", (float) BridgeHandler.RENDER.get().getPixelGrid().getPixelSize());
	}

	public void aligned(final boolean aligned) {
		super.getShader().uniform("u_Aligned", aligned ? 1 : 0);
	}

	public void stroke(final float stroke) {
		super.getShader().uniform("u_Stroke", stroke);
	}

}