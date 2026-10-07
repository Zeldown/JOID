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
		final FloatUniform radiusUniform = RoundedShader.INSTANCE.shader.getFloatUniform("u_Radius");
		radiusUniform.setValue(radius);

		final Float4Uniform rectUniform = RoundedShader.INSTANCE.shader.getFloat4Uniform("u_InnerRect");
		rectUniform.setValue(x1, y1, x2, y2);

		final IntUniform typeUniform = RoundedShader.INSTANCE.shader.getIntUniform("u_Type");
		typeUniform.setValue(type.ordinal());

		final IntUniform gradientUniform = RoundedShader.INSTANCE.shader.getIntUniform("u_Gradient");
		gradientUniform.setValue(0);

		final FloatUniform strokeUniform = RoundedShader.INSTANCE.shader.getFloatUniform("u_Stroke");
		strokeUniform.setValue(0F);

		final IntUniform alignedUniform = RoundedShader.INSTANCE.shader.getIntUniform("u_Aligned");
		alignedUniform.setValue(1);
	}

	public void aligned(final boolean aligned) {
		final IntUniform alignedUniform = RoundedShader.INSTANCE.shader.getIntUniform("u_Aligned");
		alignedUniform.setValue(aligned ? 1 : 0);
	}

	public void stroke(final float stroke) {
		final FloatUniform strokeUniform = RoundedShader.INSTANCE.shader.getFloatUniform("u_Stroke");
		strokeUniform.setValue(stroke);
	}

	public void gradient(final @NonNull ColorGradient gradient, final @NonNull Vector4f canvas) {
		final IntUniform gradientUniform = RoundedShader.INSTANCE.shader.getIntUniform("u_Gradient");
		gradientUniform.setValue(1);

		final Float2Uniform startPosUniform = RoundedShader.INSTANCE.shader.getFloat2Uniform("u_StartPos");
		startPosUniform.setValue(gradient.getDirection().x, gradient.getDirection().y);

		final Float2Uniform endPosUniform = RoundedShader.INSTANCE.shader.getFloat2Uniform("u_EndPos");
		endPosUniform.setValue(gradient.getDirection().z, gradient.getDirection().w);

		final Float4Uniform startColorUniform = RoundedShader.INSTANCE.shader.getFloat4Uniform("u_StartColor");
		startColorUniform.setValue(gradient.getStartColor().r, gradient.getStartColor().g, gradient.getStartColor().b, gradient.getStartColor().a);

		final Float4Uniform endColorUniform = RoundedShader.INSTANCE.shader.getFloat4Uniform("u_EndColor");
		endColorUniform.setValue(gradient.getEndColor().r, gradient.getEndColor().g, gradient.getEndColor().b, gradient.getEndColor().a);

		final Float4Uniform canvasUniform = RoundedShader.INSTANCE.shader.getFloat4Uniform("u_Canvas");
		canvasUniform.setValue(canvas.x, canvas.y, canvas.z, canvas.w);
	}

}