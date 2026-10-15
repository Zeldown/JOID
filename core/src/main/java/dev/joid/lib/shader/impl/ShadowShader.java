package dev.joid.lib.shader.impl;

import dev.joid.lib.bridge.render.shader.source.CoreShader;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import lombok.NonNull;

public final class ShadowShader extends ShaderProgram {

	private static final ShadowShader INSTANCE = new ShadowShader();

	private ShadowShader() {
		this.load(CoreShader.SHADOW.open(ShaderStage.VERTEX), CoreShader.SHADOW.open(ShaderStage.FRAGMENT));
	}

	public static @NonNull ShadowShader inst() {
		return ShadowShader.INSTANCE;
	}

	public static void use(final float radius, final float blur, final float x1, final float y1, final float x2, final float y2, final @NonNull Runnable runnable) {
		ShadowShader.INSTANCE.use(() -> ShadowShader.INSTANCE.bind(radius, blur, x1, y1, x2, y2), runnable);
	}

	public void bind(final float radius, final float blur, final float x1, final float y1, final float x2, final float y2) {
		this.bind();
		this.getShader()
		.uniform("u_Radius", radius)
		.uniform("u_Blur", blur)
		.uniform("u_Box", x1, y1, x2, y2)
		.uniform("u_Sides", 1F, 1F, 1F, 1F);
	}

	public void sides(final boolean left, final boolean top, final boolean right, final boolean bottom) {
		this.getShader().uniform("u_Sides", left ? 1F : 0F, top ? 1F : 0F, right ? 1F : 0F, bottom ? 1F : 0F);
	}

}