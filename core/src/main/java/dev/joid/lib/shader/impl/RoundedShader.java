package dev.joid.lib.shader.impl;

import javax.vecmath.Vector4f;

import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.color.ColorGradient;
import lombok.NonNull;

public class RoundedShader extends ShaderImpl {

	private static final RoundedShader INSTANCE = new RoundedShader();

	private RoundedShader() {
		this.load(JOID.class.getResourceAsStream("/assets/shaders/rounded/rounded.vsh"), JOID.class.getResourceAsStream("/assets/shaders/rounded/rounded.fsh"));
	}

	public static @NonNull RoundedShader inst() {
		return RoundedShader.INSTANCE;
	}

	public static void use(final float radius, final float x1, final float y1, final float x2, final float y2, final @NonNull Runnable runnable) {
		if (!RoundedShader.INSTANCE.canDraw()) {
			return;
		}

		final IRenderBridge render = BridgeHandler.RENDER.get();
		final IShader previousShader = render.getShader();
		RoundedShader.INSTANCE.bind(radius, x1, y1, x2, y2);
		try {
			runnable.run();
		} finally {
			RoundedShader.INSTANCE.unbind();
			render.shader(previousShader);
		}
	}

	public void bind(final float radius, final float x1, final float y1, final float x2, final float y2) {
		this.bind(radius, x1, y1, x2, y2, RoundedShaderType.AUTO);
	}

	public void bind(final float radius, final float x1, final float y1, final float x2, final float y2, final @NonNull RoundedShaderType type) {
		RoundedShader.INSTANCE.bind();
		RoundedShader.INSTANCE.shader
		.uniform("u_Radius", radius)
		.uniform("u_InnerRect", x1, y1, x2, y2)
		.uniform("u_Type", type.ordinal())
		.uniform("u_Gradient", 0)
		.uniform("u_Stroke", 0F)
		.uniform("u_Aligned", 1);
	}

	public void aligned(final boolean aligned) {
		RoundedShader.INSTANCE.shader.uniform("u_Aligned", aligned ? 1 : 0);
	}

	public void stroke(final float stroke) {
		RoundedShader.INSTANCE.shader.uniform("u_Stroke", stroke);
	}

	public void gradient(final @NonNull ColorGradient gradient, final @NonNull Vector4f canvas) {
		RoundedShader.INSTANCE.shader
		.uniform("u_Gradient", 1)
		.uniform("u_StartPos", gradient.getDirection().x, gradient.getDirection().y)
		.uniform("u_EndPos", gradient.getDirection().z, gradient.getDirection().w)
		.uniform("u_StartColor", gradient.getStartColor().r, gradient.getStartColor().g, gradient.getStartColor().b, gradient.getStartColor().a)
		.uniform("u_EndColor", gradient.getEndColor().r, gradient.getEndColor().g, gradient.getEndColor().b, gradient.getEndColor().a)
		.uniform("u_Canvas", canvas.x, canvas.y, canvas.z, canvas.w);
	}

}