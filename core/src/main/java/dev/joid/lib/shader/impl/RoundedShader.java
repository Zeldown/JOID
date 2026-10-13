package dev.joid.lib.shader.impl;

import dev.joid.lib.bridge.render.shader.source.CoreShader;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.color.ColorGradient;
import dev.joid.lib.utils.box.BoundingBox;
import lombok.NonNull;

public class RoundedShader extends ShaderProgram {

	private static final RoundedShader INSTANCE = new RoundedShader();

	private RoundedShader() {
		this.load(CoreShader.ROUNDED.open(ShaderStage.VERTEX), CoreShader.ROUNDED.open(ShaderStage.FRAGMENT));
	}

	public static @NonNull RoundedShader inst() {
		return RoundedShader.INSTANCE;
	}

	public static void use(final float radius, final float x1, final float y1, final float x2, final float y2, final @NonNull Runnable runnable) {
		if (!RoundedShader.INSTANCE.canDraw()) {
			return;
		}

		final IRenderBridge render = BridgeHandler.RENDER.get();
		final IShader previousShader = render.getState().getShader();
		RoundedShader.INSTANCE.bind(radius, x1, y1, x2, y2);
		try {
			runnable.run();
		} finally {
			RoundedShader.INSTANCE.unbind();
			render.getState().shader(previousShader);
		}
	}

	public void bind(final float radius, final float x1, final float y1, final float x2, final float y2) {
		this.bind(radius, x1, y1, x2, y2, RoundedShaderType.AUTO);
	}

	public void bind(final float radius, final float x1, final float y1, final float x2, final float y2, final @NonNull RoundedShaderType type) {
		RoundedShader.INSTANCE.bind();
		RoundedShader.INSTANCE.getShader()
		.uniform("u_Radius", radius)
		.uniform("u_InnerRect", x1, y1, x2, y2)
		.uniform("u_Type", type.ordinal())
		.uniform("u_Gradient", 0)
		.uniform("u_Stroke", 0F)
		.uniform("u_Aligned", 1);
	}

	public void aligned(final boolean aligned) {
		RoundedShader.INSTANCE.getShader().uniform("u_Aligned", aligned ? 1 : 0);
	}

	public void stroke(final float stroke) {
		RoundedShader.INSTANCE.getShader().uniform("u_Stroke", stroke);
	}

	public void gradient(final @NonNull ColorGradient gradient, final @NonNull BoundingBox canvas) {
		RoundedShader.INSTANCE.getShader()
		.uniform("u_Gradient", 1)
		.uniform("u_StartPos", (float) gradient.getStartPoint().getX(), (float) gradient.getStartPoint().getY())
		.uniform("u_EndPos", (float) gradient.getEndPoint().getX(), (float) gradient.getEndPoint().getY())
		.uniform("u_StartColor", gradient.getStartColor().r, gradient.getStartColor().g, gradient.getStartColor().b, gradient.getStartColor().a)
		.uniform("u_EndColor", gradient.getEndColor().r, gradient.getEndColor().g, gradient.getEndColor().b, gradient.getEndColor().a)
		.uniform("u_Canvas", (float) canvas.getMinX(), (float) canvas.getMinY(), (float) canvas.getMaxX(), (float) canvas.getMaxY());
	}

}